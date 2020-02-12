package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.packet.Publish;

import java.util.Iterator;

/**
 * @author ameya
 */
public class SharedSubscriptionState extends SubscriptionState {

    private final Cursor cursor;
    private int roundRobinPos = 0;

    public SharedSubscriptionState() {
        super();
        this.cursor = newCursor();
    }

    @Override
    protected void fanoutQoS0(Publish msg) {
        final var listener = selectListener();
        if (listener != null) {
            listener.onMatchedQoS0Message(msg);
        }
    }

    private SubscriptionListener selectListener() {
        // CoW array list provides a snapshot capability for an iterator, so use that
        final var listenerIterator = listeners.iterator();
        int current = 0;
        SubscriptionListener listener = null;
        while (listenerIterator.hasNext() && current != roundRobinPos) {
            listener = listenerIterator.next();
            current++;
        }
        if (listenerIterator.hasNext()) {
            roundRobinPos++;
        } else {
            roundRobinPos = 0;
        }
        return listener;
    }

    @Override
    protected void fanoutQoS1(Publish msg) {
        final var listener = selectListener();
        if (listener != null) {
            if (listener.getSubscriptionMaxQoS() == QoS.AT_MOST_ONCE) {
                listener.onMatchedQoS0Message(msg);
            } else {
                listener.onMatchedQoS1Message();
            }
        }
    }

    @Override
    protected void save(Publish msg) {
        synchronized(this) {
            final int newPos = messageBuffer.put(msg);
            // TODO: this logic is wrong! (off by one error)
            if (newPos == cursor.getPosition()) {
                cursor.invalidate();
            }
        }
        fanoutQoS1(msg);
    }
}
