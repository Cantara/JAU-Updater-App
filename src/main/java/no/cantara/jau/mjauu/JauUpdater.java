package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static no.cantara.jau.mjauu.FileUtil.loadPropreties;
import static no.cantara.jau.mjauu.FileUtil.saveProperties;

/**
 * Created by baardl on 27/05/2016.
 */
public class JauUpdater {
    private static final Logger log = LoggerFactory.getLogger(JauUpdater.class);
    static final String JAU_SERVICE_NAME = "java-auto-update";
    static final String JAU_ARTIFACT_ID = "java-auto-update";
    private static final String JAU_PROPERTIES_FILE = "jau.properties";
    private static final String MJAUU_OVERRIDES_PROPERTIES_FILE = "mjauu-override.properties";

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
        boolean configUpdated = false;
        File newJau = new File(toDir + File.separator + JAU_ARTIFACT_ID +"-" +version);
        log.info("Restore files from backup {} to JAU {}", newJau,jauDir);
        boolean restoredFromBackup = restoreFromBackup(newJau);

        boolean newConfigWritten = false;
        if (restoredFromBackup){
            //FIXME copy correct content;
            newConfigWritten = replacePropertiesFromOverride();
        }
        return newConfigWritten;
    }

    protected boolean restoreFromBackup(File newJau) {
        boolean restoredFromBackup = false;
        if (jauDir.exists() ) {
            if ( newJau.exists()) {

                try {
                   restoredFromBackup =  jauBackup.copy(newJau, jauDir);
                    if (restoredFromBackup){
                        log.info("Restored content from {} to {}", newJau,jauDir);
                    } else {
                        log.warn("Failed to restore content from {} to {}", newJau, jauDir);
                    }

                } catch (Exception e) {
                    log.warn("Failed to copy files from {} to {}", newJau, jauDir);
                }
            } else {
                log.warn("Missing newJau directory [{}]", newJau);
            }
        } else {
            log.warn("Missing JAU directory [{}].", jauDir);
        }
        return restoredFromBackup;
    }

    protected boolean replacePropertiesFromOverride() {
        boolean propertiesUpdated = false;

        File jauPropertiesFile = new File("config_override" + File.separator + JAU_PROPERTIES_FILE);
        File mjauuOverridesFile = new File(MJAUU_OVERRIDES_PROPERTIES_FILE);
        log.info("Replace propterties from {} to {}", mjauuOverridesFile, jauPropertiesFile);
        if (mjauuOverridesFile.exists()) {
            Properties mjauuOverride = loadPropreties(mjauuOverridesFile);
            if (mjauuOverride != null) {
                Properties jauProperties = loadPropreties(jauPropertiesFile);
                if (jauProperties == null) {
                    jauProperties = new Properties();
                }
                jauProperties.putAll(mjauuOverride);
                propertiesUpdated = saveProperties(jauProperties, jauPropertiesFile);
            }
        }

        return propertiesUpdated;

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
