package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.property.types.FourByteIntegerProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class MaximumPacketSize extends FourByteIntegerProperty {

    public MaximumPacketSize(long value) {
        super(PropertyIdentifiers.MAXIMUM_PACKET_SIZE, value);
    }

    public static MaximumPacketSize fromBuffer(ByteBuffer buffer) {
        return new MaximumPacketSize(FourByteIntegerProperty.decodeValue(buffer));
    }
}
