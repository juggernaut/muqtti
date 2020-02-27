package com.github.juggernaut.muqtti.session;

import com.github.juggernaut.muqtti.packet.QoS;
import com.github.juggernaut.muqtti.packet.Publish;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author ameya
 */
public class SharedSubscriptionState extends SubscriptionState implements SubscriptionListener {

    private final SubscriptionState subscriptionState;
    private final Cursor sharedCursor;
    private final AtomicInteger roundRobinPos = new AtomicInteger(0);

    private final List<SubscriptionListener> listeners = new CopyOnWriteArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(SharedSubscriptionState.class.getName());

    private SharedSubscriptionState(SubscriptionState subscriptionState, final Cursor sharedCursor) {
        super(subscriptionState.getSubscriptionId());
        this.subscriptionState = subscriptionState;
        this.sharedCursor = sharedCursor;
    }

    public static SharedSubscriptionState fromSubscriptionState(final SubscriptionState subscriptionState) {
        final var sharedSS = new SharedSubscriptionState(subscriptionState, subscriptionState.newCursor());
        subscriptionState.addListener(sharedSS);
        return sharedSS;
    }

    @Override
    public Cursor newCursor() {
        return sharedCursor;
    }

    @Override
    public void deleteCursor(Cursor cursor) {
        // TODO: should probably have a reference count here to release the shared cursor
    }

    @Override
    public void addListener(SubscriptionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(SubscriptionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public QoS getSubscriptionMaxQoS() {
        // Choose the highest QoS here, as this shared subscription itself may have listeners with either QoS0 or QoS1
        return QoS.AT_LEAST_ONCE;
    }

    @Override
    public void onMatchedQoS0Message(Publish msg) {
        final var listener = chooseListener();
        if (listener != null) {
            listener.onMatchedQoS0Message(msg);
        }
    }

    @Override
    public void onMatchedQoS1Message() {
        final var listener = chooseListener();
        if (listener != null) {
            if (listener.getSubscriptionMaxQoS() == QoS.AT_MOST_ONCE) {
                final List<MessageEntry> message = new ArrayList<>(1);
                subscriptionState.readQoS1Messages(sharedCursor, 1, message);
                if (message.isEmpty()) {
                    LOGGER.warning("Got notification for QoS1 message, but readQoS1Messages did not return it");
                } else {
                    listener.onMatchedQoS0Message(message.get(0).getMessage());
                }
            } else {
                listener.onMatchedQoS1Message();
            }
        }
    }

    private SubscriptionListener chooseListener() {
        final var listenerIterator = listeners.iterator();
        final int pos = roundRobinPos.get();
        int current = -1;
        SubscriptionListener listener = null;
        while (listenerIterator.hasNext() && current < pos) {
            listener = listenerIterator.next();
            current++;
        }
        if (listener != null) {
            if (current == pos) {
                if (listenerIterator.hasNext()) {
                    roundRobinPos.incrementAndGet();
                } else {
                    roundRobinPos.set(0);
                }
            } else {
                roundRobinPos.set(0);
            }
        }
        return listener;
    }
}
