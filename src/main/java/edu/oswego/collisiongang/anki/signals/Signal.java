package edu.oswego.collisiongang.anki.signals;

public class Signal {
    protected byte[] payload;

    public Signal() {
        super();
    }

    public Signal(int len) {
        this.payload = new byte[len];
    }

    public Signal(byte[] payload) {
        this.payload = payload;
    }

    public byte opcode() {
        return payload[0];
    }

    public byte[] getPayload() {
        return payload;
    }
}
