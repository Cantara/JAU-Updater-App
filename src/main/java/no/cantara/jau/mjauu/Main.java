package no.cantara.jau.mjauu;

import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.client.ConfigServiceClient;
import no.cantara.cs.dto.CheckForUpdateRequest;
import no.cantara.cs.dto.Client;
import no.cantara.cs.dto.ClientConfig;
import no.cantara.cs.dto.event.ExtractedEventsStore;
import no.cantara.jau.mjauu.state.Event;
import no.cantara.jau.mjauu.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

import static no.cantara.jau.mjauu.state.State.*;

public class Main {
    public static final String MJAUU_OVERRIDES_PROPERTIES_FILE = "mjauu-override.properties"; // "config.properties";
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String APPLICATION_STATE_PROPERTIES_FILE = "applicationState.properties";
    private final Properties properties = new Properties();
    private final Properties applcationState = new Properties();
    private final String version;
    private final String artifactId;
    private final String newAppConfigId;
    private final String mjauuAppConfigId;
    private Enum status = null;
    private ConfigServiceClient configServiceClient;
    private ConfigServiceAdminClient adminClient;

    PrintWriter writer = null;
    FileWriter fw = null;
    BufferedWriter bw = null;
    private String clientId;
    private boolean autoUpgrade = true;
    private String customId = "";

    public static void main(String[] args) {
        Main main = null;

        try {
            main = new Main();
            Client client = main.registerClientOnNewCS();
            log.info("Continue with client {}", client);
            main.updateStatus(Started);
            boolean updatedOk = main.doUpdateProcess();
            if (updatedOk) {
                main.updateStatus(State.Success);
            } else {
                main.updateStatus(State.Failure);
                //FIXME parse content from log files.
                //TODO forward logs from files
            }
        } catch (URISyntaxException e) {
            log.error("Failed to update", e);
            main.updateStatus(State.Failure,e);
        } catch (IOException e) {
            log.error("Failed to load properties file {}", MJAUU_OVERRIDES_PROPERTIES_FILE);
            main.updateStatus(State.Failure, e);
        } catch (Exception e) {
            log.error("Failure to run MJAUU. Reason {}", e.getMessage(), e);
            try {
                main.updateStatus(State.Failure,e);
            } catch (Exception ie) {
                log.error("Failed to send notification to ConfigService. Original cause {}", e.getMessage(), ie);
            }
        }
        main.issueEvent(99,Event.MjauuFinished);
        log.info("Finished");



    }

    private Client registerClientOnNewCS() {
        Client persistedClient = null;
        Client client = null;// = new Client(clientId, mjauuAppConfigId,autoUpgrade);
        try {
           persistedClient = adminClient.getClient(clientId);

        } catch (IOException e) {
            log.info("No client found with id {}. Attempting to create a new one. Reason {}", clientId, e.getMessage());
        }
        if (persistedClient == null){
            client = new Client(clientId, mjauuAppConfigId, autoUpgrade);
            try {
                persistedClient =adminClient.putClient(client);
            } catch (IOException e) {
               log.warn("Failed to create client with clientId {}, mjauuAppConfigId {}, autoUpgrade {}. Reason {}",
                       clientId, mjauuAppConfigId, autoUpgrade, e.getMessage());
            }
        }
        return persistedClient;

    }

    public Main() throws IOException {
        InputStream inStream = new FileInputStream(MJAUU_OVERRIDES_PROPERTIES_FILE);//zipUri.openStream();
        properties.load(inStream);
        this.version = properties.getProperty("mjauu.version");

        String clientIdFromState = findClientIdFromApplcationState();
        //FIXME  implement override from JAU providedd config.properties file
        String configServiceUrl = properties.getProperty("configservice.url");
        String configServiceUsername = properties.getProperty("configservice.username");
        String configServicePassword = properties.getProperty("configservice.password");
        this.clientId = clientIdFromState;
        //this.clientId = properties.getProperty("configservice.clientid"); //FIXME find from applicationState.properties
        this.artifactId = properties.getProperty("configservice.artifactid");
        this.mjauuAppConfigId = properties.getProperty("mjauuApplicationConfigId");
        this.newAppConfigId = properties.getProperty("nextApplicationConfigId");
        configServiceClient = new ConfigServiceClient(configServiceUrl, configServiceUsername, configServicePassword);
        String adminUrl = configServiceUrl.replace("/client", "");
        adminClient = new ConfigServiceAdminClient(adminUrl, configServiceUsername, configServicePassword);


    }

