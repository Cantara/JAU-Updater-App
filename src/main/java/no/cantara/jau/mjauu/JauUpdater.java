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
    static final String JAU_SERVICE_NAME = "java-auto-update";

    //URL url = getClass().getClassLoader().getResource("validconfig.properties");
    //JauProperties props = Util.getAndVerifyProperties(new File(url.toURI()));
    private final File zipFile;
    private final File toDir;
    private final File backupDir;
    private final File jauDir;
    private final JauServiceCommander serviceCommander;
    JauBackup jauBackup = null;


    public JauUpdater(File zipFile, File toDir) {
        this.zipFile = zipFile;
        this.toDir = toDir;
        if (!zipFile.exists()){
            throw new IllegalArgumentException("ZipFile " + zipFile.toString() + " does not exist.");
        }
        backupDir = new File("backup");
        jauDir = new File(".");
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
        boolean uninstalledOk = false;
        boolean serviceRemoved = serviceCommander.uninstallService();
        log.info("uninstallJau: serviceRemoved {}", serviceRemoved);
        if (serviceRemoved) {
            if (jauBackup != null) {
                uninstalledOk = jauBackup.removeFilesAndDirectories();
                log.info("uninstallJau: uninstalled {}", uninstalledOk);
            }
        }
        return uninstalledOk;
    }

    public boolean updateConfig(String artifactId, String version) {
        boolean copyZipContent = false;
        File newJau = new File(toDir + File.separator + "java-auto-update" +"-" +version);
        log.info("Copy files from backup {} to JAU {}", newJau,jauDir);
        if (jauDir.exists() ) {
            if ( newJau.exists()) {

                try {
                    jauBackup.copy(newJau, jauDir);
                    copyZipContent = true;
                } catch (Exception e) {
                    log.warn("Failed to copy files from {} to {}", newJau, jauDir);
                }
            } else {
                log.warn("Missing newJau directory [{}]", newJau);
            }
        } else {
            log.warn("Missing JAU directory [{}].", jauDir);
        }

        boolean newConfigWritten = false;
        if (copyZipContent){
            //FIXME copy correct content;
        }
        return newConfigWritten;
    }

    public boolean installJau() {
        boolean isInstalled = false;
        isInstalled = serviceCommander.installService();

        return isInstalled;
    }

    public boolean startJau() {
        boolean isStarted = false;
        isStarted = serviceCommander.startService();
        return isStarted;
    }

    public boolean verifyUpgrade() {
        return false;
    }
}
