package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.VariableByteIntegerDecoder;
import com.github.juggernaut.macchar.exception.MalformedPacketException;
import com.github.juggernaut.macchar.property.PropertiesDecoder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * @author ameya
 */
public class Connect extends MqttPacket {

    private static int CLEAN_START = 1;
    private static int WILL_FLAG = 2;
    private static int WILL_QOS = 3;
    private static int WILL_RETAIN = 5;
    private static int PASSWORD_FLAG = 6;
    private static int USERNAME_FLAG = 7;

    private final int keepAlive;
    private final byte connectFlags;
    private final ConnectProperties connectProperties;
    private final String clientId;
    private final String userName;
    private final byte[] password;
    private final QoS willQos;
    private final WillData willData;

    public Connect(int flags, int keepAlive, byte connectFlags, ConnectProperties connectProperties, String clientId, String userName, byte[] password, QoS willQos, WillData willData) {
        super(PacketType.CONNECT, flags);
        this.keepAlive = keepAlive;
        this.connectFlags = connectFlags;
        this.connectProperties = connectProperties;
        this.clientId = Objects.requireNonNull(clientId);
        this.userName = userName;
        this.password = password;
        this.willQos = willQos;
        this.willData = willData;
    }

    public static Connect fromBuffer(final int flags, final ByteBuffer buffer) {
        // mqtt-v5.0 Section 3.1.2
        validateProtocolLength(buffer);
        validateProtocolName(buffer);
        validateProtocolVersion(buffer);
        final byte connectFlags = decodeConnectFlags(buffer);
        final QoS willQoS = decodeWillQoS(connectFlags);
        validateConnectFlags(connectFlags, willQoS);
        final int keepAlive = decodeKeepAlive(buffer);
        final int propertyLength = decodePropertyLength(buffer);
        if (propertyLength > buffer.remaining()) {
            throw new IllegalArgumentException("Invalid packet length based on property length");
        }
        ConnectProperties connectProperties = null;
        if (propertyLength > 0) {
            // Slicing the properties portion makes it easier to not overflow bounds and read invalid data if
            // somehow the properties are faked to entice us to read past the property length.
            final var propertiesSlice = buffer.slice();
            propertiesSlice.limit(propertyLength);
            connectProperties = new ConnectProperties();
            connectProperties.decodeFromBuffer(propertiesSlice);
            if (propertiesSlice.hasRemaining()) {
                throw new IllegalArgumentException("Invalid properties length in CONNECT PACKET");
            }
            buffer.position(buffer.position() + propertyLength);
        }
        // Payload
        // TODO: length checks
        final String clientId = decodeClientId(buffer);
        WillData willData = null;
        if (hasWillFlag(connectFlags)) {
            willData = decodeWillData(buffer, willQoS);
        }
        final String userName = decodeUsername(buffer, connectFlags);
        final byte[] password = decodePassword(buffer, connectFlags);
        if (buffer.hasRemaining()) {
            throw new IllegalArgumentException("Extraneous length in buffer for CONNECT packet");
        }
        return new Connect(flags, keepAlive, connectFlags, connectProperties, clientId, userName, password, willQoS, willData);
    }

    private static byte[] decodePassword(ByteBuffer buffer, byte connectFlags) {
        if (hasPasswordFlag(connectFlags)) {
            return ByteBufferUtil.decodeBinaryData(buffer);
        }
        return null;
    }

    private static void validateProtocolLength(ByteBuffer buffer) {
        int variableHeaderLength = ByteBufferUtil.decodeTwoByteInteger(buffer);
        if (variableHeaderLength != 4) {
            throw new MalformedPacketException("Variable header length of CONNECT packet must be 4!", PacketType.CONNECT, ConnAck.ConnectReasonCode.MALFORMED_PACKET.getIntValue());
        }
    }

    private static void validateProtocolName(ByteBuffer buffer) {
        final byte[] protocolName = new byte[4];
        buffer.get(protocolName);
        final String protocolNameStr = new String(protocolName, StandardCharsets.UTF_8);
        if (!"MQTT".equalsIgnoreCase(protocolNameStr)) {
            throw new MalformedPacketException("Protocol name in CONNECT packet must be 'MQTT'", PacketType.CONNECT, ConnAck.ConnectReasonCode.MALFORMED_PACKET.getIntValue());
        }
    }

    private static void validateProtocolVersion(ByteBuffer buffer) {
        final int protocolVersion = buffer.get();
        if (protocolVersion != 5) {
            throw new MalformedPacketException("Only MQTT version 5 is supported", PacketType.CONNECT, ConnAck.ConnectReasonCode.UNSUPPORTED_PROTOCOL_VERSION.getIntValue());
        }
    }

    private static byte decodeConnectFlags(ByteBuffer buffer) {
        byte connectFlags = buffer.get();
        // The Server MUST validate that the reserved flag in the CONNECT packet is set to 0 [MQTT-3.1.2-3]
        if ((connectFlags & 0x01) != 0) {
            throw new MalformedPacketException("Reserved flag in the CONNECT packet must be set to 0", PacketType.CONNECT, ConnAck.ConnectReasonCode.MALFORMED_PACKET.getIntValue());
        }
        return connectFlags;
    }

