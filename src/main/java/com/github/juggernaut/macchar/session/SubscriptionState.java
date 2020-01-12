package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.packet.Publish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ameya
 */
public class SubscriptionState {


    private final SubscriptionId subscriptionId;
    private final CircularBuffer<Publish> messageBuffer = new CircularBuffer<>(1024);
    private final List<Cursor> cursors = new ArrayList<>();
    private final List<SubscriptionListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public SubscriptionState(SubscriptionId subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public SubscriptionState() {
        this(new SubscriptionId());
    }

    public Cursor newCursor() {
        final var cursor = new Cursor(subscriptionId, messageBuffer.getPosition() - 1);
        cursors.add(cursor);
        return cursor;
    }

    public void addListener(final SubscriptionListener listener) {
        listeners.add(listener);
    }

    public void removeConsumer(final SubscriptionListener listener) {
        listeners.remove(listener);
    }

    public void onPublishReceived(Publish msg) {
        if (msg.getQoS() == QoS.AT_MOST_ONCE) {
            fanoutQoS0(msg);
        } else {
            save(msg);
        }
    }

    private synchronized void save(Publish msg) {
        final int newPos = messageBuffer.add(msg);
        cursors.forEach(cursor -> {
            // The write pos has gone just past our cursor which means there is no valid
            // data to read for the cursor, so invalidate it.
            if (newPos == cursor.getPosition() + 1) {
                cursor.invalidate();
            }
        });
        fanoutQoS1();
    }

    private void fanoutQoS0(Publish msg) {
        listeners.forEach(listener -> listener.onMatchedQoS0Message(msg));
    }

    private void fanoutQoS1() {
        listeners.forEach(SubscriptionListener::onMatchedQoS1Message);
    }

    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }
}
