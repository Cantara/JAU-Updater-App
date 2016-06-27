package no.cantara.jau.mjauu.deprecated;

import no.cantara.jau.mjauu.JauProperties;
import no.cantara.jau.mjauu.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;


public class UpdaterTest {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Test(enabled = false)
    public void shouldUpdateJau() throws IOException, URISyntaxException {
        URL url = getClass().getClassLoader().getResource("validconfig.properties");
        JauProperties props = Util.getAndVerifyProperties(new File(url.toURI()));
        Updater updater = new Updater(props);
        updater.updateJAU();
    }
}