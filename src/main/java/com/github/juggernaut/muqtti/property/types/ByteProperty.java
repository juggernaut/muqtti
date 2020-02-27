package com.github.juggernaut.muqtti.property.types;

import com.github.juggernaut.muqtti.property.MqttProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public abstract class ByteProperty extends MqttProperty {

    private final byte value;

    protected ByteProperty(int propertyIdentifier, byte value) {
        super(propertyIdentifier);
        this.value = value;
    }

    @Override
    public int getEncodedLength() {
        return 1 + 1;
    }

    @Override
    protected void encodeValue(ByteBuffer buffer) {
        buffer.put(value);
    }

    public byte getValue() {
        return value;
    }

    protected static byte decodeValue(ByteBuffer buffer) {
        return buffer.get();
    }
}
