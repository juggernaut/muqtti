package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.ByteBufferUtil;
import com.github.juggernaut.macchar.VariableByteIntegerDecoder;
import com.github.juggernaut.macchar.property.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.juggernaut.macchar.property.PropertyIdentifiers.*;

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

    private final List<MqttProperty> rawProperties;

    private ConnectProperties(List<MqttProperty> rawProperties) {
        this.rawProperties = rawProperties;
    }

    public SessionExpiryInterval getSessionExpiryInterval() {
        // 3.1.2.11.2: If the Session Expiry Interval is absent the value 0 is used
        return Utils.extractProperty(SessionExpiryInterval.class, rawProperties)
                .orElseGet(() -> new SessionExpiryInterval(0));
    }

    public ReceiveMaximum getReceiveMaximum() {
        // 3.1.2.11.3: If the Receive Maximum value is absent then its value defaults to 65,535
        return Utils.extractProperty(ReceiveMaximum.class, rawProperties)
                .orElseGet(() -> new ReceiveMaximum(65535));
    }

    public Optional<MaximumPacketSize> getMaximumPacketSize() {
        return Utils.extractProperty(MaximumPacketSize.class, rawProperties);
    }

    public TopicAliasMaximum getTopicAliasMaximum() {
        // 3.1.2.11.5: If the Topic Alias Maximum property is absent, the default value is 0.
        return Utils.extractProperty(TopicAliasMaximum.class, rawProperties)
                .orElseGet(() -> new TopicAliasMaximum(0));
    }

    public RequestResponseInformation getRequestResponseInformation() {
        // 3.1.2.11.6:  If the Request Response Information is absent, the value of 0 is used
        return Utils.extractProperty(RequestResponseInformation.class, rawProperties)
                .orElseGet(() -> new RequestResponseInformation((byte) 0));
    }

    public RequestProblemInformation getRequestProblemInformation() {
        // 3.1.2.11.7: If the Request Problem Information is absent, the value of 1 is used
        return Utils.extractProperty(RequestProblemInformation.class, rawProperties)
                .orElseGet(() -> new RequestProblemInformation((byte) 1));
    }


    public List<UserProperty> getUserProperties() {
        return Utils.extractUserProperties(rawProperties);
    }

    public Optional<AuthenticationMethod> getAuthenticationMethod() {
        return Utils.extractProperty(AuthenticationMethod.class, rawProperties);
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
                    sessionExpiryInterval = ByteBufferUtil.decodeFourByteInteger(buffer);
                    break;
                case RECEIVE_MAXIMUM:
                    // 3.1.2.11.3: It is a Protocol Error to include the Receive Maximum value more than once or for it to have the value 0
                    if (receiveMaximum != -1) {
                        throw new IllegalArgumentException("REceive Maximum property canot be specified more than once");
                    }
                    receiveMaximum = ByteBufferUtil.decodeTwoByteInteger(buffer);
                    if (receiveMaximum == 0) {
                        throw new IllegalArgumentException("Receive Maximum proprety cannot be 0");
                    }
                    break;
                case MAXIMUM_PACKET_SIZE:
                    // 3.1.2.11.4: It is a Protocol Error to include the Maximum Packet Size more than once, or for the value to be set to zero
                    if (maximumPacketSize != -1) {
                        throw new IllegalArgumentException("Maximum packet size cannot be specified more than once");
                    }
                    maximumPacketSize = ByteBufferUtil.decodeFourByteInteger(buffer);
                    if (maximumPacketSize == 0) {
                        throw new IllegalArgumentException("Maximum packet size cannot be 0");
                    }
                    break;
                case TOPIC_ALIAS_MAXIMUM:
                    // 3.1.2.11.5: It is a Protocol Error to include the Topic Alias Maximum value more than once
                    if (topicAliasMaximum != -1) {
                        throw new IllegalArgumentException("Topic alias maximum cannot be specified more than once");
                    }
                    topicAliasMaximum = ByteBufferUtil.decodeTwoByteInteger(buffer);
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
                case REQUEST_PROBLEM_INFORMATION:
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
                    authenticationMethod = ByteBufferUtil.decodeUTF8String(buffer);
                    break;
                case AUTHENTICATION_DATA:
                    if (authenticationData != null) {
                        throw new IllegalArgumentException("authenication data cannot be sepcifid more than once");
                    }
                    authenticationData = ByteBufferUtil.decodeBinaryData(buffer);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown property for CONNECT packet");
            }

        }
    }
}
