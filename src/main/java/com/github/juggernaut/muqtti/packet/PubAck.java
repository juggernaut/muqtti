package com.github.juggernaut.muqtti.packet;

import com.github.juggernaut.muqtti.ByteBufferUtil;
import com.github.juggernaut.muqtti.exception.DecodingException;
import com.github.juggernaut.muqtti.property.MqttProperty;
import com.github.juggernaut.muqtti.property.PropertiesDecoder;

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

    public static PubAck fromBuffer(int flags, ByteBuffer buffer) {
        validateFlags(flags);
        int packetId = ByteBufferUtil.decodeTwoByteInteger(buffer);
        final ReasonCode reasonCode;
        if (!buffer.hasRemaining()) {
            // Byte 3 in the Variable Header is the PUBACK Reason Code. If the Remaining Length is 2, then there is no Reason Code and the value of 0x00 (Success) is used.
            reasonCode = ReasonCode.SUCCESS;
        } else {
            final byte reasonCodeByte = buffer.get();
            reasonCode = ReasonCode.fromIntValue(Byte.toUnsignedInt(reasonCodeByte));
        }
        if (!buffer.hasRemaining()) {
            // If the Remaining Length is less than 4 there is no Property Length and the value of 0 is used
            // TODO: empty properties
        } else {
            PropertiesDecoder.decode(buffer);
        }
        return new PubAck(packetId, reasonCode);
    }

    private static void validateFlags(int flags) {
        if (flags != 0) {
            throw new DecodingException("PUBACK flags must be 0");
        }
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
