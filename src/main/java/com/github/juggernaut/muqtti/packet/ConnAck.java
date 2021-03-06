package com.github.juggernaut.muqtti.packet;

import com.github.juggernaut.muqtti.ByteBufferUtil;
import com.github.juggernaut.muqtti.Configuration;
import com.github.juggernaut.muqtti.property.AssignedClientIdentifier;
import com.github.juggernaut.muqtti.property.MaximumQoS;
import com.github.juggernaut.muqtti.property.MqttProperty;
import com.github.juggernaut.muqtti.property.ServerKeepAlive;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.juggernaut.muqtti.property.PropertyIdentifiers.ASSIGNED_CLIENT_IDENTIFIER;

/**
 * @author ameya
 */
public class ConnAck extends MqttPacket {

    public enum ConnectReasonCode {
        SUCCESS(0x00),
        MALFORMED_PACKET(0x81),
        PROTOCOL_ERROR(0x82),
        UNSUPPORTED_PROTOCOL_VERSION(0x84),
        UNSPECIFIED_ERROR(0x80),
        QOS_NOT_SUPPORTED(0x9b),
        ;

        private final int intValue;

        ConnectReasonCode(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static ConnectReasonCode fromIntValue(int value) {
            return Arrays.stream(values())
                    .filter(v -> v.intValue == value)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No ConnectReasonCode for value " + value));
        }
    }

    private final ConnectReasonCode connectReasonCode;
    private final boolean sessionPresent;
    private final Optional<String> assignedClientId;
    private final Optional<Integer> keepAlive;
    private final List<MqttProperty> properties = new ArrayList<>();


    public ConnAck(ConnectReasonCode connectReasonCode, boolean sessionPresent,
                   Optional<String> assignedClientId, Optional<Integer> keepAlive) {
        super(PacketType.CONNACK, 0); // flags is reserved = 0
        this.connectReasonCode = connectReasonCode;
        this.sessionPresent = sessionPresent;
        this.assignedClientId = assignedClientId;
        this.keepAlive = keepAlive;
        populateProperties();
    }

    private void populateProperties() {
        properties.add(new MaximumQoS(Configuration.MAX_SUPPORTED_QOS));
        assignedClientId.map(AssignedClientIdentifier::new).ifPresent(properties::add);
        keepAlive.map(ServerKeepAlive::new).ifPresent(properties::add);
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
