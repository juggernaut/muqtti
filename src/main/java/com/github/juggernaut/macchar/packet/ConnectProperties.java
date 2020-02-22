package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.property.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author ameya
 */
public class ConnectProperties {

    private final List<MqttProperty> rawProperties;

    private static final Set<Class<? extends MqttProperty>> EXPECTED_TYPES = Set.of(
            SessionExpiryInterval.class,
            ReceiveMaximum.class,
            MaximumPacketSize.class,
            TopicAliasMaximum.class,
            RequestResponseInformation.class,
            RequestProblemInformation.class,
            UserProperty.class,
            AuthenticationMethod.class,
            AuthenticationData.class
    );

    private ConnectProperties(List<MqttProperty> rawProperties) {
        this.rawProperties = rawProperties;
    }

    public static ConnectProperties fromRawProperties(final List<MqttProperty> rawProperties) {
        Utils.validatePropertyTypes(rawProperties, EXPECTED_TYPES, MqttPacket.PacketType.CONNECT);
        return new ConnectProperties(rawProperties);
    }

    public Optional<SessionExpiryInterval> getSessionExpiryInterval() {
        return Utils.extractProperty(SessionExpiryInterval.class, rawProperties);
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

    public Optional<AuthenticationData> getAuthenticationData() {
        return Utils.extractProperty(AuthenticationData.class, rawProperties);
    }

}
