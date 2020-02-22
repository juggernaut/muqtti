package com.github.juggernaut.macchar.property.types;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.property.MqttProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public abstract class TwoByteIntegerProperty extends MqttProperty {

    private final int value;

    public TwoByteIntegerProperty(int propertyIdentifier, int value) {
        super(propertyIdentifier);
        this.value = value;
    }

    @Override
    public int getEncodedLength() {
        return 1 + 2;
    }

    @Override
    protected void encodeValue(ByteBuffer buffer) {
        ByteBufferUtil.encodeTwoByteInteger(buffer, value);
    }

    public int getValue() {
        return value;
    }

    protected static int decodeValue(ByteBuffer buffer) {
        return ByteBufferUtil.decodeTwoByteInteger(buffer);
    }
}
