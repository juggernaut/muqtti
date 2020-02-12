package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.packet.Publish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ameya
 */
public abstract class SubscriptionState {


    protected final SubscriptionId subscriptionId;
    protected final CircularBuffer<Publish> messageBuffer = new CircularBuffer<>(1024);
    protected final List<SubscriptionListener> listeners = new CopyOnWriteArrayList<>();

    protected SubscriptionState(SubscriptionId subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    protected Cursor newCursor() {
        return new Cursor(subscriptionId, messageBuffer.getPosition() - 1);
    }

    protected SubscriptionState() {
        this(new SubscriptionId());
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

    protected abstract void save(Publish msg);

    public synchronized void readQoS1Messages(Cursor cursor, int numMessages, List<Publish> messages) {
        assert numMessages > 0;
        if (!cursor.isValid()) {
            throw new IllegalStateException("Cursor " + cursor + " is invalidated, session state needs to be invalidated as well");
        }
        final int readUntilPos = messageBuffer.take(cursor.getPosition(), messages);
        cursor.setPosition(readUntilPos);
    }

    protected abstract void fanoutQoS0(Publish msg);
    protected abstract void fanoutQoS1(Publish msg);


    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }
}
