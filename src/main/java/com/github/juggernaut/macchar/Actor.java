package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.fsm.events.Event;

/**
 * @author ameya
 */
public interface Actor {

    String getId();
    void sendMessage(Event event);
    void destroy();
}
