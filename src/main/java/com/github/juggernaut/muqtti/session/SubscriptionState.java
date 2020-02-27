package com.github.juggernaut.muqtti.session;

import com.github.juggernaut.muqtti.packet.QoS;
import com.github.juggernaut.muqtti.packet.Publish;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ameya
 */
public class SubscriptionState {


    private final SubscriptionId subscriptionId;
    private final CircularBuffer<MessageEntry> messageBuffer = new CircularBuffer<>(1024);
    private final List<Cursor> cursors = new CopyOnWriteArrayList<>();
    private final List<SubscriptionListener> listeners = new CopyOnWriteArrayList<>();

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
            final int newPos = messageBuffer.put(MessageEntry.forReceivedMessage(msg));
            cursors.forEach(cursor -> {
                // The write pos has wrapped around and bumped against our cursor, so invalidate it.
                // TODO: this logic is wrong! (off by one error)
                if (newPos == cursor.getPosition()) {
                    cursor.invalidate();
                }
            });
        }
        fanoutQoS1(msg);
    }

    public synchronized void readQoS1Messages(Cursor cursor, int numMessages, List<MessageEntry> entries) {
        assert numMessages > 0;
        if (!cursor.isValid()) {
            throw new IllegalStateException("Cursor " + cursor + " is invalidated, session state needs to be invalidated as well");
        }
        final int readUntilPos = messageBuffer.take(cursor.getPosition(), entries, entry -> !entry.isExpired());
        cursor.setPosition(readUntilPos);
    }

    private void fanoutQoS0(Publish msg) {
        listeners.forEach(listener -> listener.onMatchedQoS0Message(msg));
    }

    private void fanoutQoS1(Publish msg) {
        listeners.forEach(listener -> {
            // subscription is QoS0, so just send it directly
            if (listener.getSubscriptionMaxQoS() == QoS.AT_MOST_ONCE) {
                listener.onMatchedQoS0Message(msg);
            } else {
                listener.onMatchedQoS1Message();
            }
        });
    }

    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }
}
