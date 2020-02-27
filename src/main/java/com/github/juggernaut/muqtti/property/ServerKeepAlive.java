package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.TwoByteIntegerProperty;

/**
 * @author ameya
 */
public class ServerKeepAlive extends TwoByteIntegerProperty {

    public ServerKeepAlive(int value) {
        super(0x13, value);
    }
}
