package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.FourByteIntegerProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class WillDelayInterval extends FourByteIntegerProperty {

    public WillDelayInterval(long value) {
        super(0x18, value);
    }

    public static WillDelayInterval fromBuffer(ByteBuffer buffer) {
        return new WillDelayInterval(FourByteIntegerProperty.decodeValue(buffer));
    }
}
