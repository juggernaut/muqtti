package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.packet.Publish;
import com.github.juggernaut.macchar.packet.Utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ameya
 */
public class SubscriptionManager {

    // Map from topic filter to subscription state
    private final ConcurrentMap<String, SubscriptionState> subscriptions = new ConcurrentHashMap<>();

    public SubscriptionState getOrCreateSubscription(final String topicFilter) {
        assert topicFilter != null;
        return subscriptions.computeIfAbsent(topicFilter, filter -> new SubscriptionState());
    }

    public void onPublishReceived(final Publish msg) {
        subscriptions.forEach((filter, state) -> {
            if (Utils.doesSubscriptionMatchTopic(filter, msg.getTopicName())) {
                state.onPublishReceived(msg);
            }
        });
    }


}
