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
     * The server-side cursor for this subscription
     */
    private final Cursor cursor;

    private SessionSubscription(Optional<Integer> subscriptionIdentifier, Subscribe.Subscription subscription, Cursor cursor) {
        this.subscriptionIdentifier = subscriptionIdentifier;
        this.subscription = subscription;
        this.cursor = cursor;
    }

    public static SessionSubscription from(Subscribe.Subscription subscription, Optional<Integer> subscriptionIdentifier,
                                           Cursor cursor) {
        return new SessionSubscription(subscriptionIdentifier, subscription, cursor);
    }
}
