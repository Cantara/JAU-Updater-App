package no.cantara.jau.mjauu;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;

import static org.testng.Assert.assertTrue;

/**
 * Created by baardl on 27/05/2016.
 */
public class UnZipTest {

    UnZip unZip;
    @BeforeMethod
    public void setUp() throws Exception {
        unZip = new UnZip();

    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test
    public void testUnzip() throws Exception {
        URL zipSource = this.getClass().getClassLoader().getResource("jau-updater-test.zip");
        URI zipUri = new URI(zipSource.toString());
        File zipFile = new File(zipUri);//new File("C:\\sources\\JAU-Updater-App\\target\\test-classes\\jau-updater-test.zip"); //new File(zipSource.toString());
        assertTrue(zipFile.exists(),"Test zip not found " + zipSource.toString());
        String toDirName = "C:\\sources\\JAU-Updater-App\\target\\test-classes\\tmp"; //zipFile.getPath() +"/../tmp"
        File toDir = new File(zipFile.getParentFile().toString() + File.separator + "tmp");
        unZip.extract(zipFile, toDir);
        String expectToFind = Main.class.getCanonicalName();
        expectToFind = expectToFind.replace(".", File.separator) + ".java";

        //URI expectedUri = new URI(toDir.toString()+ File.separator + "main" + File.separator + "java" + File.separator + expectToFind);
        //assertNotNull(expectedUri);
        File expectedFile = new File(toDir.toString()+ File.separator + "main" + File.separator + "java" + File.separator + expectToFind);
        assertTrue(expectedFile.exists());



    }

}