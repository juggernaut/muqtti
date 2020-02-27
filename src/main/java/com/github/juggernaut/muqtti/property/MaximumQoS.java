package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.packet.QoS;
import com.github.juggernaut.muqtti.property.types.ByteProperty;

/**
 * @author ameya
 */
public class MaximumQoS extends ByteProperty {

    public MaximumQoS(QoS maxQoS) {
        super(0x24, (byte) maxQoS.getIntValue());
    }
}
