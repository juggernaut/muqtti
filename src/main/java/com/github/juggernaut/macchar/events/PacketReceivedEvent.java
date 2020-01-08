package com.github.juggernaut.macchar.events;

import com.github.juggernaut.macchar.Event;
import com.github.juggernaut.macchar.MqttPacket;

/**
 * @author ameya
 */
public class PacketReceivedEvent implements Event {

    private final MqttPacket packet;

    public PacketReceivedEvent(MqttPacket packet) {
        this.packet = packet;
    }

    public MqttPacket getPacket() {
        return packet;
    }
}
