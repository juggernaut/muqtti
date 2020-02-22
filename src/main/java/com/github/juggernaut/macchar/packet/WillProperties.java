package com.github.juggernaut.macchar.packet;

import com.github.juggernaut.macchar.exception.DecodingException;
import com.github.juggernaut.macchar.property.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author ameya
 */
public class WillProperties {

    private final WillDelayInterval willDelayInterval;
    private final PayloadFormatIndicator payloadFormatIndicator;
    private final MessageExpiryInterval messageExpiryInterval;
    private final ContentType contentType;
    private final ResponseTopic responseTopic;
    private final CorrelationData correlationData;
    private final List<UserProperty> userProperties;

    private WillProperties(WillDelayInterval willDelayInterval, PayloadFormatIndicator payloadFormatIndicator, MessageExpiryInterval messageExpiryInterval, ContentType contentType, ResponseTopic responseTopic, CorrelationData correlationData, List<UserProperty> userProperties) {
        this.willDelayInterval = willDelayInterval;
        this.payloadFormatIndicator = payloadFormatIndicator;
        this.messageExpiryInterval = messageExpiryInterval;
        this.contentType = contentType;
        this.responseTopic = responseTopic;
        this.correlationData = correlationData;
        this.userProperties = userProperties;
    }

    public static WillProperties fromGenericProperties(final List<MqttProperty> properties) {
        WillDelayInterval willDelayInterval = null;
        PayloadFormatIndicator payloadFormatIndicator = null;
        MessageExpiryInterval messageExpiryInterval = null;
        ContentType contentType = null;
        ResponseTopic responseTopic = null;
        CorrelationData correlationData = null;
        final List<UserProperty> userProperties = new ArrayList<>();
        for (MqttProperty prop: properties) {
            if (prop instanceof WillDelayInterval) {
                willDelayInterval = (WillDelayInterval) prop;
            } else if (prop instanceof PayloadFormatIndicator) {
                payloadFormatIndicator = (PayloadFormatIndicator) prop;
            } else if (prop instanceof MessageExpiryInterval) {
                messageExpiryInterval = (MessageExpiryInterval) prop;
            } else if (prop instanceof ContentType) {
                contentType = (ContentType) prop;
            } else if (prop instanceof ResponseTopic) {
                responseTopic = (ResponseTopic) prop;
            } else if (prop instanceof CorrelationData) {
                correlationData = (CorrelationData) prop;
            } else if (prop instanceof UserProperty) {
                userProperties.add((UserProperty) prop);
            } else {
                throw new DecodingException("Unknown Will property type encountered");
            }
        }
        return new WillProperties(willDelayInterval, payloadFormatIndicator, messageExpiryInterval, contentType,
                responseTopic, correlationData, userProperties);

    }

    public Optional<WillDelayInterval> getWillDelayInterval() {
        return Optional.ofNullable(willDelayInterval);
    }

    public Optional<PayloadFormatIndicator> getPayloadFormatIndicator() {
        return Optional.ofNullable(payloadFormatIndicator);
    }

    public Optional<MessageExpiryInterval> getMessageExpiryInterval() {
        return Optional.of(messageExpiryInterval);
    }

    public Optional<ContentType> getContentType() {
        return Optional.ofNullable(contentType);
    }

    public Optional<ResponseTopic> getResponseTopic() {
        return Optional.ofNullable(responseTopic);
    }

    public Optional<CorrelationData> getCorrelationData() {
        return Optional.ofNullable(correlationData);
    }

    public List<UserProperty> getUserProperties() {
        return userProperties;
    }
}
