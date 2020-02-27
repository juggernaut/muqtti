package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.ByteProperty;

import java.nio.ByteBuffer;

import static com.github.juggernaut.muqtti.property.PropertyIdentifiers.PAYLOAD_FORMAT_INDICATOR;

/**
 * @author ameya
 */
public class PayloadFormatIndicator extends ByteProperty {

    public PayloadFormatIndicator(byte value) {
        super(PAYLOAD_FORMAT_INDICATOR, value);
    }

    public static PayloadFormatIndicator fromBuffer(ByteBuffer buffer) {
        return new PayloadFormatIndicator(decodeValue(buffer));
    }
}
