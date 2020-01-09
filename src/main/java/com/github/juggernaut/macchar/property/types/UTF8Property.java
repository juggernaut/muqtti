package com.github.juggernaut.macchar.property.types;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.property.MqttProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public abstract class UTF8Property extends MqttProperty {

    private final String value;

    protected UTF8Property(int propertyIdentifier, String value) {
        super(propertyIdentifier);
        this.value = value;
    }

    @Override
    public int getEncodedLength() {
        // 1 byte id + encoded string length
        return 1 + ByteBufferUtil.getEncodedUTF8StringLenghInBytes(value);
    }

    @Override
    public void encodeValue(ByteBuffer buffer) {
        ByteBufferUtil.encodeUTF8String(buffer, value);
    }

    public String getValue() {
        return value;
    }

    protected static String decodeValue(ByteBuffer buffer) {
        return ByteBufferUtil.decodeUTF8String(buffer);
    }
}
