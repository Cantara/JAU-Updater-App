package no.cantara.emi;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by andeero on 16/04/16.
 */
public class UpdaterTest {

    @Test
    public void shouldCopyConfig() {
        Path testFolder = Paths
        Path source = Paths.get("config_override");
        Path dest = Paths.get("../new-jau/java-auto-update-0.4.1/config_override");

        Updater updater = new Updater();
    }
}