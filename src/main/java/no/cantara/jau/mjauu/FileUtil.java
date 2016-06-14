package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created by baardl on 14/06/2016.
 */
public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);


    public static Properties loadPropreties(File propertiesFile) {
        Properties properties = new Properties();
        try {


            InputStream is = new FileInputStream(propertiesFile);

            properties.load(is);
        } catch (FileNotFoundException e) {
            log.warn("Failed to load needed file {}. Reason {}", propertiesFile,e.getMessage());
            properties = null;
        } catch (IOException e) {
            log.warn("Failed to read a file {}. Reason {}", propertiesFile,e.getMessage());
            properties = null;
        }
        return properties;
    }

    public static boolean saveProperties(Properties jauProperties, File jauPropertiesFile) {
        boolean savedProperties = false;
        OutputStream out = null;
        try {
            out = new FileOutputStream( jauPropertiesFile );
            jauProperties.store(out, "This is an optional header comment string");
            savedProperties = true;
        } catch (FileNotFoundException e) {
            log.warn("File not found {}, can not update properties {}. Reason {}", jauPropertiesFile,jauProperties, e.getMessage());
        } catch (IOException e) {
            log.warn("Failed to write properties {} to file {}. Reason {}", jauProperties, jauPropertiesFile,e.getMessage());
        }

        return savedProperties;
    }


}
