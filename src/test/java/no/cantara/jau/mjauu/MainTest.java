package no.cantara.jau.mjauu;

import org.testng.annotations.Test;

/**
 * Created by baardl on 17/06/2016.
 */
public class MainTest {
    @Test
    public void testNotifyFailure() throws Exception {
        Main main = new Main();
        main.notifyFailure("Hei", null, null);

    }

}