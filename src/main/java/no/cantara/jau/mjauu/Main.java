package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Instant;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    PrintWriter writer = null;
    FileWriter fw = null;
    BufferedWriter bw = null;

    public static void main(String[] args) {
        Main main = new Main();
        main.printStatus("Start");
        //main.stopService("java-auto-update");
        do {
            main.printStatus("Continue:");
            main.stopService("java-auto-update");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
    }

    private void stopService(String serviceId) {
        String[] command = {"cmd.exe", "/c", "net", "stop", "java-auto-update"};
        command = new String[]{"cmd", "/c","net", "stop", serviceId};

        try {
            printStatus("Run command: " + buildString(command));
            Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info(line);
                printStatus(line);
            }
            log.info("Done.");
            printStatus("Done.");
        } catch(Exception ex) {
            log.warn("Exception : "+ex);
            writer.print("Exception: " + ex.toString());
        }
    }

    void printStatus(String status){


        try {
            fw = new FileWriter("hei2.txt", true);
            bw = new BufferedWriter(fw);
            writer = new PrintWriter(bw); //new PrintWriter("hei2.txt", "UTF-8");

            writer.println(status + Instant.now());
        } catch(Exception ex) {
            log.warn("Exception : "+ex);
            writer.print("Exception: " + ex.toString());
        } finally {
            writer.close();
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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