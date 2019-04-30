package edu.oswego.collisiongang.anki;

import de.adesso.anki.*;
import de.adesso.anki.messages.*;

import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    static final boolean DEBUG = true;
    static Vehicle ego;
    public static void main( String[] args ) throws IOException
    {
        if (DEBUG) System.out.printf("Launching connector...\n");
        AnkiConnector connector = new AnkiConnector("localhost", 5000);
        if (DEBUG) System.out.printf("Getting vehicle list...\n");
        List<Vehicle> vehicles = connector.findVehicles();

        if (DEBUG) System.out.printf("Selecting first vehicle...\n");
        ego = vehicles.stream().findFirst().get();
        if (DEBUG) System.out.printf("Connecting...\n");
        ego.connect();
        LocalizationPositionUpdateListener listener = new LocalizationPositionUpdateListener();
        ego.addMessageListener(LocalizationPositionUpdateMessage.class, listener);
        ego.sendMessage(new SdkModeMessage());

        ego.sendMessage(new SetSpeedMessage(1000, 0));

    }

    private static class LocalizationPositionUpdateListener implements MessageListener<LocalizationPositionUpdateMessage> {
        private int lastId = -1;

        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            if (m.getRoadPieceId() == 10) {
                ego.sendMessage(new SetSpeedMessage(0, 1000));
            }
        }
    }
}
