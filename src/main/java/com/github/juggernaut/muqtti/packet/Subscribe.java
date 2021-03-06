package com.github.juggernaut.muqtti.packet;

import com.github.juggernaut.muqtti.ByteBufferUtil;
import com.github.juggernaut.muqtti.exception.DecodingException;
import com.github.juggernaut.muqtti.property.PropertiesDecoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ameya
 */
public class Subscribe extends MqttPacket {

    public static class Subscription {
        private final TopicFilter filter;
        private final int subscriptionOptions;

        private Subscription(TopicFilter filter, int subscriptionOptions) {
            this.filter = filter;
            this.subscriptionOptions = subscriptionOptions;
        }

        public TopicFilter getFilter() {
            return filter;
        }

        public QoS getQoS() {
            return QoS.fromIntValue(subscriptionOptions & 0x03);
        }

        public boolean hasNoLocalOption() {
            return ((subscriptionOptions >> 2) & 0x01) == 1;
        }

        public boolean hasRetainAsPublishedOption() {
            return ((subscriptionOptions >> 3) & 0x01) == 1;
        }

        public int getRetainHandlingOption() {
            return ((subscriptionOptions >> 4) & 0x03);
        }

        public static Subscription fromBuffer(ByteBuffer buffer) {
            // The Topic Filters MUST be a UTF-8 Encoded String [MQTT-3.8.3-1]
            final String filter = ByteBufferUtil.decodeUTF8String(buffer);
            final var topicFilter = TopicFilter.fromString(filter);
            final byte options = buffer.get();
            validateOptions(options);
            return new Subscription(topicFilter, options);
        }

        private static void validateOptions(byte options) {
            int qos = options & 0x03;
            if (qos == 3) {
                throw new DecodingException("Invalid QoS value in topic filter subscription options");
            }
            int retainHandling = ((options >> 4) & 0x03);
            if (retainHandling == 3) {
                throw new DecodingException("Invalid value for Retain Handling in subscription options");
            }
            // Bits 6 and 7 of the Subscription Options byte are reserved for future use. The Server MUST treat a SUBSCRIBE packet as malformed if any of Reserved bits in the Payload are non-zero [MQTT-3.8.3-5]
            final int reserved = ((options >> 6) & 0x03);
            if (reserved != 0) {
                throw new DecodingException("Reserved bits in subscription options byte are non-zero");
            }
        }
    }

    private final int packetId;
    private final List<Subscription> subscriptions;
    private final SubscribeProperties properties;

    public Subscribe(int flags, int packetId, List<Subscription> subscriptions, SubscribeProperties properties) {
        super(PacketType.SUBSCRIBE, flags);
        this.packetId = packetId;
        this.subscriptions = subscriptions;
        this.properties = properties;
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

    public static Subscribe fromBuffer(int flags, ByteBuffer buffer) {
        validateFlags(flags);
        int packetId = decodePacketIdentifier(buffer);
        final var rawProperties = PropertiesDecoder.decode(buffer);
        final var subscribeProperties = SubscribeProperties.fromRawProperties(rawProperties);
        if (!buffer.hasRemaining()) {
            // The Payload MUST contain at least one Topic Filter and Subscription Options pair [MQTT-3.8.3-2]
            throw new DecodingException("SUBSCRIBE packet must have a non-empty payload");
        }
        final var subscriptions = decodeSubscriptions(buffer);
        return new Subscribe(flags, packetId, subscriptions, subscribeProperties);
    }

    private static List<Subscription> decodeSubscriptions(ByteBuffer buffer) {
        final List<Subscription> subscriptions = new ArrayList<>();
        while (buffer.hasRemaining()) {
            subscriptions.add(Subscription.fromBuffer(buffer));
        }
        return subscriptions;
    }

    private static void validateFlags(int flags) {
        // Bits 3,2,1 and 0 of the Fixed Header of the SUBSCRIBE packet are reserved and MUST be set to 0,0,1 and 0 respectively. The Server MUST treat any other value as malformed and close the Network Connection [MQTT-3.8.1-1].
        if (flags != 2) {
            throw new IllegalArgumentException("flags in SUBSCRIBE packet are reserved and must be set to 0010");
        }
    }

    private static int decodePacketIdentifier(final ByteBuffer buffer) {
        return ByteBufferUtil.decodeTwoByteInteger(buffer);
    }

    public int getPacketId() {
        return packetId;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    @Override
    protected ByteBuffer encodePayload() {
        throw new UnsupportedOperationException("not implemented");
    }

    public SubscribeProperties getProperties() {
        return properties;
    }
}
