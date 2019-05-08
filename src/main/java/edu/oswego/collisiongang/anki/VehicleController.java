package edu.oswego.collisiongang.anki;

import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.*;
import edu.oswego.collisiongang.anki.signals.TimestampSignal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class VehicleController implements Runnable {
    private Vehicle ego;
    private ServerSocket peerServer;
    private CountDownLatch arrivedAtIntersection = new CountDownLatch(1);
    private CountDownLatch readyToClear = new CountDownLatch(1);
    private AtomicBoolean navigatingIntersection = new AtomicBoolean(false);
    private Map<String, Socket> peers;
    private AtomicInteger generation = new AtomicInteger(0);
    private AtomicBoolean isMaster = new AtomicBoolean(false);

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
        ego.sendMessage(new SetSpeedMessage(400, 12500));


        for (;;) {
            try {
                arrivedAtIntersection.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            generation.getAndIncrement();
            arrivedAtIntersection = new CountDownLatch(1);
            navigatingIntersection.compareAndSet(false, true);
            long arrivalTime = System.currentTimeMillis();
            ego.sendMessage(new SetSpeedMessage(0, 12500));
            isMaster.compareAndSet(false, true);
            ForkJoinPool.commonPool().execute(() -> {
                int currentGen = generation.get();
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }

                if (generation.get() == currentGen) readyToClear.countDown();
            });

            TimestampSignal arrivalMessage = new TimestampSignal(ego.getAddress(), arrivalTime);
            for(Socket s : peers.values()) {
                try {
                    s.getChannel().write(ByteBuffer.wrap(arrivalMessage.getPayload()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                readyToClear.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setPeers(Map<String, InetSocketAddress> peerAddrs) throws IOException {
        for (Map.Entry<String, InetSocketAddress> e : peerAddrs.entrySet()) {
            if (!e.getKey().equals(ego.getAddress())) peers.put(e.getKey(), new Socket(e.getValue().getAddress(), e.getValue().getPort()));
        }
    }

    class LocalizationPositionUpdateListener implements MessageListener<LocalizationPositionUpdateMessage> {

        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            if (!navigatingIntersection.get() && m.getRoadPieceId() == 10) {
             arrivedAtIntersection.countDown();
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
