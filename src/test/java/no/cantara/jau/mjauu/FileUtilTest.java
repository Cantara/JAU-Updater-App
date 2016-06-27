package no.cantara.jau.mjauu;

import no.cantara.jau.mjauu.util.FileUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
    @Test
    public void testFindByRegExEmptyValues() throws Exception {
        String regex = null;
        String filePath = null;
        List<String> matches = FileUtil.findByRegEx(filePath, regex);
        assertNotNull(matches);
        regex = "";
        filePath = "";
        matches = FileUtil.findByRegEx(filePath, regex);
        assertNotNull(matches);

    }
}