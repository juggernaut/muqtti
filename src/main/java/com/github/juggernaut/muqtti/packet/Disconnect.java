package com.github.juggernaut.muqtti.packet;

import com.github.juggernaut.muqtti.ByteBufferUtil;
import com.github.juggernaut.muqtti.exception.DecodingException;
import com.github.juggernaut.muqtti.property.MqttProperty;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author ameya
 */
public class Disconnect extends MqttPacket {

    public static Disconnect fromFixedHeaderOnly(int flags) {
        if (flags != 0) {
            throw new DecodingException("DISCONNECT flags must be 0");
        }
        // 3.14.2.1: If the Remaining Length is less than 1 the value of 0x00 (Normal disconnection) is used
        return Disconnect.create(ReasonCode.NORMAL_DISCONNECTION);
    }

    public enum ReasonCode {
        // TODO: all reason codes in Table 3‑10
        NORMAL_DISCONNECTION(0x0),
        SESSION_TAKEN_OVER(0x8E),
        QOS_NOT_SUPPORTED(0x9B),
        MALFORMED_ERROR(0x81),
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

    private final ReasonCode reasonCode;

    protected Disconnect(int flags, ReasonCode reasonCode) {
        super(PacketType.DISCONNECT, flags);
        this.reasonCode = reasonCode;
    }

    public static Disconnect create(ReasonCode reasonCode) {
        // flags is reserved = 0
        return new Disconnect(0, reasonCode);
    }

    public static Disconnect fromBuffer(int flags, ByteBuffer buffer) {
        if (flags != 0) {
            throw new DecodingException("DISCONNECT flags must be 0");
        }
        final byte reasonCodeByte = buffer.get();
        final ReasonCode reasonCode = ReasonCode.fromIntValue(Byte.toUnsignedInt(reasonCodeByte));
        // TODO: handle properties
        return Disconnect.create(reasonCode);
    }

    @Override
    protected int getEncodedVariableHeaderLength() {
        // TODO: for now consider empty properties
        final List<MqttProperty> properties = Collections.emptyList();
        final int propertyLength = getEncodedPropertiesLength(properties);
        // reason code + property length encoded + property length
        return 1 + ByteBufferUtil.getEncodedVariableByteIntegerLength(propertyLength) + propertyLength;
    }

    @Override
    protected int getEncodedPayloadLength() {
        return 0;
    }

    @Override
    protected void encodeVariableHeader(ByteBuffer buffer) {
        buffer.put((byte) reasonCode.getValue());
        // property length
        ByteBufferUtil.encodeVariableByteInteger(buffer, 0);
    }

    @Override
    protected ByteBuffer encodePayload() {
        return null;
    }
}
