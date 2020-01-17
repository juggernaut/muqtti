package com.github.juggernaut.macchar.packet;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class PingResp extends MqttPacket {

    public static final PingResp INSTANCE = new PingResp();

    private static final ByteBuffer PACKET = ByteBuffer.allocate(2);

    static {
        PACKET.put((byte) (PacketType.PINGRESP.getIntValue() << 4));
        PACKET.put((byte) 0);
        PACKET.flip();
    }

    private PingResp() {
        super(PacketType.PINGRESP, 0);
    }

    @Override
    public ByteBuffer[] encode() {
        return new ByteBuffer[] { PACKET.slice() };
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
        // intentionally empty
    }

    @Override
    protected ByteBuffer encodePayload() {
        return null;
    }
}
