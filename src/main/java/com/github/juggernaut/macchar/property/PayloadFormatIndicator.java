package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.property.types.ByteProperty;

import java.nio.ByteBuffer;

import static com.github.juggernaut.macchar.property.PropertyIdentifiers.PAYLOAD_FORMAT_INDICATOR;

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
