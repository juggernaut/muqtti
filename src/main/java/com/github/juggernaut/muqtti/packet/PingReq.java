package com.github.juggernaut.muqtti.packet;

import com.github.juggernaut.muqtti.exception.DecodingException;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class PingReq extends MqttPacket {

    public static final PingReq INSTANCE = new PingReq();

    private PingReq() {
        super(PacketType.PINGREQ, 0);
    }

    public static PingReq fromFixedHeaderOnly(int flags) {
        if (flags != 0) {
            throw new DecodingException("PINGREQ flags must be 0");
        }
        return PingReq.INSTANCE;
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
