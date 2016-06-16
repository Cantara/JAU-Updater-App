package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 1. Gets properties from CS
 * -- Which file (JAU) to download
 * -- Checksum of JAU file (or just assume mvn-repo with checksum at filename-md5?)
 * -- Where to copy new JAU
 * -- Which files/folders to copy to new JAU
 * -- Which install file to exec (in this case .bat file and Windows)
 * 2. Download new JAU
 * 3. Checksum to verify correct.
 * -- Else download again with exp backoff?
 * 4. Unpack JAU to new dir - check that this went well?
 * -- If install.bat script runs fine, it's likely unpacked well?
 * 5. Copy files/folders to new dir
 * 6. Run install.bat
 */
public class Updater {
    private static final Logger log = LoggerFactory.getLogger(Updater.class);
    private final JauProperties properties;

    public Updater(JauProperties properties) throws IOException {
        this.properties = properties;
    }

    public void updateJAU() {
        Path artifact = Util.downloadFile(properties.JAU_ARTIFACT_URL, null, null, null, "./");
        Util.verifyChecksum(artifact, properties.JAU_ARTIFACT_CHECKSUM);
        Util.unZipIt(artifact.toString(), properties.NEW_JAU_LOCATION);
    }
}
