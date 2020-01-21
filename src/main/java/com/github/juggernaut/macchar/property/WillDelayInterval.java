package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.property.types.FourByteIntegerProperty;

/**
 * @author ameya
 */
public class WillDelayInterval extends FourByteIntegerProperty {

    public WillDelayInterval(long value) {
        super(0x18, value);
    }
}
