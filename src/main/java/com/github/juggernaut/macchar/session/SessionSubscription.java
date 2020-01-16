package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.QoS;
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
     * The server-side cursor for this subscription; present only if this is a QoS1 subscription
     */
    private final Optional<Cursor> cursor;

    private SessionSubscription(Optional<Integer> subscriptionIdentifier, Subscribe.Subscription subscription, Session session,
                                final SubscriptionState subscriptionState, Optional<Cursor> cursor) {
        this.subscriptionIdentifier = subscriptionIdentifier;
        this.subscription = subscription;
        this.session = session;
        this.subscriptionState = subscriptionState;
        this.cursor = cursor;
    }

    public static SessionSubscription from(Subscribe.Subscription subscription, Optional<Integer> subscriptionIdentifier,
                                           final Session session, final SubscriptionState subscriptionState) {
        final var cursor = subscription.getQoS() == QoS.AT_LEAST_ONCE ? subscriptionState.newCursor() : null;
        final var sessionSubscription = new SessionSubscription(subscriptionIdentifier, subscription, session, subscriptionState, Optional.ofNullable(cursor));
        subscriptionState.addListener(sessionSubscription);
        return sessionSubscription;
    }

    @Override
    public void onMatchedQoS0Message(Publish msg) {
        session.getActor().sendMessage(new SendQoS0PublishEvent(msg));
    }

    @Override
    public void onMatchedQoS1Message() {
        assert subscription.getQoS() == QoS.AT_LEAST_ONCE;
        session.getActor().sendMessage(new QoS1PublishMatchedEvent());
    }

    public void readQoS1Messages(final List<Publish> messages, final int maxMessages) {
        assert cursor.isPresent();
        subscriptionState.readQoS1Messages(cursor.get(), maxMessages, messages);
    }

    public void deactivate() {
        subscriptionState.removeListener(this);
    }

    public void delete() {
        cursor.ifPresent(subscriptionState::deleteCursor);
    }

    public void reactivate() {
        subscriptionState.addListener(this);
        // TODO: handle an invalid cursor here
    }

    @Override
    public QoS getSubscriptionMaxQoS() {
        return subscription.getQoS();
    }
}
