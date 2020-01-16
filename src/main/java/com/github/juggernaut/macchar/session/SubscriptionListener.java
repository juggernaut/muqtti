package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.packet.Publish;

/**
 * @author ameya
 */
public interface SubscriptionListener {

    QoS getSubscriptionMaxQoS();

    void onMatchedQoS0Message(Publish msg);

    /**
     * QoS 1 messages are ordered, so the client is expected to pull instead of messages being pushed to it;
     * this callback only serves as an event that a new QoS 1 message has arrived matching the subscription
     */
    void onMatchedQoS1Message();
}
