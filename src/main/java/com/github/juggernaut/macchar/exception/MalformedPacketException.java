package com.github.juggernaut.macchar.exception;

import com.github.juggernaut.macchar.packet.MqttPacket;

/**
 * @author ameya
 */
public class MalformedPacketException extends MqttException {

    private final MqttPacket.PacketType packetType;
    private final int reasonCode;

    public MalformedPacketException(String message, MqttPacket.PacketType packetType, int reasonCode) {
        super(message);
        this.packetType = packetType;
        this.reasonCode = reasonCode;
    }

    public MqttPacket.PacketType getPacketType() {
        return packetType;
    }

    public int getReasonCode() {
        return reasonCode;
    }
}
