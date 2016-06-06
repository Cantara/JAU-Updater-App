package no.cantara.jau.mjauu;

import no.cantara.cs.client.ConfigServiceClient;
import no.cantara.cs.dto.CheckForUpdateRequest;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static no.cantara.jau.mjauu.state.State.*;

public class Main {
    public static final String PROPERTIES_FILE_NAME = "config.properties";
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private final Properties properties = new Properties();
    private final String version;
    private Enum status = null;
    private ConfigServiceClient configServiceClient;

    PrintWriter writer = null;
    FileWriter fw = null;
    BufferedWriter bw = null;
    private String clientId;

    public static void main(String[] args) {
        Main main = null;


        //main.stopService("java-auto-update");

        try {
            main = new Main();
            main.updateStatus(Started);
            main.doUpdateProcess();
            main.updateStatus(State.Success);
        } catch (URISyntaxException e) {
            log.error("Failed to update", e);
            main.updateStatus(State.Failure);
        } catch (IOException e) {
            log.error("Failed to load properties file {}", PROPERTIES_FILE_NAME);
            main.updateStatus(State.Failure);
        }
        main.issueEvent(99,Event.MjauuFinished);


    }

    public Main() throws IOException {
        InputStream inStream = new FileInputStream(PROPERTIES_FILE_NAME);//zipUri.openStream();
        properties.load(inStream);
        this.version = properties.getProperty("version");
        String configServiceUrl = properties.getProperty("configServiceUrl");
        String configServiceUsername = properties.getProperty("configServiceUsername");
        String configServicePassword = properties.getProperty("configServicePassword");
        configServiceClient = new ConfigServiceClient(configServiceUrl, configServiceUsername, configServicePassword);

    }

    void doUpdateProcess() throws URISyntaxException {
        File zipFile = findZip("java-auto-update-"+ version + ".zip");
        File toDir = findToDir("tmp");
        JauUpdater jauUpdater = new JauUpdater(zipFile, toDir);
        boolean unzipedOk = jauUpdater.extractZip();
        if (!unzipedOk){
            issueEvent(1,Event.UnzipFailed);
            return;
        }
        issueEvent(1,Event.UnzipOk);

        boolean backupJauOk = jauUpdater.backupJau();
        if (!backupJauOk){
            issueEvent(2,Event.BackupJauFailed);
            return;
        }
        issueEvent(2,Event.BackupJauOk);

        boolean unistallOk = jauUpdater.uninstallJau();
        if (!unistallOk){
            issueEvent(3,Event.UninstallFailed);
            return;
        }
        issueEvent(3,Event.UninstallOk);
        boolean configUpdatedOk = jauUpdater.updateConfig();
        if (!configUpdatedOk){
            issueEvent(4,Event.ConfigUpdatedFailed);
            return;
        }
        issueEvent(4,Event.ConfigUpdatedOk);
        boolean jauInstalledOk = jauUpdater.installJau();
        if (!jauInstalledOk){
            issueEvent(5,Event.JauInstallFailed);
            return;
        }
        issueEvent(5,Event.JauInstalledOk);
        boolean jauStartedOk = jauUpdater.startJau();
        if (!jauStartedOk){
            issueEvent(6,Event.JauInstallFailed);
            return;
        }
        issueEvent(6,Event.JauInstalledOk);

        boolean upgradeVerified = jauUpdater.verifyUpgrade();
        if (!upgradeVerified){
           updateStatus(Failure);
            return;
        }
        updateStatus(Success);

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

    private void stopService(String serviceId) {
        String[] command = {"cmd.exe", "/c", "net", "stop", "java-auto-update"};
        command = new String[]{"cmd", "/c","net", "stop", serviceId};

        try {
            printStatus("Run command: " + buildString(command));
            Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info(line);
                printStatus(line);
            }
            log.info("Done.");
            printStatus("Done.");
        } catch(Exception ex) {
            log.warn("Exception : "+ex);
            writer.print("Exception: " + ex.toString());
        }
    }

    void printStatus(String status){


        try {
            fw = new FileWriter("hei2.txt", true);
            bw = new BufferedWriter(fw);
            writer = new PrintWriter(bw); //new PrintWriter("hei2.txt", "UTF-8");

            writer.println(status + Instant.now());
        } catch(Exception ex) {
            log.warn("Exception : "+ex);
            writer.print("Exception: " + ex.toString());
        } finally {
            writer.close();
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String buildString(String[] command) {
        StringBuilder builder = new StringBuilder();
        for(String s : command) {
            builder.append(" " +s);
        }
        return builder.toString();
    }

    void issueEvent(int eventCount, Event event) {
        ExtractedEventsStore eventsStore = new ExtractedEventsStore();
        List<no.cantara.cs.dto.event.Event> events = new ArrayList<>();
        events.add(new no.cantara.cs.dto.event.Event(eventCount, event.name()));
        eventsStore.addEvents(events);

        Properties applicationState = configServiceClient.getApplicationState();
        try {
            ClientConfig clientConfig = configServiceClient.checkForUpdate(getClientId(),
                    new CheckForUpdateRequest(applicationState.getProperty(ConfigServiceClient.LAST_CHANGED)));
            log.info("Forwarded Event {} to configService.", event);
        } catch (IOException e) {
            log.warn("Failed to issue update Event to ConfigService");
            //FIXME how to handle this error.
        }
    }

    void updateStatus(State status) {
        this.status = status;
        switch (status){
            case Started:
                log.info("Status;{}" + Started);
                issueEvent(0,Event.MjauuStarted);
                break;
            case Success:
                log.info("Status;{}" + Success);
                issueEvent(99,Event.UpgradeSuccess);
                break;
            case Failure:
                log.info("Status;{}" + Failure);
                issueEvent(98,Event.UpgradeFailed);
                break;

        }


    }

    public String getClientId() {
        return clientId;
    }
}