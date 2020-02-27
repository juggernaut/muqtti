package com.github.juggernaut.muqtti.fsm.events;

import com.github.juggernaut.muqtti.packet.Disconnect;

/**
 * @author ameya
 */
public class SendDisconnectEvent implements Event {

    private final Disconnect msg;

    public SendDisconnectEvent(Disconnect msg) {
        this.msg = msg;
    }

    public Disconnect getMsg() {
        return msg;
    }
}
