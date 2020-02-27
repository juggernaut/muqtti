package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.BinaryDataProperty;

import java.nio.ByteBuffer;

import static com.github.juggernaut.muqtti.property.PropertyIdentifiers.CORRELATION_DATA;

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
