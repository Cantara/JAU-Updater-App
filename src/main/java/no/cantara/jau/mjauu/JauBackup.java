package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by baardl on 06/06/2016.
 */
public class JauBackup {
    private static final Logger log = LoggerFactory.getLogger(JauBackup.class);
    private final File backupDir;
    private final File jauDir;

    public JauBackup(File jauDir, File backupDir) {
        this.backupDir = backupDir;
        this.jauDir = jauDir;
        if (!backupDir.exists()) {
                backupDir.mkdir();
        }
    }

    public boolean performBackup() {
        if (!backupDir.canRead()) {
            throw new SecurityException("Not able to write to " + backupDir.toString());
        }
        boolean backupOk = false;

        backup("bin");
        backup("config_override");
        backup("etc");
        backup("java");//Java may be upgraded in later releases of MJAUU.
        backup("lib");
        backup("applicationState.properties");
        backup("config.properties");
        backup("download-java");
        backup("jau.log.properties");
        backup("install.bat");
        backup("unzip.exe");
        backup("wget.exe");
        backup("last-running-process.txt");

        return backupOk;
    }

    private void backup(String fileOrDir) {
        File copyDir = new File(fileOrDir);
        copy(copyDir,new File(getBackupDir() + File.separator + copyDir.toString()));
    }

    private File getBackupDir() {
        return backupDir;
    }

    public boolean copy(File sourceLocation, File targetLocation)  {
        boolean copyOk = false;
        try {
            log.info("Copy {}, to {}", sourceLocation, targetLocation);
            if (sourceLocation.isDirectory()) {
                copyDirectory(sourceLocation, targetLocation);
            } else {
                copyFile(sourceLocation, targetLocation);
            }
            copyOk = true;
        }catch (IOException e) {
            log.warn("Failed to copy {} to {}",sourceLocation,targetLocation);
        }
        return copyOk;

    }

    private void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdir();
        }

        for (String f : source.list()) {
            copy(new File(source, f), new File(target, f));
        }
    }

    private void copyFile(File source, File target) throws IOException {
        if (source.exists()) {
            try (
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target)
            ) {
                byte[] buf = new byte[1024];
                int length;
                while ((length = in.read(buf)) > 0) {
                    out.write(buf, 0, length);
                }
            }
        } else {
            log.trace("File does not exist {}", source);
        }
    }

    public boolean removeFilesAndDirectories() {
        boolean allRemoved = false;
        if (!backupDir.canRead()) {
            throw new SecurityException("Not able to write to " + backupDir.toString());
        }

        try {
            delete("bin");
            delete("etc");
            delete("lib");
            delete("install.bat");
            allRemoved = true;
        } catch (Exception e) {
            log.warn("Failed to remove file and directories. Reason {}", e.getMessage());
        }

        return allRemoved;
    }

    private boolean delete(String fileOrDir) {
        boolean isDeleted = false;
        File toBeDeleted = new File(fileOrDir);
        if (toBeDeleted.exists()) {
            if (toBeDeleted.isDirectory()) {
                for (File file : toBeDeleted.listFiles()) {
                    file.delete();
                }
            }
            isDeleted = toBeDeleted.delete();

            if (isDeleted) {
                log.info("Deleted file or directory {}. ", fileOrDir);
            } else {
                log.warn("Failed to delete file or directory {}", fileOrDir);
            }
        } else {
            log.info("File or dir {} does not exist", fileOrDir);
            isDeleted = true;
        }
        return isDeleted;
    }
}
