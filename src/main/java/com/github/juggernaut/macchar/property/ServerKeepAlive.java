package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.property.types.TwoByteIntegerProperty;

/**
 * @author ameya
 */
public class ServerKeepAlive extends TwoByteIntegerProperty {

    public ServerKeepAlive(int value) {
        super(0x13, value);
    }
}
