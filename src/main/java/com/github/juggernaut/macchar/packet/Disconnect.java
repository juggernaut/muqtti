package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.property.MqttProperty;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * @author ameya
 */
public class Disconnect extends MqttPacket {

    private final ReasonCode reasonCode;

    protected Disconnect(int flags, ReasonCode reasonCode) {
        super(PacketType.DISCONNECT, flags);
        this.reasonCode = reasonCode;
    }

    public static Disconnect create(ReasonCode reasonCode) {
        // flags is reserved = 0
        return new Disconnect(0, reasonCode);
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
