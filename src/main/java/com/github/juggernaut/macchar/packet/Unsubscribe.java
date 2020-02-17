package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.TopicFilter;
import com.github.juggernaut.macchar.property.PropertiesDecoder;
import com.github.juggernaut.macchar.property.UserProperty;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ameya
 */
public class Unsubscribe extends MqttPacket {

    private final int packetId;
    private final List<TopicFilter> topicFilters;
    private final List<UserProperty> userProperties;


    public Unsubscribe(int flags, int packetId, List<TopicFilter> topicFilters, List<UserProperty> userProperties) {
        super(PacketType.UNSUBSCRIBE, flags);
        this.packetId = packetId;
        this.topicFilters = topicFilters;
        this.userProperties = userProperties;
    }

    public static Unsubscribe fromBuffer(int flags, ByteBuffer buffer) {
        validateFlags(flags);
        final int packetId = ByteBufferUtil.decodeTwoByteInteger(buffer);
        final var rawProperties = PropertiesDecoder.decode(buffer);
        final var userProperties = Utils.extractUserProperties(rawProperties);
        final var topicFilters = decodeTopicFilters(buffer);
        // The Payload of an UNSUBSCRIBE packet MUST contain at least one Topic Filter [MQTT-3.10.3-2]. An UNSUBSCRIBE packet with no Payload is a Protocol Error
        if (topicFilters.isEmpty()) {
            throw new IllegalArgumentException("UNSUBSCRIBE must contain at least one Topic Filter");
        }
        return new Unsubscribe(flags, packetId, topicFilters, userProperties);
    }

    private static List<TopicFilter> decodeTopicFilters(final ByteBuffer buffer) {
        final List<TopicFilter> topicFilters = new ArrayList<>();
        while (buffer.hasRemaining()) {
            final var filterString = ByteBufferUtil.decodeUTF8String(buffer);
            topicFilters.add(TopicFilter.fromString(filterString));
        }
        return topicFilters;
    }

    private static void validateFlags(int flags) {
        // Bits 3,2,1 and 0 of the Fixed Header of the UNSUBSCRIBE packet are reserved and MUST be set to 0,0,1 and 0 respectively. The Server MUST treat any other value as malformed and close the Network Connection [MQTT-3.10.1-1]
        if (flags != 2) {
            throw new IllegalArgumentException("UNSUBSCRIBE flags must be set to 2");
        }
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
        throw new IllegalArgumentException("not implemented");
    }

    public int getPacketId() {
        return packetId;
    }

    public List<TopicFilter> getTopicFilters() {
        return topicFilters;
    }

    public List<UserProperty> getUserProperties() {
        return userProperties;
    }
}
