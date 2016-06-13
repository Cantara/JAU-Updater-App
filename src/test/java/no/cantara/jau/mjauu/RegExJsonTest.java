package no.cantara.jau.mjauu;

import org.json.JSONObject;
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

    private final String exampleLog = "06:17:39.325 [pool-3-thread-1] INFO  n.n.p.s.File - some data data as json={\"agentId\":\"8011\",";

    @Test
    public void findAgentIdFromRegex() throws IOException, URISyntaxException {
        String regex = "\\\"agentId\\\"\\:\\\"[0-9]*\\\"";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(exampleLog);
        assertTrue(m.find());
        assertEquals(m.group(),"\"agentId\":\"8011\"");
    }

    @Test
    public void findAgentIdFromJsonRegex() throws Exception{
        String validJson = "{\n" +
                "\t\"tagName\": \"pharmacyId\",\n" +
                "\t\"regex\": \"\\\\\\\"agentId\\\\\\\"\\\\:\\\\\\\"[0-9]*\\\\\\\"\",\n" +
                "\t\"filePath\": \"logs/pharmacyAgent.log\"\n" +
                "}";
        JSONObject eventExtractionConfig = new JSONObject(validJson);
        String regex = eventExtractionConfig.getString("regex");
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(exampleLog);
        assertTrue(m.find());
        assertEquals(m.group(),"\"agentId\":\"8011\"");

    }
}
