package no.cantara.jau.mjauu;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by baardl on 2016-06-22.
 */
public class FileUtilTest {

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @Test
    public void testFindByRegEx() throws Exception {
        String regex = "\\\"agentId\\\"\\:\\\"[0-9]*\\\"";
        Path filePath = Paths.get(ClassLoader.getSystemResource("regex-test.log").toURI());
        List<String> matches = FileUtil.findByRegEx(filePath.toString(), regex);
        assertNotNull(matches);
        assertTrue(matches.size() >0);
        assertEquals(matches.get(0), "\"agentId\":\"8011\"");

    }
}