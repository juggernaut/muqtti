package com.github.juggernaut.macchar;

import java.nio.ByteBuffer;
import java.util.Optional;

import static com.github.juggernaut.macchar.PropertyIdentifiers.ASSIGNED_CLIENT_IDENTIFIER;

/**
 * @author ameya
 */
public class MqttConnAck extends MqttPacket {

    enum ConnectReasonCode {
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


    protected MqttConnAck(ConnectReasonCode connectReasonCode, boolean sessionPresent, Optional<String> assignedClientId) {
        super(PacketType.CONNACK, 0); // flags is reserved = 0
        this.connectReasonCode = connectReasonCode;
        this.sessionPresent = sessionPresent;
        this.assignedClientId = assignedClientId;
    }

    @Override
    public ByteBuffer encode() {
        // max length is
        // 2 byte fixed + 1 byte conn ack flags + 1 byte connect reason code + 1 byte property length +
        // 5 byte session expiry interval + 3 byte receive maximum + 2 byte maximum QoS + 2 byte retain available +
        // 5 byte max packet size + (1 + 36 + len('auto') = 4) clientId +  3 byte topic alias maximum +
        // unsupported (Reason, user properties, wildcard, subscription ids, shared sub, ...)
        // = 68, let's round to 70
        final var buffer = ByteBuffer.allocate(70);
        encodeFixedHeader(buffer);
        final int clientIdLength = assignedClientId.map(id -> 1 + 2 + ByteBufferUtil.getUTF8StringLengthInBytes(id)).orElse(0);
        final int propertyLength = clientIdLength;
        int remainingLength = 1 + 1 + 1 + propertyLength;
        ByteBufferUtil.encodeVariableByteInteger(buffer, remainingLength);
        encodeConnAckFlags(buffer);
        encodeConnReasonCode(buffer);
        ByteBufferUtil.encodeVariableByteInteger(buffer, propertyLength);
        encodeAssignedClientId(buffer);
        return buffer;
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

}
