package no.cantara.jau.mjauu;

import no.cantara.jau.mjauu.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by baardl on 13/06/2016.
 */
public class ManualEventTest {
    private static final Logger log = LoggerFactory.getLogger(ManualEventTest.class);


    private static Main main;
    public static void main(String[] args) {
        try {
            main = new Main();
            //boolean clientIsRegistered = main.checkIfClientExistOnCS();
            main.updateStatus(State.Started);
        } catch (IOException e) {
            log.error("Unexpected ", e);
        }
    }

}