    private String findClientIdFromApplcationState() {
        String clientId = null;
        InputStream inStream = null;//zipUri.openStream();
        try {
            inStream = new FileInputStream(APPLICATION_STATE_PROPERTIES_FILE);
            applcationState.load(inStream);
            clientId = applcationState.getProperty("clientId");
        } catch (FileNotFoundException e) {
            log.warn("Failed to load {}. Reason {}" , APPLICATION_STATE_PROPERTIES_FILE, e.getMessage());
        } catch (IOException e) {
            log.warn("Failed to load {}. Reason {}" , APPLICATION_STATE_PROPERTIES_FILE, e.getMessage());
        }

        return clientId;

    }

    boolean doUpdateProcess() throws URISyntaxException {
        boolean updatedOk = false;
        File zipFile = findZip("java-auto-update-"+ version + ".zip");
        File toDir = findToDir("tmp");
        JauUpdater jauUpdater = new JauUpdater(zipFile, toDir);
        boolean serviceStopedOk = jauUpdater.stopJau();
        if (!serviceStopedOk) {
            issueEvent(1,Event.JauStopFailed);
            return updatedOk;
        }
        issueEvent(1,Event.JauStopOk);
        boolean unzipedOk = jauUpdater.extractZip();
        if (!unzipedOk){
            issueEvent(1,Event.UnzipFailed);
            return updatedOk;
        }
        issueEvent(1,Event.UnzipOk);

        boolean backupJauOk = jauUpdater.backupJau();
        if (!backupJauOk){
            issueEvent(2,Event.BackupJauFailed);
            return updatedOk;
        }
        issueEvent(2,Event.BackupJauOk);

        boolean unistallOk = jauUpdater.uninstallJau();
        if (!unistallOk){
            issueEvent(3,Event.UninstallFailed);
            return updatedOk;
        }
        issueEvent(3,Event.UninstallOk);

        boolean configUpdatedOk = jauUpdater.updateConfig(artifactId,version);
        if (!configUpdatedOk){
            issueEvent(4,Event.ConfigUpdatedFailed);
            return updatedOk;
        }
        issueEvent(4,Event.ConfigUpdatedOk);
        boolean jauInstalledOk = jauUpdater.installJau();
        if (!jauInstalledOk){
            issueEvent(5,Event.JauInstallFailed);
            return updatedOk;
        }
        issueEvent(5,Event.JauInstalledOk);
        boolean linkedToNewAppConfigId = jauUpdater.connectClientToApplicationConfigId(adminClient,clientId,newAppConfigId);
        if (!linkedToNewAppConfigId){
            issueEvent(6,Event.LinkApplicationConfigFailed);
            return updatedOk;
        }
        issueEvent(6,Event.LinkApplicationConfigOk);
        boolean jauStartedOk = jauUpdater.startJau();
        if (!jauStartedOk){
            issueEvent(6,Event.JauInstallFailed);
            return updatedOk;
        }
        issueEvent(6,Event.JauInstalledOk);

        boolean upgradeVerified = jauUpdater.verifyUpgrade();
        if (!upgradeVerified){
            updateStatus(Failure);
            return updatedOk;
        }
        updateStatus(Success);
        updatedOk = true;
        return updatedOk;

        /*
        do {
            printStatus("Continue:");
            stopService("java-auto-update");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
        */
    }

    private File findToDir(String toDirName) {
        File toDir = new File(toDirName);
        return toDir;
    }

    private File findZip(String zipPath) throws URISyntaxException {
        URL zipSource = this.getClass().getClassLoader().getResource(zipPath);

        File zipFile;
        if (zipSource != null) {
            URI zipUri = new URI(zipSource.toString());
            zipFile = new File(zipUri);
        } else {
            zipFile = new File(zipPath);
        }

        return zipFile;
    }



    void issueEvent(int eventCount, Event event) {
        ExtractedEventsStore eventsStore = new ExtractedEventsStore();
        List<no.cantara.cs.dto.event.Event> events = new ArrayList<>();
        try {
            String eventText = event.name() + " - " + Instant.now().toString();
            no.cantara.cs.dto.event.Event csEvent = new no.cantara.cs.dto.event.Event(eventCount, eventText);
            csEvent.setGroupName("mjauu");
            csEvent.setTag("UPGRADE");
            csEvent.setFileName("logs/mjauu.log");
            events.add(csEvent);

            eventsStore.addEvents(events);

            Properties applicationState = configServiceClient.getApplicationState();
            clientId = applicationState.getProperty(ConfigServiceClient.CLIENT_ID);
            Map<String, String> envInfo = new HashMap<>();
            String configLastChanged = applicationState.getProperty(ConfigServiceClient.LAST_CHANGED);

            CheckForUpdateRequest updateRequest = new CheckForUpdateRequest(configLastChanged, envInfo, getClientId(),
                    eventsStore);

            ClientConfig clientConfig = configServiceClient.checkForUpdate(getClientId(),
                    updateRequest);
            log.info("Forwarded Event \"{}\" to configService.", event);
        } catch (IOException e) {
            log.warn("Failed to issue update Event to ConfigService. Reason {}", e.getMessage());
            //FIXME how to handle this error.
        } catch (Exception e) {
            log.warn("Error while creating event {}", event, e);
        }
    }

