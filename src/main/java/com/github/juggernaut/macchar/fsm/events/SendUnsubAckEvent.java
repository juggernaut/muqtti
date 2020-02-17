package com.github.juggernaut.macchar.fsm.events;

import com.github.juggernaut.macchar.packet.UnsubAck;

/**
 * @author ameya
 */
public class SendUnsubAckEvent implements Event {

    private final UnsubAck unsubAck;

    public SendUnsubAckEvent(UnsubAck unsubAck) {
        this.unsubAck = unsubAck;
    }

    public UnsubAck getUnsubAck() {
        return unsubAck;
    }
}
