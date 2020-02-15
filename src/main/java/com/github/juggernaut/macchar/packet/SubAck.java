package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.property.MqttProperty;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ameya
 */
public class SubAck extends MqttPacket {

    public enum ReasonCode {
        GRANTED_QOS_0(0x0),
        GRANTED_QOS_1(0x01),
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
    private final List<ReasonCode> reasonCodes;
    private final List<MqttProperty> properties = new ArrayList<>();


    public SubAck(int packetId, List<ReasonCode> reasonCodes) {
        super(PacketType.SUBACK, 0); // flags is reserved = 0
        this.packetId = packetId;
        this.reasonCodes = reasonCodes;
    }

    @Override
    protected int getEncodedVariableHeaderLength() {
        final int propertyLength = getEncodedPropertiesLength(properties);
        return 2 + ByteBufferUtil.getEncodedVariableByteIntegerLength(propertyLength) + propertyLength;
    }

    @Override
    protected int getEncodedPayloadLength() {
        return reasonCodes.size();
    }

    @Override
    protected void encodeVariableHeader(ByteBuffer buffer) {
        ByteBufferUtil.encodeTwoByteInteger(buffer, packetId);
        final int propertyLength = getEncodedPropertiesLength(properties);
        ByteBufferUtil.encodeVariableByteInteger(buffer, propertyLength);
        encodeProperties(buffer, properties);
    }

    private void encodeReasonCodes(ByteBuffer buffer) {
        reasonCodes.forEach(code -> buffer.put((byte) code.getValue()));
    }

    @Override
    protected ByteBuffer encodePayload() {
        final var payload = ByteBuffer.allocate(reasonCodes.size());
        encodeReasonCodes(payload);
        return payload.flip();
    }
}
