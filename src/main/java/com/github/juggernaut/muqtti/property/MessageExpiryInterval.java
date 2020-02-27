package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.FourByteIntegerProperty;

import java.nio.ByteBuffer;

import static com.github.juggernaut.muqtti.property.PropertyIdentifiers.MESSAGE_EXPIRY_INTERVAL;

/**
 * @author ameya
 */
public class MessageExpiryInterval extends FourByteIntegerProperty {

    public MessageExpiryInterval(long value) {
        super(MESSAGE_EXPIRY_INTERVAL, value);
    }

    public static MessageExpiryInterval fromBuffer(ByteBuffer buffer) {
        return new MessageExpiryInterval(decodeValue(buffer));
    }
}
