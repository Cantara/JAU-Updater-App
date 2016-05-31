package no.cantara.jau.mjauu;

import no.cantara.jau.mjauu.state.Event;
import no.cantara.jau.mjauu.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Properties;

import static no.cantara.jau.mjauu.state.State.*;

public class Main {
    public static final String PROPERTIES_FILE_NAME = "config.properties";
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private final Properties properties = new Properties();
    private final String version;
    private Enum status = null;

    PrintWriter writer = null;
    FileWriter fw = null;
    BufferedWriter bw = null;

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
        main.issueEvent(Event.MjauuFinished);


    }

    public Main() throws IOException {
        InputStream inStream = new FileInputStream(PROPERTIES_FILE_NAME);//zipUri.openStream();
        properties.load(inStream);
        this.version = properties.getProperty("version");

    }

    void doUpdateProcess() throws URISyntaxException {
        File zipFile = findZip("java-auto-update-"+ version + ".zip");
        File toDir = findToDir("tmp");
        JauUpdater jauUpdater = new JauUpdater(zipFile, toDir);
        boolean unzipedOk = jauUpdater.extractZip();
        if (!unzipedOk){
            issueEvent(Event.UnzipFailed);
            return;
        }
        issueEvent(Event.UnzipOk);

        boolean backupJauOk = jauUpdater.backupJau();
        if (!backupJauOk){
            issueEvent(Event.BackupJauFailed);
            return;
        }
        issueEvent(Event.BackupJauOk);

        boolean unistallOk = jauUpdater.uninstallJau();
        if (!unistallOk){
            issueEvent(Event.UninstallFailed);
            return;
        }
        issueEvent(Event.UninstallOk);
        boolean configUpdatedOk = jauUpdater.updateConfig();
        if (!configUpdatedOk){
            issueEvent(Event.ConfigUpdatedFailed);
            return;
        }
        issueEvent(Event.ConfigUpdatedOk);
        boolean jauInstalledOk = jauUpdater.installJau();
        if (!jauInstalledOk){
            issueEvent(Event.JauInstallFailed);
            return;
        }
        issueEvent(Event.JauInstalledOk);
        boolean jauStartedOk = jauUpdater.startJau();
        if (!jauStartedOk){
            issueEvent(Event.JauInstallFailed);
            return;
        }
        issueEvent(Event.JauInstalledOk);

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

    void issueEvent(Event event){
        //TODO forward to ConfigService
        log.info("Event; {}", event);
    }

    void updateStatus(State status) {
        this.status = status;
        switch (status){
            case Started:
                log.info("Status;{}" + Started);
                issueEvent(Event.MjauuStarted);
                break;
            case Success:
                log.info("Status;{}" + Success);
                issueEvent(Event.UpgradSuccess);
                break;
            case Failure:
                log.info("Status;{}" + Failure);
                issueEvent(Event.UpgradeFailed);
                break;

        }


    }
}