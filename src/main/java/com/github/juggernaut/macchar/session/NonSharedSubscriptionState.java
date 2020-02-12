package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.packet.Publish;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ameya
 */
public class NonSharedSubscriptionState extends SubscriptionState {

    private final List<Cursor> cursors = new CopyOnWriteArrayList<>();

    public Cursor newCursor() {
        final var cursor = super.newCursor();
        cursors.add(cursor);
        return cursor;
    }

    public void deleteCursor(Cursor cursor) {
        cursors.remove(cursor);
    }


    // NOTE: need no synchronization here since listeners is CoW arraylist
    @Override
    protected void fanoutQoS0(Publish msg) {
        listeners.forEach(listener -> listener.onMatchedQoS0Message(msg));
    }

    @Override
    protected void fanoutQoS1(Publish msg) {
        listeners.forEach(listener -> {
            // subscription is QoS0, so just send it directly
            if (listener.getSubscriptionMaxQoS() == QoS.AT_MOST_ONCE) {
                listener.onMatchedQoS0Message(msg);
            } else {
                listener.onMatchedQoS1Message();
            }
        });
    }

    @Override
    protected void save(Publish msg) {
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
        fanoutQoS1(msg);
    }
}
