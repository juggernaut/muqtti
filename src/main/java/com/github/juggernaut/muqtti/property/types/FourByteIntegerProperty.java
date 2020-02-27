package com.github.juggernaut.muqtti.property.types;

import com.github.juggernaut.muqtti.ByteBufferUtil;
import com.github.juggernaut.muqtti.property.MqttProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public abstract class FourByteIntegerProperty extends MqttProperty {

    private final long value;

    protected FourByteIntegerProperty(int propertyIdentifier, long value) {
        super(propertyIdentifier);
        this.value = value;
    }

    @Override
    public int getEncodedLength() {
        // 2.2.2.2: Although the Property Identifier is defined as a Variable Byte Integer, in this version of the specification all of the Property Identifiers are one byte long
        return 1 + 4;
    }

    @Override
    protected void encodeValue(ByteBuffer buffer) {
        ByteBufferUtil.encodeFourByteInteger(buffer, value);
    }

    protected static long decodeValue(ByteBuffer buffer) {
        return ByteBufferUtil.decodeFourByteInteger(buffer);
    }

    public long getValue() {
        return value;
    }
}
