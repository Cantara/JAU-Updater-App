package no.cantara.emi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class UpdaterTest {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    @Test
    public void shouldUpdateJau() throws IOException, URISyntaxException {
        URL url = getClass().getClassLoader().getResource("validconfig.properties");
        JauProperties props = Util.getAndVerifyProperties(new File(url.toURI()));
        Updater updater = new Updater(props);
        updater.updateJAU();
    }
}