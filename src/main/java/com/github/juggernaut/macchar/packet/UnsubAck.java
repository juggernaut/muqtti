package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.property.MqttProperty;
import com.github.juggernaut.macchar.property.UserProperty;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ameya
 */
public class UnsubAck extends MqttPacket {

    public enum ReasonCode {

        SUCCESS(0x0),
        NO_SUBSCRIPTION_EXISTED(0x11),
        UNSPECIFIED_ERROR(0x80),
        IMPLEMENTATION_SPECIFIC_ERROR(0x83),
        NOT_AUTHORIZED(0x87),
        TOPIC_FILTER_INVALID(0x8F),
        PACKET_IDENTIFIER_IN_USE(0x91)
        ;

        private final int value;

        ReasonCode(final int value) {
            this.value = value;
        }

        public static ReasonCode fromIntValue(final int input) {
            return Arrays.stream(values()).filter(v -> v.value == input)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Illegal value " + input + " for Reason code"));
        }

        public int getValue() {
            return value;
        }
    }

    private final int packetId;
    private final List<UserProperty> userProperties;
    private final List<ReasonCode> reasonCodes;

    public UnsubAck(int packetId, List<UserProperty> userProperties, List<ReasonCode> reasonCodes) {
        super(PacketType.UNSUBACK, 0);
        this.packetId = packetId;
        this.userProperties = userProperties;
        this.reasonCodes = reasonCodes;
    }

    @Override
    protected int getEncodedVariableHeaderLength() {
        final int propertyLength = getEncodedPropertiesLength(userProperties.stream().map(u -> (MqttProperty) u).collect(Collectors.toList()));
        return 2 + ByteBufferUtil.getEncodedVariableByteIntegerLength(propertyLength) + propertyLength;
    }

    @Override
    protected int getEncodedPayloadLength() {
        return reasonCodes.size();
    }

    @Override
    protected void encodeVariableHeader(ByteBuffer buffer) {
        ByteBufferUtil.encodeTwoByteInteger(buffer, packetId);
        final var rawProperties = userProperties.stream().map(u -> (MqttProperty) u).collect(Collectors.toList());
        final int propertyLength = getEncodedPropertiesLength(rawProperties);
        ByteBufferUtil.encodeVariableByteInteger(buffer, propertyLength);
        encodeProperties(buffer, rawProperties);
    }

    @Override
    protected ByteBuffer encodePayload() {
        final ByteBuffer payload = ByteBuffer.allocate(reasonCodes.size());
        reasonCodes.forEach(code -> payload.put((byte) code.value));
        return payload.flip();
    }
}