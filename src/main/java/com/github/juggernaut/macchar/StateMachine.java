package com.github.juggernaut.macchar;

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
