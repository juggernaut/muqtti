package com.github.juggernaut.macchar.packet;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class PingReq extends MqttPacket {

    public static final PingReq INSTANCE = new PingReq();

    private PingReq() {
        super(PacketType.PINGREQ, 0);
    }

    @Override
    protected int getEncodedVariableHeaderLength() {
        return 0;
    }

    @Override
    protected int getEncodedPayloadLength() {
        return 0;
    }

    @Override
    protected void encodeVariableHeader(ByteBuffer buffer) {

    }

    @Override
    protected ByteBuffer encodePayload() {
        return null;
    }
}
