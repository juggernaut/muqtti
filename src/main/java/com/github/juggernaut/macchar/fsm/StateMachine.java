package com.github.juggernaut.macchar.fsm;

import com.github.juggernaut.macchar.fsm.events.Event;

/**
 * @author ameya
 */
public interface StateMachine {

    void init();

    /**
     * Returns whether the event was handled (and action applied) or not
     * @param event
     * @return
     */
    boolean onEvent(Event event);
    void shutDown();
}
