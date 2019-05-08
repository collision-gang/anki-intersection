package edu.oswego.collisiongang.anki.signals;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TimestampSignal extends Signal {
    private final byte OP = 69;
    private String addr;
    private long timestamp;

    public TimestampSignal(String addr, long timestamp) {
        this.addr = addr;
        this.timestamp = timestamp;
        ByteBuffer buff = ByteBuffer.allocate(13 + addr.length());
        buff.put(OP);
        buff.putInt(addr.length());
        buff.put(addr.getBytes(StandardCharsets.US_ASCII));
        buff.putLong(timestamp);
        this.payload =  buff.array();
    }

    public TimestampSignal(byte[] arr) {
        super(arr.length);
        this.payload = arr;
        ByteBuffer buff = ByteBuffer.wrap(arr);
        buff.get();
        int len = buff.getInt();
        byte[] addrBytes = new byte[len];
        buff.get(addrBytes);
        addr = new String(addrBytes, StandardCharsets.US_ASCII);
        timestamp = buff.getLong();
    }
}
