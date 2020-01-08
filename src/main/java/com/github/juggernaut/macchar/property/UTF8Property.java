package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.ByteBufferUtil;

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
        // 1 byte id + 2 byte strlen + string
        return 1 + 2 + ByteBufferUtil.getUTF8StringLengthInBytes(value);
    }

    @Override
    public void encodeValue(ByteBuffer buffer) {
        ByteBufferUtil.encodeUTF8String(buffer, value);
    }
}
