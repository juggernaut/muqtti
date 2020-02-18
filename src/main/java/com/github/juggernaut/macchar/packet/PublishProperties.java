package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.property.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ameya
 */
public class PublishProperties {

    private final List<MqttProperty> rawProperties;

    private static final Set<Class<? extends MqttProperty>> EXPECTED_TYPES = Set.of(
            PayloadFormatIndicator.class,
            MessageExpiryInterval.class,
            TopicAlias.class,
            ResponseTopic.class,
            CorrelationData.class,
            UserProperty.class,
            SubscriptionIdentifier.class,
            ContentType.class
    );

    private static final Set<Class<? extends MqttProperty>> FORWARD_UNALTERED_PROPERTY_TYPES = Set.of(
            PayloadFormatIndicator.class,
            ResponseTopic.class,
            CorrelationData.class,
            UserProperty.class,
            ContentType.class
    );

    private PublishProperties(List<MqttProperty> rawProperties) {
        this.rawProperties = rawProperties;
    }

    public static PublishProperties fromRawProperties(final List<MqttProperty> rawProperties) {
        Utils.validatePropertyTypes(rawProperties, EXPECTED_TYPES, MqttPacket.PacketType.PUBLISH);
        return new PublishProperties(rawProperties);
    }

    public static PublishProperties emptyProperties() {
        return new PublishProperties(Collections.emptyList());
    }

    public Optional<PayloadFormatIndicator> getPayloadFormatIndicator() {
        return Utils.extractProperty(PayloadFormatIndicator.class, rawProperties);
    }

    public Optional<MessageExpiryInterval> getMessageExpiryInterval() {
        return Utils.extractProperty(MessageExpiryInterval.class, rawProperties);
    }

    public Optional<TopicAlias> getTopicAlias() {
        return Utils.extractProperty(TopicAlias.class, rawProperties);
    }

    public Optional<ResponseTopic> getResponseTopic() {
        return Utils.extractProperty(ResponseTopic.class, rawProperties);
    }

    public Optional<CorrelationData> getCorrelationData() {
        return Utils.extractProperty(CorrelationData.class, rawProperties);
    }

    public List<UserProperty> getUserProperties() {
        return Utils.extractUserProperties(rawProperties);
    }

    public Optional<SubscriptionIdentifier> getSubscriptionIdentifier() {
        return Utils.extractProperty(SubscriptionIdentifier.class, rawProperties);
    }

    public Optional<ContentType> getContentType() {
        return Utils.extractProperty(ContentType.class, rawProperties);
    }

    public List<MqttProperty> getRawProperties() {
        return rawProperties;
    }

    public PublishProperties getPropertiesToForwardUnaltered() {
        final var rawUnaltered = rawProperties.stream()
                .filter(prop -> FORWARD_UNALTERED_PROPERTY_TYPES.contains(prop.getClass()))
                .collect(Collectors.toList());
        return new PublishProperties(rawUnaltered);
    }
}
