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
    public static void main( String[] args ) throws IOException
    {
        if (DEBUG) System.out.printf("Launching connector...\n");
        AnkiConnector connector = new AnkiConnector("localhost", 5000);
        if (DEBUG) System.out.printf("Getting vehicle list...\n");
        List<Vehicle> vehicles = connector.findVehicles();

        Vehicle ego = vehicles.stream().findFirst().get();
        ego.connect();
        ego.sendMessage(new SdkModeMessage());
        ego.sendMessage(new SetSpeedMessage(1000, 0));
    }
}
