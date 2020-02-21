package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.property.types.TwoByteIntegerProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class ReceiveMaximum extends TwoByteIntegerProperty {

    public ReceiveMaximum(int value) {
        super(PropertyIdentifiers.RECEIVE_MAXIMUM, value);
    }

    public static ReceiveMaximum fromBuffer(ByteBuffer buffer) {
        return new ReceiveMaximum(TwoByteIntegerProperty.decodeValue(buffer));
    }
}
