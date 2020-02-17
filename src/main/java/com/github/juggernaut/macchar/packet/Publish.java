package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.property.MqttProperty;
import com.github.juggernaut.macchar.property.PropertiesDecoder;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author ameya
 */
public class Publish extends MqttPacket {

    private final String topicName;
    private final Optional<Integer> packetId; // empty if QoS=0
    private final ByteBuffer payload;
    private final PublishProperties publishProperties;

    protected Publish(int flags, String topicName, Optional<Integer> packetId, final ByteBuffer payload,
                      final PublishProperties publishProperties) {
        super(PacketType.PUBLISH, flags);
        this.topicName = topicName;
        this.packetId = packetId;
        this.payload = payload;
        this.publishProperties = publishProperties;
    }

    public static Publish fromBuffer(final int flags, final ByteBuffer buffer) {
        validateFlags(flags);
        final QoS qos = getQoS(flags);
        final String topicName = decodeTopicName(buffer);
        Utils.validateTopicName(topicName);
        Integer packetId = null;
        if (qos != QoS.AT_MOST_ONCE) {
            packetId = decodePacketIdentifier(buffer);
        }
        final var properties = PropertiesDecoder.decode(buffer);
        final var publishProperties = PublishProperties.fromRawProperties(properties);
        final var payload = buffer.slice();
        // Make sure that we tell the decoder layer that we have read this buffer fully
        buffer.position(buffer.limit());
        return new Publish(flags, topicName, Optional.ofNullable(packetId), payload, publishProperties);
    }

    public static Publish create(final QoS qos, final boolean dup, final boolean retain, final String topicName,
                                 final Optional<Integer> packetId, final ByteBuffer payload) {
        if (qos == QoS.AT_MOST_ONCE && packetId.isPresent()) {
            throw new IllegalArgumentException("Packet ID cannot be specified if QoS is 0");
        }
        if (qos != QoS.AT_MOST_ONCE && packetId.isEmpty()) {
            throw new IllegalArgumentException("Packet ID must be present if Qos > 0");
        }
        final int flags = encodeFlags(qos, dup, retain);
        return new Publish(flags, topicName, packetId, payload, PublishProperties.emptyProperties());
    }

    private static int encodeFlags(QoS qos, boolean dup, boolean retain) {
        int flags = 0;
        if (dup) {
            flags |= 0x08;
        }
        flags |= (qos.getIntValue() << 1);
        if (retain) {
            flags |= 0x01;
        }
        return flags;
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
        final int propertyLength = getEncodedPropertiesLength(publishProperties.getRawProperties());
        return ByteBufferUtil.getEncodedUTF8StringLenghInBytes(topicName) +
                packetId.map(p -> 2).orElse(0) +
                ByteBufferUtil.getEncodedVariableByteIntegerLength(propertyLength) +
                propertyLength;
    }

    @Override
    protected int getEncodedPayloadLength() {
        return payload.remaining();
    }

    @Override
    protected void encodeVariableHeader(ByteBuffer buffer) {
        ByteBufferUtil.encodeUTF8String(buffer, topicName);
        packetId.ifPresent(p -> ByteBufferUtil.encodeTwoByteInteger(buffer, p));
        final int propertyLength = getEncodedPropertiesLength(publishProperties.getRawProperties());
        ByteBufferUtil.encodeVariableByteInteger(buffer, propertyLength);
        encodeProperties(buffer, publishProperties.getRawProperties());
    }

    public String getTopicName() {
        return topicName;
    }

    public Optional<Integer> getPacketId() {
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
    protected ByteBuffer encodePayload() {
        return payload;
    }
}
