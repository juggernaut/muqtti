package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.property.MqttProperty;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ameya
 */
public class SubAck extends MqttPacket {

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
