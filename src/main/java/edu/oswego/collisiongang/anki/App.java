package edu.oswego.collisiongang.anki;

import de.adesso.anki.*;
import de.adesso.anki.messages.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

/**
 * Hello world!
 *
 */
public class App {
    static final boolean DEBUG = true;
    static final String HOST = "localhost";
    static final int PORT = 5000;

    public static void main( String[] args ) throws IOException {
        AnkiConnector connector = new AnkiConnector(HOST, PORT);

        List<Vehicle> vehicleList = connector.findVehicles();
        Map<String, InetSocketAddress> peers = new HashMap<>();
        if (!vehicleList.isEmpty()) {
            System.out.println(vehicleList.size());
            int controllerPort = 5380;
            for (Vehicle v : vehicleList) {
                ForkJoinPool.commonPool().execute(new VehicleController(v, ++controllerPort));
                peers.put(v.getAddress(), new InetSocketAddress("localhost", controllerPort));
            }
        }

        for (Map.Entry<String, InetSocketAddress> e : peers.entrySet()) {
            System.out.printf("%s --- %s\n", e.getKey(), e.getValue());
        }
    }
}
