package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.packet.Subscribe;

import java.util.Optional;

/**
 * @author ameya
 */
public class SessionSubscription {

    /**
     * The identifier specified by the client in the SUBSCRIBE packet
     */
    private final Optional<Integer> subscriptionIdentifier;

    /**
     * A single subscription request in the original SUBSCRIBE packet
     */
    private final Subscribe.Subscription subscription;

    /**
     * The internal global subscription state
     */
    private final SubscriptionState subscriptionState;

    private SessionSubscription(Optional<Integer> subscriptionIdentifier, Subscribe.Subscription subscription, SubscriptionState subscriptionState) {
        this.subscriptionIdentifier = subscriptionIdentifier;
        this.subscription = subscription;
        this.subscriptionState = subscriptionState;
    }

    public static SessionSubscription from(Subscribe.Subscription subscription, Optional<Integer> subscriptionIdentifier,
                                           SubscriptionState subscriptionState) {
        return new SessionSubscription(subscriptionIdentifier, subscription, subscriptionState);
    }
}
