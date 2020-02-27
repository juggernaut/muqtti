package com.github.juggernaut.muqtti;

import com.github.juggernaut.muqtti.packet.QoS;

/**
 * @author ameya
 */
public class Configuration {

    public static final QoS MAX_SUPPORTED_QOS = QoS.AT_LEAST_ONCE;
    public static final int MAX_KEEP_ALIVE = 600; // 10 minutes
}
