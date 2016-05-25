package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        String[] command = {"cmd.exe", "/c", "net", "stop", "java-auto-update"};
        command = new String[]{"bin/java-auto-update.bat", "stop"};
        PrintWriter writer = null;

        try {
            writer = new PrintWriter("hei2.txt", "UTF-8");
            log.info("Run command: " + buildString(command));
            writer.println("Run command: " + buildString(command));
            Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
            log.info(line);
            writer.println(line);
        }
        log.info("Done.");
        writer.println("Done.");
        } catch(Exception ex) {
            log.warn("Exception : "+ex);
            writer.print("Exception: " + ex.toString());
        } finally {
            writer.close();
        }
    }

    private static String buildString(String[] command) {
        StringBuilder builder = new StringBuilder();
        for(String s : command) {
            builder.append(" " +s);
        }
        return builder.toString();
    }

}