package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by baardl on 06/06/2016.
 */
public class JauServiceCommander  {
    private static final Logger log = LoggerFactory.getLogger(JauServiceCommander.class);

    private final String serviceId;

    public JauServiceCommander() {
        serviceId = "java-auto-update";
    }

    public JauServiceCommander(String serviceId) {
        this.serviceId = serviceId;
    }


    protected boolean stopService() {
        String[] command = {"cmd.exe", "/c", "net", "stop", "java-auto-update"};
        command = new String[]{"cmd", "/c","net", "stop", serviceId};
        //FIXME verify or stop
        // command = new String[]{"sc", "query", serviceId};
        boolean serviceStopedOk = false;
        try {
            log.info("Run command: {}" , buildString(command));
            Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info("StopServiece {}",line);
                //printStatus(line);
                boolean isVerified = verifyServiceIsStopped(line);
                if (isVerified){
                    serviceStopedOk = true;
                }
            }
            log.info("Finshed waiting for Service stoping. Stoped {}", serviceStopedOk);

            //printStatus("Done.");
        } catch(Exception ex) {
            log.warn("Exception : "+ex);
           // writer.print("Exception: " + ex.toString());
        }
        return serviceStopedOk;
    }

    private boolean verifyServiceIsStopped(String line) {
        boolean stopedOk = false;
        if (line.contains("stoppet")){
            stopedOk = true;
        } else if (line.contains("stoped")) {
            stopedOk = true;
        } else if(line.contains("The specified service does not exist as an installed service")){
            log.info("{} is not installed.", serviceId);
            stopedOk = true;
        } else if(line.contains("STATE") && line.contains("STOPPED")){
            log.info("{} is stoped.", serviceId);
            stopedOk = true;
        }
        return stopedOk;
    }

    protected  boolean uninstallService(){
        boolean isUninstalledOk = false;
        String[] command = {"cmd.exe", "/c", "net", "stop", "java-auto-update"};
        //command = new String[]{"cmd", "/c","net", "stop", serviceId};
        command = new String[]{"sc", "delete", serviceId};
        try {
            log.info("Run command: {}" , buildString(command));
            Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info("RemoveServiece {}",line);
                //printStatus(line);
                boolean isVerified = verifyServiceIsUninstalled(line);
                if (isVerified){
                    isUninstalledOk = true;
                }
            }
            log.info("Finshed waiting for Service removal. Removed {}", isUninstalledOk);

            //printStatus("Done.");
        } catch(Exception ex) {
            log.warn("Exception : "+ex);
            // writer.print("Exception: " + ex.toString());
        }

        return isUninstalledOk;
    }

    private boolean verifyServiceIsUninstalled(String line) {
            boolean removedOk = false;
            if (line.contains("stoppet")){
                removedOk = true;
            } else if (line.contains("stoped")) {
                removedOk = true;
            } else if(line.contains("The specified service does not exist as an installed service")){
                log.info("{} is not installed.", serviceId);
                removedOk = true;
            } else if(line.contains("DeleteService") && line.contains("SUCCESS")){
                log.info("{} is removed.", serviceId);
                removedOk = true;
            }
            return removedOk;
    }

    /*
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
    */

    private static String buildString(String[] command) {
        StringBuilder builder = new StringBuilder();
        for(String s : command) {
            builder.append(" " +s);
        }
        return builder.toString();
    }

    public boolean installService() {
        boolean isInstaled = false;
        String[] command = {"cmd.exe", "/c", "net", "stop", "java-auto-update"};
        command = new String[]{"cmd", "/c","bin/java-auto-update", "install"};
       // command = new String[]{"sc", "delete", serviceId};
        try {
            log.info("Run command: {}" , buildString(command));
            Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info("RemoveServiece {}",line);
                //printStatus(line);
                boolean isVerified = verifyServiceIsIsInstalled(line);
                if (isVerified){
                    isInstaled = true;
                }
            }
            log.info("Finshed waiting for Service removal. Removed {}", isInstaled);

            //printStatus("Done.");
        } catch(Exception ex) {
            log.warn("Exception : "+ex);
            // writer.print("Exception: " + ex.toString());
        }
    return isInstaled;
    }

    private boolean verifyServiceIsIsInstalled(String line) {
        return false;
    }

    public boolean startService() {
        //TODO sc start service-id
        return false;
    }
}
