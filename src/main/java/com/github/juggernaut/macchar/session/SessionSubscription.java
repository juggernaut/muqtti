package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.fsm.events.QoS1PublishMatchedEvent;
import com.github.juggernaut.macchar.fsm.events.SendQoS0PublishEvent;
import com.github.juggernaut.macchar.packet.Publish;
import com.github.juggernaut.macchar.packet.Subscribe;

import java.util.List;
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
     * The MQTT session
     */
    private final Session session;

    /**
     * The global subscription state associated with this filter
     */
    private final SubscriptionState subscriptionState;

    /**
     * The server-side cursor for this subscription
     */
    private final Cursor cursor;

    private SessionSubscription(Optional<Integer> subscriptionIdentifier, Subscribe.Subscription subscription, Session session,
                                final SubscriptionState subscriptionState, Cursor cursor) {
        this.subscriptionIdentifier = subscriptionIdentifier;
        this.subscription = subscription;
        this.session = session;
        this.subscriptionState = subscriptionState;
        this.cursor = cursor;
    }

    public static SessionSubscription from(Subscribe.Subscription subscription, Optional<Integer> subscriptionIdentifier,
                                           final Session session, final SubscriptionState subscriptionState) {
        final var cursor = subscriptionState.newCursor();
        final var sessionSubscription = new SessionSubscription(subscriptionIdentifier, subscription, session, subscriptionState, cursor);
        subscriptionState.addListener(sessionSubscription);
        return sessionSubscription;
    }

    @Override
    public void onMatchedQoS0Message(Publish msg) {
        session.getActor().sendMessage(new SendQoS0PublishEvent(msg));
    }

    @Override
    public void onMatchedQoS1Message() {
        session.getActor().sendMessage(new QoS1PublishMatchedEvent());
    }

    public void readQoS1Messages(final List<Publish> messages, final int maxMessages) {
        subscriptionState.readQoS1Messages(cursor, maxMessages, messages);
    }

    public void deactivate() {
        subscriptionState.removeListener(this);
    }

    public void delete() {
        subscriptionState.deleteCursor(cursor);
    }

    public void reactivate() {
        subscriptionState.addListener(this);
        // TODO: handle an invalid cursor here
    }
}
