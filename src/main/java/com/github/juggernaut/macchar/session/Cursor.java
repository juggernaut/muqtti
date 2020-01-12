package com.github.juggernaut.macchar.session;

/**
 * @author ameya
 */
public class Cursor {

    private final SubscriptionId subscriptionId;
    private final int position;
    private volatile boolean valid = true;

    public Cursor(SubscriptionId subscriptionId, int position) {
        this.subscriptionId = subscriptionId;
        this.position = position;
    }

    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    public int getPosition() {
        return position;
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        valid = false;
    }
}
