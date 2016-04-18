package no.cantara.emi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class App {
    private final static String JAU = "http://mvnrepo.cantara.no/content/repositories/releases/no/cantara/jau/java-auto-update/0.4.1/java-auto-update-0.4.1.zip";
    private final static String unzipFolder = "../new-java-autoupdate/";
    private final static String zipName = "jau.zip";
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Path zip = DownloadUtil.downloadFile(JAU, zipName, null, null, unzipFolder);
        DownloadUtil.unZipIt(unzipFolder + zipName, unzipFolder);
        copyConfig();
    }

}