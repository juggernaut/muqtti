package com.github.juggernaut.muqtti.fsm.events;

import com.github.juggernaut.muqtti.packet.MqttPacket;

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
