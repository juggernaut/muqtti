package com.github.juggernaut.macchar.property.types;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.VariableByteIntegerDecoder;
import com.github.juggernaut.macchar.property.MqttProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public abstract class VariableByteIntegerProperty extends MqttProperty {

    private final int value;

    public VariableByteIntegerProperty(int propertyIdentifier, int value) {
        super(propertyIdentifier);
        this.value = value;
    }

    @Override
    public int getEncodedLength() {
        return 1 + ByteBufferUtil.getEncodedVariableByteIntegerLength(value);
    }

    @Override
    protected void encodeValue(ByteBuffer buffer) {
         ByteBufferUtil.encodeVariableByteInteger(buffer, value);
    }

    protected static int decodeValue(final ByteBuffer buffer) {
        final var decoder = new VariableByteIntegerDecoder();
        if (decoder.decode(buffer)) {
            return decoder.getValue();
        }
        throw new IllegalArgumentException("Invalid variable byte integer in buffer");
    }

    public int getValue() {
        return value;
    }
}
