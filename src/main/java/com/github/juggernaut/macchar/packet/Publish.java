package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.QoS;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class Publish extends MqttPacket {

    protected Publish(int flags) {
        super(PacketType.PUBLISH, flags);
    }

    public static Publish fromBuffer(final int flags, final ByteBuffer buffer) {
        validateFlags(flags);
        final QoS qos = getQoS(flags);
        final String topicName = decodeTopicName(buffer);
        int packetId = -1;
        if (qos != QoS.AT_MOST_ONCE) {
            packetId = decodePacketIdentifier(buffer);
        }
    }

    private static void validateFlags(int flags) {
        int qos = (flags >> 1) & 0x03;
        if (qos == 3) {
            // A PUBLISH Packet MUST NOT have both QoS bits set to 1 [MQTT-3.3.1-4]
            throw new IllegalArgumentException("QoS cannot have both bits set in PUBLISH packet flags");
        }
    }

    private static QoS getQoS(int flags) {
        int qos = (flags >> 1) & 0x03;
        return QoS.fromIntValue(qos);
    }

    private static String decodeTopicName(final ByteBuffer buffer) {
        return ByteBufferUtil.decodeUTF8String(buffer);
    }

    private static int decodePacketIdentifier(final ByteBuffer buffer) {
        return ByteBufferUtil.decodeTwoByteInteger(buffer);
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
}
