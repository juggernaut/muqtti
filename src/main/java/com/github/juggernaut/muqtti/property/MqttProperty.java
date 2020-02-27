package com.github.juggernaut.muqtti.property;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public abstract class MqttProperty {

    private final int propertyIdentifier;

    protected MqttProperty(int propertyIdentifier) {
        this.propertyIdentifier = propertyIdentifier;
    }

    public int getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public abstract int getEncodedLength();
    protected abstract void encodeValue(ByteBuffer buffer);

    public void encode(ByteBuffer buffer) {
       // this is actually variable byte, but all property ids fit in a byte in the spec
       buffer.put((byte) propertyIdentifier);
       encodeValue(buffer);
    }
}