    private static QoS decodeWillQoS(final byte connectFlags) {
        int willQos = (connectFlags >> WILL_QOS) & 0x03;
        if (willQos > 2) {
            throw new MalformedPacketException("Will QoS must be 0, 1 or 2", PacketType.CONNECT, ConnAck.ConnectReasonCode.MALFORMED_PACKET.getIntValue());
        }
        if (willQos == 2) {
            throw new MalformedPacketException("Will QoS 2 not supported", PacketType.CONNECT, ConnAck.ConnectReasonCode.QOS_NOT_SUPPORTED.getIntValue());
        }
        return QoS.fromIntValue(willQos);
    }

    private static void validateConnectFlags(byte connectFlags, QoS willQos) {
        // If the Will Flag is set to 0, then the Will QoS MUST be set to 0 (0x00) [MQTT-3.1.2-11]
        if (!hasWillFlag(connectFlags) && willQos != QoS.AT_MOST_ONCE) {
            throw new MalformedPacketException("Will QoS must be 0 if the Will Flag is not set", PacketType.CONNECT, ConnAck.ConnectReasonCode.MALFORMED_PACKET.getIntValue());
        }

        // If the Will Flag is set to 0, then Will Retain MUST be set to 0 [MQTT-3.1.2-13]
        if (!hasWillFlag(connectFlags) && hasWillRetainFlag(connectFlags)) {
            throw new MalformedPacketException("If the Will Flag is set to 0, then Will Retain MUST be set to 0", PacketType.CONNECT, ConnAck.ConnectReasonCode.MALFORMED_PACKET.getIntValue());
        }
    }

    private static int decodeKeepAlive(ByteBuffer buffer) {
        return ByteBufferUtil.decodeTwoByteInteger(buffer);
    }

    private static int decodePropertyLength(ByteBuffer buffer) {
        final var variableLengthDecoder = new VariableByteIntegerDecoder();
        boolean finished = variableLengthDecoder.decode(buffer);
        if (!finished) {
            throw new IllegalArgumentException("Invalid property length in CONNECT packet");
        }
        return variableLengthDecoder.getValue();
    }

    private static String decodeClientId(final ByteBuffer buffer) {
        var clientId = ByteBufferUtil.decodeUTF8String(buffer);
        return clientId;
    }

    private static String decodeUsername(final ByteBuffer buffer, final byte connectFlags) {
        if (hasUsernameFlag(connectFlags)) {
            return ByteBufferUtil.decodeUTF8String(buffer);
        }
        return null;
    }

    private static WillData decodeWillData(ByteBuffer buffer, QoS willQos) {
        final var genericWillProperties = PropertiesDecoder.decode(buffer);
        final var willProperties = WillProperties.fromGenericProperties(genericWillProperties);
        final String willTopic = ByteBufferUtil.decodeUTF8String(buffer);
        final ByteBuffer willPayload = ByteBufferUtil.extractBinaryDataAsByteBuffer(buffer);
        return new WillData(willQos, willProperties, willTopic, willPayload);
    }

    private static boolean hasCleanStartFlag(final byte flags) {
        return ((flags >> CLEAN_START) & 0x01) == 1;
    }

    private static boolean hasWillFlag(final byte flags) {
        return ((flags >> WILL_FLAG) & 0x01) == 1;
    }

    private static boolean hasUsernameFlag(final byte flags) {
        return ((flags >> USERNAME_FLAG) & 0x01) == 1;
    }

    private static boolean hasPasswordFlag(final byte flags) {
        return ((flags >> PASSWORD_FLAG) & 0x01) == 1;
    }

    private static boolean hasWillRetainFlag(final byte flags) {
        return ((flags >> WILL_RETAIN) & 0x01) == 1;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public Optional<ConnectProperties> getConnectProperties() {
        return Optional.ofNullable(connectProperties);
    }

    public String getClientId() {
        return clientId;
    }

    public Optional<String> getUserName() {
        return Optional.ofNullable(userName);
    }

    public Optional<byte[]> getPassword() {
        return Optional.ofNullable(password);
    }

    public boolean hasCleanStartFlag() {
        return hasCleanStartFlag(connectFlags);
    }

    public boolean hasWillFlag() {
        return hasWillFlag(connectFlags);
    }

    private boolean hasWillRetain(final byte flags) {
        return ((flags >> WILL_RETAIN) & 0x01) == 1;
    }

    public boolean hasWillRetain() {
        return hasWillRetain(connectFlags);
    }

    public QoS getWillQoS() {
        return willQos;
    }

    @Override
    protected int getEncodedVariableHeaderLength() {
        throw new IllegalArgumentException("not implemented");
    }

    @Override
    protected int getEncodedPayloadLength() {
        throw new IllegalArgumentException("not implemented");
    }

    @Override
    protected void encodeVariableHeader(ByteBuffer buffer) {
        throw new IllegalArgumentException("not implemented");
    }

    @Override
    protected ByteBuffer encodePayload() {
        throw new UnsupportedOperationException("not implemented");
    }

    public Optional<WillData> getWillData() {
        return Optional.ofNullable(willData);
    }
}
