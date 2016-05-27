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

    //URL url = getClass().getClassLoader().getResource("validconfig.properties");
    //JauProperties props = Util.getAndVerifyProperties(new File(url.toURI()));
    private final File zipFile;
    private final File toDir;


    public JauUpdater(File zipFile, File toDir) {
        this.zipFile = zipFile;
        this.toDir = toDir;
        if (!zipFile.exists()){
            throw new IllegalArgumentException("ZipFile " + zipFile.toString() + " does not exist.");
        }
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
}