    void notifyFailure(String customId, Exception ex, Event upgradeFailed){
        log.error("Notify failure for customId {}, exeption {}", customId, ex);
        ExtractedEventsStore eventsStore = new ExtractedEventsStore();
        List<no.cantara.cs.dto.event.Event> events = new ArrayList<>();
        int eventCount = 0;
        try {
            File logFile = FileUtil.findLogFile();
            events = parseLogFile(eventCount, logFile);
            eventCount = eventCount + events.size() +1;
            String eventText = "CustomId: " + customId;
            if (ex != null) {
                eventText += ", Exception: " + ex ;
            }
            if (upgradeFailed != null) {
                eventText += ", Event {} " + upgradeFailed.name();
            }
            eventText +=  " - " + Instant.now().toString();
            no.cantara.cs.dto.event.Event csEvent = new no.cantara.cs.dto.event.Event(eventCount, eventText);

            csEvent.setGroupName("mjauu");
            csEvent.setTag("UPGRADE-FAILED-" + customId);
            csEvent.setFileName("logs/mjauu.log");
            events.add(csEvent);

            eventsStore.addEvents(events);

            Properties applicationState = configServiceClient.getApplicationState();
            clientId = applicationState.getProperty(ConfigServiceClient.CLIENT_ID);
            Map<String, String> envInfo = new HashMap<>();
            String configLastChanged = applicationState.getProperty(ConfigServiceClient.LAST_CHANGED);

            CheckForUpdateRequest updateRequest = new CheckForUpdateRequest(configLastChanged, envInfo, getClientId(),
                    eventsStore);

            ClientConfig clientConfig = configServiceClient.checkForUpdate(getClientId(),
                    updateRequest);
            log.info("Forwarded Failure {} to configService. CustomId {]", ex, customId);
        } catch (IOException e) {
            log.warn("Failed to issue update Failure to ConfigService. Reason {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Error while creating event {}", ex, e);
        }

    }

    private List<no.cantara.cs.dto.event.Event> parseLogFile( int eventCount, File logFile) {
        List<no.cantara.cs.dto.event.Event> events = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(logFile.toPath(), StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {
                // ++eventCount;
                //   if (hasStartPattern(line) || event == null) {
                no.cantara.cs.dto.event.Event event = new no.cantara.cs.dto.event.Event(eventCount, line);
                event.setGroupName("mjauu");
                event.setTag("UPGRADE-FAILED-" + customId);
                event.setFileName("logs/mjauu.log");
                events.add(event);
                // } else if (event.getLine().length() < MAX_LINE_LENGTH) {
                // Append to the current log event if this line is a continuation.
                //   event.setLine(event.getLine() + "\n" + line);
                //}
            }

        } catch (IOException e) {
            log.error("Error reading log file {}", logFile, e);
      //  } catch (IOException e) {
        //    e.printStackTrace();
        }
        return events;
    }



    void updateStatus(State status) {
        this.status = status;
        switch (status){
            case Started:
                log.info("Status;{}", Started);
                issueEvent(0,Event.MjauuStarted);
                break;
            case Success:
                log.info("Status;{}" , Success);
                issueEvent(99,Event.UpgradeSuccess);
                break;
            case Failure:
                log.info("Status;{}" ,Failure);
                notifyFailure(customId,null, Event.UpgradeFailed);
                //issueEvent(98,Event.UpgradeFailed);
                break;

        }
    }

    void updateStatus(State status, Exception e) {
        if (e == null ){
            updateStatus(status);
        } else {
            this.status = status;
            switch (status) {

                case Failure:
                    log.info("Status;{}", Failure);
                    notifyFailure(customId, e, Event.UpgradeFailed);
                    break;
                default:
                    updateStatus(status);
            }
        }
    }

    public String getClientId() {
        return clientId;
    }

}