package no.cantara.jau.mjauu;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

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

    public static File findLogFile(){
        File logFile = null;
        FileAppender<?> fileAppender = null;
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()){
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
                Object enumElement = index.next();
                if (enumElement instanceof FileAppender) {
                    fileAppender=(FileAppender<?>)enumElement;
                }
            }
        }

        if (fileAppender != null) {
            logFile=new File(fileAppender.getFile());
        }
        return logFile;
    }


    public static List<String> findByRegEx(String customIdFile, String customIdRegex) {
        log.trace("Try findByRegEx. customIdFile {}, customIdRegex {}", customIdFile,customIdRegex);
        List<String> matches = new ArrayList<>();
        if (hasValue(customIdFile) && hasValue(customIdRegex)) {

            Scanner s = null;
            try {
                File fileWithId = new File(customIdFile);
                if (fileWithId != null && fileWithId.exists()) {
                    s = new Scanner(fileWithId);
                    String nextMatch = s.findWithinHorizon(customIdRegex, 0);
                    matches.add(nextMatch);
                }
            } catch (FileNotFoundException e) {
                log.info("Could not fine file with name {}", customIdFile);
            }
        }

        return matches;
    }

    private static boolean hasValue(String customIdFile) {
        return customIdFile != null && !customIdFile.isEmpty();
    }
}
