package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.property.MqttProperty;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @author ameya
 */
public abstract class MqttPacket {

    public enum PacketType {
        CONNECT(1),
        CONNACK(2),
        PUBLISH(3),
        PUBACK(4),
        PUBREC(5),
        PUBREL(6),
        PUBCOMP(7),
        SUBSCRIBE(8),
        SUBACK(9),
        UNSUBSCRIBE(10),
        UNSUBACK(11),
        PINGREQ(12),
        PINGRESP(13),
        DISCONNECT(14),
        AUTH(15);

        private final int intValue;

        PacketType(int value) {
            this.intValue = value;
        }

        public static PacketType fromInt(int input) {
            return Arrays.stream(values()).filter(v -> v.intValue == input).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid value " + input + " for packet type"));
        }

        public int getIntValue() {
            return intValue;
        }
    }

    private final PacketType packetType;
    private final int flags;

    protected MqttPacket(PacketType packetType, int flags) {
        this.packetType = packetType;
        this.flags = flags;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public int getFlags() {
        return flags;
    }

    protected abstract int getEncodedVariableHeaderLength();
    protected abstract int getEncodedPayloadLength();
    protected abstract void encodeVariableHeader(ByteBuffer buffer);
    protected abstract ByteBuffer encodePayload();

    public ByteBuffer[] encode() {
        final int remainingLength = getEncodedVariableHeaderLength() + getEncodedPayloadLength();
        //final int packetSize = 1 + ByteBufferUtil.getEncodedVariableByteIntegerLength(remainingLength) + remainingLength;
        final int packetWithoutPayloadSize = 1 + ByteBufferUtil.getEncodedVariableByteIntegerLength(remainingLength) + getEncodedVariableHeaderLength();
        final var buffer = ByteBuffer.allocate(packetWithoutPayloadSize);
        encodeFixedHeader(buffer);
        encodeRemainingLength(buffer, remainingLength);
        encodeVariableHeader(buffer);
        buffer.flip();
        // Assuming payload is already flipped
        final var payload = encodePayload();
        if (payload != null) {
            return new ByteBuffer[] { buffer, payload };
        }
        return new ByteBuffer[] { buffer };
    }

    private void encodeFixedHeader(final ByteBuffer buffer) {
        byte headerByte = (byte) ((packetType.intValue << 4) | flags);
        buffer.put(headerByte);
    }

    private static void encodeRemainingLength(final ByteBuffer buffer, final int length) {
        ByteBufferUtil.encodeVariableByteInteger(buffer, length);
    }

    protected int getEncodedPropertiesLength(final List<MqttProperty> properties) {
        return properties.stream().mapToInt(MqttProperty::getEncodedLength).sum();
    }

    protected void encodeProperties(final ByteBuffer buffer, final List<MqttProperty> properties) {
        properties.forEach(p -> p.encode(buffer));
    }
}
