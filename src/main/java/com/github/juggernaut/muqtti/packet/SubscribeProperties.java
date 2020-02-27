package com.github.juggernaut.muqtti.packet;

import com.github.juggernaut.muqtti.property.MqttProperty;
import com.github.juggernaut.muqtti.property.SubscriptionIdentifier;
import com.github.juggernaut.muqtti.property.UserProperty;

import java.util.List;
import java.util.Optional;

/**
 * @author ameya
 */
public class SubscribeProperties {


    private final Optional<SubscriptionIdentifier> subscriptionIdentifier;
    private final List<UserProperty> userProperties;

    public SubscribeProperties(Optional<SubscriptionIdentifier> subscriptionIdentifier, List<UserProperty> userProperties) {
        this.subscriptionIdentifier = subscriptionIdentifier;
        this.userProperties = userProperties;
    }

    public static SubscribeProperties fromRawProperties(List<MqttProperty> properties) {
        assert properties != null;
        final var subscriptionId = Utils.extractProperty(SubscriptionIdentifier.class, properties);
        final var userProps = Utils.extractUserProperties(properties);
        return new SubscribeProperties(subscriptionId, userProps);
    }

    public Optional<SubscriptionIdentifier> getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    public List<UserProperty> getUserProperties() {
        return userProperties;
    }
}
