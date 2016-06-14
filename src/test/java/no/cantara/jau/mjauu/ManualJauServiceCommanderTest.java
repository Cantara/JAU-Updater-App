package no.cantara.jau.mjauu;

import static org.testng.Assert.assertTrue;

/**
 * Created by baardl on 14/06/2016.
 */
public class ManualJauServiceCommanderTest {

    private JauServiceCommander serviceCommander;

    public ManualJauServiceCommanderTest(String serviceName) {
        this.serviceCommander = new JauServiceCommander(serviceName);
    }

    public static void main(String[] args) {
        ManualJauServiceCommanderTest test  = new ManualJauServiceCommanderTest(JauUpdater.JAU_SERVICE_NAME);
        try {
            test.testUninstallService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testUninstallService() throws Exception {
        boolean isRemoved = serviceCommander.uninstallService();
        assertTrue(isRemoved);

    }

}