package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.property.MqttProperty;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author ameya
 */
public class PubAck extends MqttPacket {

    public enum ReasonCode {
        SUCCESS(0x0),
        NO_MATCHING_SUBSCRIBERS(0x10),
        UNSPECIFIED_ERROR(0x80),
        IMPLEMENTATION_SPECIFIC_ERROR(0x83),
        NOT_AUTHORIZED(0x87),
        TOPIC_NAME_INVALID(0x90),
        PACKET_IDENTIFIER_IN_USE(0x91),
        QUOTA_EXCEEDED(0x97),
        PAYLOAD_FORMAT_INVALID(0x99),
        ;

        final int value;

        ReasonCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ReasonCode fromIntValue(int input) {
            return Arrays.stream(values())
                    .filter(v -> v.value == input)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid integer value " + input + " for PUBACK reason code"));
        }
    }

    private final int packetId;
    private final ReasonCode reasonCode;


    protected PubAck(int packetId, ReasonCode reasonCode) {
        super(PacketType.PUBACK, 0);
        this.packetId = packetId;
        this.reasonCode = reasonCode;
    }

    public static PubAck create(int packetId, ReasonCode reasonCode) {
        return new PubAck(packetId, reasonCode);
    }

    @Override
    protected int getEncodedVariableHeaderLength() {
        // TODO: for now consider empty properties
        final List<MqttProperty> properties = Collections.emptyList();
        final int propertyLength = getEncodedPropertiesLength(properties);
        // packet id + reason code + property length encoded + property length
        return 2 + 1 + ByteBufferUtil.getEncodedVariableByteIntegerLength(propertyLength) + propertyLength;
    }

    @Override
    protected int getEncodedPayloadLength() {
        return 0;
    }

    @Override
    protected void encodeVariableHeader(ByteBuffer buffer) {
        ByteBufferUtil.encodeTwoByteInteger(buffer, packetId);
        buffer.put((byte) reasonCode.value);
        // TODO: 0-length properties for now
        ByteBufferUtil.encodeVariableByteInteger(buffer, 0);
    }

    @Override
    protected ByteBuffer encodePayload() {
        return null;
    }

    public int getPacketId() {
        return packetId;
    }

    public ReasonCode getReasonCode() {
        return reasonCode;
    }
}
