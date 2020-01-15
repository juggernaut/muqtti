package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.property.AssignedClientIdentifier;
import com.github.juggernaut.macchar.property.MqttProperty;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.juggernaut.macchar.property.PropertyIdentifiers.ASSIGNED_CLIENT_IDENTIFIER;

/**
 * @author ameya
 */
public class ConnAck extends MqttPacket {

    public enum ConnectReasonCode {
        SUCCESS(0x00),
        UNSPECIFIED_ERROR(0x80);

        private final int intValue;

        ConnectReasonCode(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }
    }

    private final ConnectReasonCode connectReasonCode;
    private final boolean sessionPresent;
    private final Optional<String> assignedClientId;
    private final List<MqttProperty> properties = new ArrayList<>();


    public ConnAck(ConnectReasonCode connectReasonCode, boolean sessionPresent, Optional<String> assignedClientId) {
        super(PacketType.CONNACK, 0); // flags is reserved = 0
        this.connectReasonCode = connectReasonCode;
        this.sessionPresent = sessionPresent;
        this.assignedClientId = assignedClientId;
        populateProperties();
    }

    private void populateProperties() {
        assignedClientId.map(AssignedClientIdentifier::new).ifPresent(properties::add);
    }

    public void encodeVariableHeader(final ByteBuffer buffer) {
        encodeConnAckFlags(buffer);
        encodeConnReasonCode(buffer);
        final int propertyLength = getEncodedPropertiesLength(properties);
        ByteBufferUtil.encodeVariableByteInteger(buffer, propertyLength);
        encodeProperties(buffer, properties);
    }

    @Override
    protected int getEncodedVariableHeaderLength() {
        final int propertyLength = getEncodedPropertiesLength(properties);
        // 1 byte conn ack flags + 1 byte connect reason code + variable byte property length + property length
        return 1 + 1 + ByteBufferUtil.getEncodedVariableByteIntegerLength(propertyLength) + propertyLength;
    }

    @Override
    protected int getEncodedPayloadLength() {
        return 0;
    }

    private void encodeConnAckFlags(ByteBuffer buffer) {
        // If a Server sends a CONNACK packet containing a non-zero Reason Code it MUST set Session Present to 0 [MQTT-3.2.2-6]
        boolean computedSessionPresent = sessionPresent;
        if (connectReasonCode != ConnectReasonCode.SUCCESS) {
            computedSessionPresent = false;
        }
        final byte connAckFlags = (byte) (computedSessionPresent ? 1 : 0);
        buffer.put(connAckFlags);
    }

    private void encodeConnReasonCode(ByteBuffer buffer) {
        buffer.put((byte) connectReasonCode.intValue);
    }

    private void encodeAssignedClientId(ByteBuffer buffer) {
        assignedClientId.ifPresent(id -> {
            buffer.put((byte) ASSIGNED_CLIENT_IDENTIFIER);
            ByteBufferUtil.encodeUTF8String(buffer, id);
        });
    }

    @Override
    protected ByteBuffer encodePayload() {
        // payload for connack is empty
        return null;
    }
}
