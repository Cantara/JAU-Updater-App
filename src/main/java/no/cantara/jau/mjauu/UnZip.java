package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by baardl on 27/05/2016.
 */
public class UnZip {
    private static final Logger log = LoggerFactory.getLogger(UnZip.class);

    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath name and dir files to be ziped
     * @param destDirectory where to save
     * @throws IOException File system errors
     */
    public void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn zipInputStream
     * @param filePath extract to
     * @throws IOException file system errors
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    void extract(File zipFile, File toDir) throws IOException {
        String outputFolder = toDir.getPath();
        log.trace("Begin unzip "+ zipFile + " into "+outputFolder);
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry ze = zis.getNextEntry();
        while(ze!=null){
            String entryName = ze.getName();
            log.trace("Extracting " + entryName + " -> " + outputFolder + File.separator +  entryName + "...");
            File f = new File(outputFolder + File.separator +  entryName);
            //create all folder needed to store in correct relative path.
            f.getParentFile().mkdirs();
            if (ze.isDirectory()){
                f.mkdir();
            } else {
                FileOutputStream fos = new FileOutputStream(f);
                int len;
                byte buffer[] = new byte[1024];
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            ze = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();

        log.trace( "OK! - {}  unzipped successfully", zipFile);
    }
}
