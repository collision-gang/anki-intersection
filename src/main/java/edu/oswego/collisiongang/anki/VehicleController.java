package edu.oswego.collisiongang.anki;

import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.LocalizationIntersectionUpdateMessage;
import de.adesso.anki.messages.LocalizationPositionUpdateMessage;
import de.adesso.anki.messages.SdkModeMessage;
import de.adesso.anki.messages.SetSpeedMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class VehicleController implements Runnable {
    private Vehicle ego;
    private ServerSocket peerServer;
    private AtomicBoolean arrivedAtIntersection = new AtomicBoolean(false);
    private AtomicBoolean navigatingIntersection = new AtomicBoolean(false);

    public VehicleController(Vehicle ego, int port) throws IOException {
        this.ego = ego;
        peerServer = new ServerSocket(port);
    }

    @Override
    public void run() {
        ego.connect();
        ego.sendMessage(new SdkModeMessage());
        ego.addMessageListener(LocalizationPositionUpdateMessage.class, new LocalizationPositionUpdateListener());
        ego.addMessageListener(LocalizationIntersectionUpdateMessage.class, new LocalizationIntersectionUpdateListener());
        ego.sendMessage(new SetSpeedMessage(300, 1000));

        for (;;) {
            if (arrivedAtIntersection.get()) {
                arrivedAtIntersection.compareAndSet(true, false);
                navigatingIntersection.compareAndSet(false, true);
                long arrivalTime = System.currentTimeMillis();
                ego.sendMessage(new SetSpeedMessage(0, 1000));
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ego.sendMessage(new SetSpeedMessage(300, 1000));
            }
        }
    }

    class LocalizationPositionUpdateListener implements MessageListener<LocalizationPositionUpdateMessage> {

        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            if (!navigatingIntersection.get() && m.getRoadPieceId() == 10) {
             arrivedAtIntersection.compareAndSet(false, true);
            }
        }
    }

    class LocalizationIntersectionUpdateListener implements MessageListener<LocalizationIntersectionUpdateMessage> {

        @Override
        public void messageReceived(LocalizationIntersectionUpdateMessage m) {
            if (m.isExiting()) navigatingIntersection.compareAndSet(true, false);
        }
    }
}
