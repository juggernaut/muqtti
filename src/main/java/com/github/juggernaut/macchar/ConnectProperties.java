package com.github.juggernaut.macchar;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.juggernaut.macchar.PropertyIdentifiers.*;

/**
 * @author ameya
 */
public class ConnectProperties {

    private long sessionExpiryInterval = -1;
    private int receiveMaximum = -1;
    private long maximumPacketSize = - 1;
    private int topicAliasMaximum = -1;
    private int requestResponseInformation = -1;
    private int requestProblemInformation = -1;
    private List<UserProperty> userProperties = new ArrayList<>();
    private String authenticationMethod;
    private byte[] authenticationData;

    public long getSessionExpiryInterval() {
        // 3.1.2.11.2: If the Session Expiry Interval is absent the value 0 is used
        return sessionExpiryInterval == -1 ? 0 : sessionExpiryInterval;
    }

    public int getReceiveMaximum() {
        // 3.1.2.11.3: If the Receive Maximum value is absent then its value defaults to 65,535
        return receiveMaximum == -1 ? 65535 : receiveMaximum;
    }

    public Optional<Long> getMaximumPacketSize() {
        return maximumPacketSize == -1 ? Optional.empty() : Optional.of(maximumPacketSize);
    }

    public int getTopicAliasMaximum() {
        // 3.1.2.11.5: If the Topic Alias Maximum property is absent, the default value is 0.
        return topicAliasMaximum == -1 ? 0 : topicAliasMaximum;
    }

    public List<UserProperty> getUserProperties() {
        return userProperties;
    }

    public void decodeFromBuffer(final ByteBuffer buffer) {
        final var variableLengthDecoder = new VariableByteIntegerDecoder();
        while (buffer.hasRemaining()) {
            if (!variableLengthDecoder.decode(buffer)) {
                throw new IllegalArgumentException("Invalid variable length value in properties of CONNECT");
            }
            final int propertyIdentifier = variableLengthDecoder.getValue();
            variableLengthDecoder.reset();
            switch(propertyIdentifier) {
                case SESSION_EXPIRY_INTERVAL:
                    if (sessionExpiryInterval != -1) {
                        throw new IllegalArgumentException("Session exprity cannot be specified more than once");
                    }
                    sessionExpiryInterval = ByteBufferUtil.getFourByteLength(buffer);
                    break;
                case RECEIVE_MAXIMUM:
                    // 3.1.2.11.3: It is a Protocol Error to include the Receive Maximum value more than once or for it to have the value 0
                    if (receiveMaximum != -1) {
                        throw new IllegalArgumentException("REceive Maximum property canot be specified more than once");
                    }
                    receiveMaximum = ByteBufferUtil.getTwoByteLength(buffer);
                    if (receiveMaximum == 0) {
                        throw new IllegalArgumentException("Receive Maximum proprety cannot be 0");
                    }
                    break;
                case MAXIMUM_PACKET_SIZE:
                    // 3.1.2.11.4: It is a Protocol Error to include the Maximum Packet Size more than once, or for the value to be set to zero
                    if (maximumPacketSize != -1) {
                        throw new IllegalArgumentException("Maximum packet size cannot be specified more than once");
                    }
                    maximumPacketSize = ByteBufferUtil.getFourByteLength(buffer);
                    if (maximumPacketSize == 0) {
                        throw new IllegalArgumentException("Maximum packet size cannot be 0");
                    }
                    break;
                case TOPIC_ALIAS_MAXIMUM:
                    // 3.1.2.11.5: It is a Protocol Error to include the Topic Alias Maximum value more than once
                    if (topicAliasMaximum != -1) {
                        throw new IllegalArgumentException("Topic alias maximum cannot be specified more than once");
                    }
                    topicAliasMaximum = ByteBufferUtil.getTwoByteLength(buffer);
                    break;
                case REQUEST_RESPONSE_INFORMATION:
                    // 3.1.2.11.6: It is Protocol Error to include the Request Response Information more than once, or to have a value other than 0 or 1
                    if (requestResponseInformation != -1) {
                        throw new IllegalArgumentException("Requst response information cannot be specified more than once");
                    }
                    requestResponseInformation = buffer.get();
                    if (requestResponseInformation != 0 && requestResponseInformation != 1) {
                        throw new IllegalArgumentException("Requst response information can only be 0 or 1");
                    }
                    break;
                case REQUEST_PROBLEM_INFOMRATION:
                    // 3.1.2.11.6: It is Protocol Error to include the Request Response Information more than once, or to have a value other than 0 or 1
                    if (requestProblemInformation != -1) {
                        throw new IllegalArgumentException("Requst problem information cannot be specified more than once");
                    }
                    requestProblemInformation = buffer.get();
                    if (requestProblemInformation != 0 && requestProblemInformation != 1) {
                        throw new IllegalArgumentException("Requst problem information can only be 0 or 1");
                    }
                    break;
                case USER_PROPERTY:
                    // 3.1.2.11.8: The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same name is allowed to appear more than once
                    final var userProperty = UserProperty.fromBuffer(buffer);
                    userProperties.add(userProperty);
                    break;
                case AUTHENTICATION_METHOD:
                    // 3.1.2.11.9: It is a Protocol Error to include Authentication Method more than once
                    if (authenticationMethod != null) {
                        throw new IllegalArgumentException("Authentication method cannot be speicified more than once");
                    }
                    authenticationMethod = ByteBufferUtil.getUTF8String(buffer);
                    break;
                case AUTHENTICATION_DATA:
                    if (authenticationData != null) {
                        throw new IllegalArgumentException("authenication data cannot be sepcifid more than once");
                    }
                    authenticationData = ByteBufferUtil.getBinaryData(buffer);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown property for CONNECT packet");
            }

        }
    }
}
