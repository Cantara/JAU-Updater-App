package no.cantara.emi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        String[] command = {"cmd.exe", "/c", "net", "stop", "java-auto-update"};
        command = new String[]{"bin/java-auto-update.bat", "stop"};
        try {
            System.out.println("Run command: " + command);
            Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("Done.");
        } catch(Exception ex) {
            System.out.println("Exception : "+ex);
        }
    }

}