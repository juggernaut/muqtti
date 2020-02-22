package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.TopicFilter;
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

    // Map from share name+topic filter to shared subscription state
    private final ConcurrentMap<String, SharedSubscriptionState> sharedSubscriptions = new ConcurrentHashMap<>();

    public SubscriptionState getOrCreateSubscription(final TopicFilter topicFilter) {
        assert topicFilter != null;
        final var subscriptionState = subscriptions.computeIfAbsent(topicFilter.getFilterString(), filter -> new SubscriptionState());
        if (topicFilter.isShared()) {
            assert topicFilter.getShareName().isPresent();
            final String key = topicFilter.getShareName().get() + "/" + topicFilter.getFilterString();
            return sharedSubscriptions.computeIfAbsent(key, k -> SharedSubscriptionState.fromSubscriptionState(subscriptionState));
        }
        return subscriptionState;
    }

    public void onPublishReceived(final Publish msg) {
        subscriptions.forEach((filter, state) -> {
            if (Utils.doesSubscriptionMatchTopic(filter, msg.getTopicName())) {
                state.onPublishReceived(msg);
            }
        });
    }


}
