package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.property.PropertiesDecoder;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class Publish extends MqttPacket {

    private final String topicName;
    private final int packetId; // can be -1 if QoS = 0
    private final ByteBuffer payload;

    protected Publish(int flags, String topicName, int packetId, final ByteBuffer payload) {
        super(PacketType.PUBLISH, flags);
        this.topicName = topicName;
        this.packetId = packetId;
        this.payload = payload;
    }

    public static Publish fromBuffer(final int flags, final ByteBuffer buffer) {
        validateFlags(flags);
        final QoS qos = getQoS(flags);
        final String topicName = decodeTopicName(buffer);
        validateTopicName(topicName);
        int packetId = -1;
        if (qos != QoS.AT_MOST_ONCE) {
            packetId = decodePacketIdentifier(buffer);
        }
        final var properties = PropertiesDecoder.decode(buffer);
        final var payload = buffer.slice();
        // Make sure that we tell the decoder layer that we have read this buffer fully
        buffer.position(buffer.limit());
        return new Publish(flags, topicName, packetId, payload);
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

    private static void validateTopicName(final String topicName) {
        // The Topic Name in the PUBLISH packet MUST NOT contain wildcard characters [MQTT-3.3.2-2]
        if (topicName.contains("#") || topicName.contains("+")) {
            throw new IllegalArgumentException("Topic Name in PUBLISH packet MUST NOT contain wildcard characters");
        }
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

    public String getTopicName() {
        return topicName;
    }

    public int getPacketId() {
        return packetId;
    }

    public QoS getQoS() {
        return getQoS(getFlags());
    }

    public boolean isRetain() {
        return (getFlags() & 0x01) == 1;
    }

    public boolean isDup() {
        return ((getFlags() >> 3) & 0x01) == 1;
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        throw new UnsupportedOperationException("not implemented");
    }
}
