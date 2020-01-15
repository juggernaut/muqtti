package com.github.juggernaut.macchar.fsm.events;

import com.github.juggernaut.macchar.packet.Publish;

/**
 * @author ameya
 */
public class SendQoS0PublishEvent implements Event {

    private final Publish msg;

    public SendQoS0PublishEvent(Publish msg) {
        this.msg = msg;
    }

    public Publish getMsg() {
        return msg;
    }
}
