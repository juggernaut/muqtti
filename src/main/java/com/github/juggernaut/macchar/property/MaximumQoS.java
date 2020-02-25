package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.packet.QoS;
import com.github.juggernaut.macchar.property.types.ByteProperty;

/**
 * @author ameya
 */
public class MaximumQoS extends ByteProperty {

    public MaximumQoS(QoS maxQoS) {
        super(0x24, (byte) maxQoS.getIntValue());
    }
}
