package com.github.juggernaut.muqtti.session;

import java.util.Objects;
import java.util.UUID;

/**
 * This is an internal subscription id; unrelated to the Subscription Identifiers in the MQTT spec
 *
 * @author ameya
 */
public class SubscriptionId {

    private final String id;

    public SubscriptionId(String id) {
        this.id = id;
    }

    public SubscriptionId() {
        this(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionId that = (SubscriptionId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
