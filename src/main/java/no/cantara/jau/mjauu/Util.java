package no.cantara.jau.mjauu;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.toList;

public class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);

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

    private static void copyFolder(Path source, Path destination) {
        List<Path> sources = null;
        try {
            sources = Files.walk(source).collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Path> destinations = sources.stream()
                .map(source::relativize)
                .map(destination::resolve)
                .collect(toList());

        for (int i = 0; i < sources.size(); i++) {
            try {
                if (!destinations.get(i).toFile().isDirectory()) {
                    Files.copy(sources.get(i), destinations.get(i), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Properties readProperties(File configFile) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configFile));
        return properties;
    }

    public static boolean verifyChecksum(Path artifact, String jauArtifactChecksum) {
        try {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            String actualChecksum = getFileChecksum(md5Digest, artifact.toFile());
            if (actualChecksum.equals(jauArtifactChecksum)) {
                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 is not an algorithm?");
        } catch (IOException e) {
            log.error("Could not read file {}", artifact.toString());
        }
        return false;
    }

    /*
     * http://howtodoinjava.com/core-java/io/how-to-generate-sha-or-md5-file-checksum-hash-in-java/
     */
    public static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    public static JauProperties getAndVerifyProperties(File configFile) throws IOException {
        Properties properties = Util.readProperties(configFile);

        String jauArtifact = properties.getProperty("jauArtifactURL");
        String jauArtifactChecksum = properties.getProperty("jauArtifactChecksum");
        String newJAULocation = properties.getProperty("newJAULocation");
        String jauFolderName = properties.getProperty("jauFolderName");
        String filesToCopy = properties.getProperty("filesToCopy");

        if (jauArtifact == null
                || jauArtifactChecksum == null
                || newJAULocation == null
                || jauFolderName == null
                || filesToCopy == null) {
            log.error("Invalid config file. Fall back to a default?");
            throw new IOException("Invalid config file. Does not contain valid values");
        }

        return new JauProperties(jauArtifact, jauArtifactChecksum, newJAULocation, jauFolderName, filesToCopy);
    }

}
