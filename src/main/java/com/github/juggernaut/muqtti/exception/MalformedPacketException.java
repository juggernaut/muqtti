package com.github.juggernaut.muqtti.exception;

import com.github.juggernaut.muqtti.packet.MqttPacket;

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
