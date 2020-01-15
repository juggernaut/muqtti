package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.Actor;
import com.github.juggernaut.macchar.fsm.events.SendQoS0PublishEvent;
import com.github.juggernaut.macchar.packet.Publish;
import com.github.juggernaut.macchar.packet.Subscribe;

import java.util.Optional;

/**
 * @author ameya
 */
public class SessionSubscription implements SubscriptionListener {

    /**
     * The identifier specified by the client in the SUBSCRIBE packet
     */
    private final Optional<Integer> subscriptionIdentifier;

    /**
     * A single subscription request in the original SUBSCRIBE packet
     */
    private final Subscribe.Subscription subscription;

    /**
     * The MQTT actor for the channel that has made this subscription
     */
    private final Actor actor;

    /**
     * The global subscription state associated with this filter
     */
    private final SubscriptionState subscriptionState;

    /**
     * The server-side cursor for this subscription
     */
    private final Cursor cursor;

    private SessionSubscription(Optional<Integer> subscriptionIdentifier, Subscribe.Subscription subscription, Actor actor,
                                final SubscriptionState subscriptionState, Cursor cursor) {
        this.subscriptionIdentifier = subscriptionIdentifier;
        this.subscription = subscription;
        this.actor = actor;
        this.subscriptionState = subscriptionState;
        this.cursor = cursor;
    }

    public static SessionSubscription from(Subscribe.Subscription subscription, Optional<Integer> subscriptionIdentifier,
                                           final Actor actor, final SubscriptionState subscriptionState) {
        final var cursor = subscriptionState.newCursor();
        final var sessionSubscription = new SessionSubscription(subscriptionIdentifier, subscription, actor, subscriptionState, cursor);
        subscriptionState.addListener(sessionSubscription);
        return sessionSubscription;
    }

    @Override
    public void onMatchedQoS0Message(Publish msg) {
        actor.sendMessage(new SendQoS0PublishEvent(msg));
    }

    @Override
    public void onMatchedQoS1Message() {
        // TODO
    }

    public void deactivate() {
        subscriptionState.removeListener(this);
    }
}
