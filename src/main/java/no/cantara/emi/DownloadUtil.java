package no.cantara.emi;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by andeero on 14/04/16.
 */
public class DownloadUtil {
    private static final Logger log = LoggerFactory.getLogger(DownloadUtil.class);

    /**
     * http://www.codejava.net/java-se/networking/use-httpurlconnection-to-download-file-from-an-http-url
     * Downloads a file from a URL
     * @param sourceUrl HTTP URL of the file to be downloaded
     * @param filenameOverride  filename to store the downloaded file as
     * @param username  username  to authenticate against the server
     * @param password  password  to authenticate against the server
     * @param targetDirectory path of the directory to save the file
     * @return  Path to the downloaded file
     */
    public static Path downloadFile(String sourceUrl, String filenameOverride, String username, String password, String targetDirectory) {
        final int BUFFER_SIZE = 4096;
        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection httpConn;
        try {
            httpConn = (HttpURLConnection) url.openConnection();
            if (username != null && password != null) {
                String authorizationValue = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
                httpConn.setRequestProperty("Authorization", authorizationValue);
            }
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.info("Could not download. Server replied with {} {}", responseCode, httpConn.getResponseMessage());
                httpConn.disconnect();
                return null;
            }

            String fileName = filenameOverride;
            if (fileName == null || fileName.isEmpty()) {
                fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");
                log.debug("Content-Disposition = " + disposition);
                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    fileName = sourceUrl.substring(sourceUrl.lastIndexOf("/") + 1, sourceUrl.length());
                }

            }


            log.debug("Content-Type = " + httpConn.getContentType());
            log.debug("Content-Length = " + httpConn.getContentLength());
            log.debug("fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            File targetDirectoryAsFile = new File(targetDirectory);
            if (!targetDirectoryAsFile.exists()) {
                targetDirectoryAsFile.mkdirs();
            }

            // opens an output stream to save into file
            String targetPath = targetDirectory + File.separator + fileName;
            FileOutputStream outputStream = new FileOutputStream(targetPath);
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            log.info("File downloaded to {}", targetPath);
            httpConn.disconnect();
            return new File(targetPath).toPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unZipIt(String zipFile, String outputFolder){
        try {
            ZipFile zip = new ZipFile(zipFile);
            zip.extractAll(outputFolder);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }


}
