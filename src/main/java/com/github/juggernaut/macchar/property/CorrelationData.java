package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.property.types.BinaryDataProperty;

import java.nio.ByteBuffer;

import static com.github.juggernaut.macchar.property.PropertyIdentifiers.CORRELATION_DATA;

/**
 * @author ameya
 */
public class CorrelationData extends BinaryDataProperty {

    public CorrelationData(byte[] value) {
        super(CORRELATION_DATA, value);
    }

    public static CorrelationData fromBuffer(ByteBuffer buffer) {
        return new CorrelationData(decodeValue(buffer));
    }
}
