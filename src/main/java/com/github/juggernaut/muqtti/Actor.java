package com.github.juggernaut.muqtti;

import com.github.juggernaut.muqtti.fsm.events.Event;

/**
 * @author ameya
 */
public interface Actor {

    String getId();
    void sendMessage(Event event);
    void destroy();
}
