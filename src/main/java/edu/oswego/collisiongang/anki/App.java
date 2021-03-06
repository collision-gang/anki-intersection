package edu.oswego.collisiongang.anki;

import de.adesso.anki.*;
import de.adesso.anki.messages.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
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
        List<VehicleController> controllerList = new ArrayList<>();
        if (!vehicleList.isEmpty()) {
            System.out.println(vehicleList.size());
            int controllerPort = 5380;
            for (Vehicle v : vehicleList) {
                VehicleController vc = new VehicleController(v, ++controllerPort);
                ForkJoinPool.commonPool().execute(vc);
                peers.put(v.getAddress(), new InetSocketAddress("localhost", controllerPort));
                controllerList.add(vc);
            }

            for(VehicleController vc : controllerList)
                vc.setPeers(peers);
        }

        for (Map.Entry<String, InetSocketAddress> e : peers.entrySet()) {
            System.out.printf("%s --- %s\n", e.getKey(), e.getValue());
        }
    }
}
