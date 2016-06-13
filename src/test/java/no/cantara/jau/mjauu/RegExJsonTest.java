package no.cantara.jau.mjauu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Created by baardl on 13/06/2016.
 */
public class RegExJsonTest {
    private static final Logger log = LoggerFactory.getLogger(RegExJsonTest.class);

    @Test
    public void findAgentIdFromJson() throws IOException, URISyntaxException {
        String regex = "\\\"agentId\\\"\\:\\\"[0-9]*\\\"";
        String validJson = ""; //TODO validate as json
        String exampleLog = "06:17:39.325 [pool-3-thread-1] INFO  n.n.p.s.File - some data data as json={\"agentId\":\"8011\",";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(exampleLog);
        assertTrue(m.find());
        assertEquals(m.group(),"\"agentId\":\"8011\"");
    }
}
