package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by baardl on 27/05/2016.
 */
public class JauUpdater {
    private static final Logger log = LoggerFactory.getLogger(JauUpdater.class);

    //URL url = getClass().getClassLoader().getResource("validconfig.properties");
    //JauProperties props = Util.getAndVerifyProperties(new File(url.toURI()));
    private final Properties properties;

    public JauUpdater(Properties properties) {
        this.properties = properties;
        extractJauZip("java-auto-update-0.8-beta-5-SNAPSHOT.zip", "tmp");
    }

    private void extractJauZip(String fileName, String toDirectory) {

    }
}
