package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
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
        String[] command = new String[]{"cmd", "/c","net", "stop", serviceId};
        boolean serviceStoppedOk = false;
        try {
            log.info("Run command: {}" , buildString(command));
            Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info("StopService {}",line);
                boolean isVerified = verifyServiceIsStopped(line);
                if (isVerified){
                    serviceStoppedOk = true;
                }
            }
            log.info("Finshed waiting for Service stoping. Stopped {}", serviceStoppedOk);

        } catch(Exception ex) {
            log.warn("Exception : "+ex);
        }
        return serviceStoppedOk;
    }

    private boolean verifyServiceIsStopped(String line) {
        boolean stoppedOk = false;
        if (line.contains("stoppet")){
            stoppedOk = true;
        } else if (line.contains("stopped")) {
            stoppedOk = true;
        } else if(line.contains("The specified service does not exist as an installed service")){
            log.info("{} is not installed.", serviceId);
            stoppedOk = true;
        } else if(line.contains("STATE") && line.contains("STOPPED")){
            log.info("{} is stopped.", serviceId);
            stoppedOk = true;
        }
        return stoppedOk;
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
            String osOutput = "";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info("RemoveServiece {}",line);
                osOutput += line + "\n";
                //printStatus(line);
                boolean isVerified = verifyServiceIsUninstalled(line);
                if (isVerified){
                    isUninstalledOk = true;
                }
            }
            if (isUninstalledOk) {
                log.info("{} Service is unistalled ok. Output from the OS \n\t{}", serviceId,osOutput);
            } else {
                log.error("Failed to stop the {} Service. Output from the OS \n\t{}", serviceId, osOutput);
            }
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
            } else if (line.contains("stopped")) {
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
        command = new String[]{"cmd", "/c","bin" + File.separator + "java-auto-update", "install"};
        try {
            log.info("Run command: {}" , buildString(command));
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            //Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            log.debug("process started.");
            while ((line = bufferedReader.readLine()) != null) {
                log.info("InstallService {}",line);
                boolean isVerified = verifyServiceIsIsInstalled(line);
                if (isVerified){
                    isInstaled = true;
                }
            }
            log.info("Finshed waiting for Service installation. Installed {}", isInstaled);

        } catch(Exception ex) {
            log.warn("Exception : {} ",ex);
        }
    return isInstaled;
    }

    private boolean verifyServiceIsIsInstalled(String line) {
        boolean installedOk = false;
        if (line.contains("java-auto-update installed")){
            installedOk = true;
        } else if (line.contains("java-auto-update installert")){
            installedOk = true;
        } else if (line.contains(serviceId) && line.contains("installed")) {
            installedOk = true;
        } else if (line.contains(serviceId) && line.contains("installert")) {
            installedOk = true;
        }

        return installedOk;
    }

    public boolean startService() {
        /*
        sc start java-auto-update

SERVICE_NAME: java-auto-update
        TYPE               : 10  WIN32_OWN_PROCESS
        STATE              : 2  START_PENDING
                                (NOT_STOPPABLE, NOT_PAUSABLE, IGNORES_SHUTDOWN)
        WIN32_EXIT_CODE    : 0  (0x0)
        SERVICE_EXIT_CODE  : 0  (0x0)
        CHECKPOINT         : 0x0
        WAIT_HINT          : 0x7d0
        PID                : 9548
        FLAGS              :
         */
        /*
        net start java-auto-update
The java-auto-update service is starting..
The java-auto-update service was started successfully.
         */
        boolean isStarted = false;
        String[] command = {"cmd.exe", "/c", "net", "start", "java-auto-update"};
       // command = new String[]{"cmd", "/c","bin/java-auto-update", "install"};
        try {
            log.info("Run command: {}" , buildString(command));
            Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info("StartService {}",line);
                boolean isVerified = verifyServiceIsIsStarted(line);
                if (isVerified){
                    isStarted = true;
                }
            }
            log.info("Finshed waiting for Service startup. Started {}", isStarted);

        } catch(Exception ex) {
            log.warn("Exception : "+ex);
        }
        return isStarted;
    }

    private boolean verifyServiceIsIsStarted(String line) {
        boolean startedOk = false;
        if (line.contains("java-auto-update service was started successfully")){
            startedOk = true;
        } else if (line.contains("java-auto-update") && line.contains("started")){
            startedOk = true;
        } else if (line.contains(serviceId) && line.contains("startet")) {
            startedOk = true;
        } else if (line.contains(serviceId) && line.contains("started")) {
            startedOk = true;
        }

        return startedOk;
    }

    public boolean serviceIsRunning() {
        boolean isRunning = false;
        String[] command = {"cmd.exe", "/c", "sc", "query", "java-auto-update"};
        // command = new String[]{"cmd", "/c","bin/java-auto-update", "install"};
        try {
            log.info("Run command: {}" , buildString(command));
            Process process = new ProcessBuilder(command).start();
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info("Query Service {}",line);
                boolean isVerified = verifyServiceIsIsRuning(line);
                if (isVerified){
                    isRunning = true;
                }
            }
            log.info("Finshed waiting for Service query. Running {}", isRunning);

        } catch(Exception ex) {
            log.warn("Exception : "+ex);
        }
        return isRunning;
    }

    private boolean verifyServiceIsIsRuning(String line) {
        boolean runningOk = false;
        if (line.contains("java-auto-update service was started successfully")){
            runningOk = true;
        } else if (line.contains("STATE") && line.contains("RUNNING")){
            runningOk = true;
        } else if (line.contains(serviceId) && line.contains("startet")) {
            runningOk = true;
        } else if (line.contains(serviceId) && line.contains("started")) {
            runningOk = true;
        }

        return runningOk;
    }
}
