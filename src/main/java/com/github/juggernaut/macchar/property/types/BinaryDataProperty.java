package com.github.juggernaut.macchar.property.types;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.property.MqttProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class BinaryDataProperty extends MqttProperty {

    private final byte[] value;

    public BinaryDataProperty(int propertyIdentifier, byte[] value) {
        super(propertyIdentifier);
        this.value = value;
    }

    @Override
    public int getEncodedLength() {
        return 1 + ByteBufferUtil.getEncodedBinaryDataLength(value);
    }

    @Override
    protected void encodeValue(ByteBuffer buffer) {
        ByteBufferUtil.encodeBinaryData(buffer, value);
    }

    public byte[] getValue() {
        return value;
    }

    protected static byte[] decodeValue(ByteBuffer buffer) {
        return ByteBufferUtil.decodeBinaryData(buffer);
    }
}
