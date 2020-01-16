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

    public void deleteCursor(Cursor cursor) {
        cursors.remove(cursor);
    }

    public void addListener(final SubscriptionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final SubscriptionListener listener) {
        listeners.remove(listener);
    }

    public void onPublishReceived(Publish msg) {
        if (msg.getQoS() == QoS.AT_MOST_ONCE) {
            fanoutQoS0(msg);
        } else {
            save(msg);
        }
    }

    private void save(Publish msg) {
        synchronized(this) {
            final int newPos = messageBuffer.put(msg);
            cursors.forEach(cursor -> {
                // The write pos has wrapped around and bumped against our cursor, so invalidate it.
                // TODO: this logic is wrong! (off by one error)
                if (newPos == cursor.getPosition()) {
                    cursor.invalidate();
                }
            });
        }
        fanoutQoS1();
    }

    public synchronized void readQoS1Messages(Cursor cursor, int numMessages, List<Publish> messages) {
        assert numMessages > 0;
        if (!cursor.isValid()) {
            throw new IllegalStateException("Cursor " + cursor + " is invalidated, session state needs to be invalidated as well");
        }
        final int readUntilPos = messageBuffer.take(cursor.getPosition(), messages);
        cursor.setPosition(readUntilPos);
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
