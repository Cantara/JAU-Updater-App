package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by baardl on 27/05/2016.
 */
public class JauUpdater {
    private static final Logger log = LoggerFactory.getLogger(JauUpdater.class);
    private static final String JAU_SERVICE_NAME = "java-auto-update";

    //URL url = getClass().getClassLoader().getResource("validconfig.properties");
    //JauProperties props = Util.getAndVerifyProperties(new File(url.toURI()));
    private final File zipFile;
    private final File toDir;
    private final File backupDir;
    private final File jauDir;
    private final JauServiceCommander serviceCommander;


    public JauUpdater(File zipFile, File toDir) {
        this.zipFile = zipFile;
        this.toDir = toDir;
        if (!zipFile.exists()){
            throw new IllegalArgumentException("ZipFile " + zipFile.toString() + " does not exist.");
        }
        backupDir = new File("backup");
        jauDir = new File("");
        serviceCommander = new JauServiceCommander(JAU_SERVICE_NAME);
    }


    public boolean stopJau(){

        boolean serviceStoped = serviceCommander.stopService();

        return serviceStoped;
    }
    public boolean extractZip() {
        boolean isExtracted = false;
        UnZip unZip = new UnZip();
        try {
            unZip.extract(zipFile, toDir);
            isExtracted = true;
        } catch (IOException e) {
            log.warn("Failed to extract file {} to dir {}", zipFile.toString(), toDir.toString());
        }
        return isExtracted;
    }

    public boolean backupJau() {
        boolean backupOk = false;
        JauBackup jauBackup;
        try {
            jauBackup = new JauBackup(jauDir,backupDir);
            backupOk = jauBackup.performBackup();
            backupOk = true;
        } catch (SecurityException se) {
            log.warn("Could not create backup directory: {}", backupDir.toString());
            backupOk = false;
        }
        return backupOk;
    }

    public boolean uninstallJau() {
        JauServiceCommander serviceCommander = new JauServiceCommander(JAU_SERVICE_NAME);
        boolean serviceStoped = serviceCommander.stopService();

        return false;
    }

    public boolean updateConfig() {
        return false;
    }

    public boolean installJau() {
        return false;
    }

    public boolean startJau() {
        return false;
    }

    public boolean verifyUpgrade() {
        return false;
    }
}
