package edu.oswego.collisiongang.anki.signals;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimestampSignalTest {
    @Test
    void testTimeSignalParse() {
        long timestamp = System.currentTimeMillis();
        String addr = "test";

        TimestampSignal msg = new TimestampSignal(addr, timestamp);
        msg = new TimestampSignal(msg.getPayload());

        assertEquals(timestamp, msg.getTimestamp());
        assertEquals(addr, msg.getAddr());
    }
}