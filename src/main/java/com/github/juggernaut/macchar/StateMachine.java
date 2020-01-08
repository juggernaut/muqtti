package com.github.juggernaut.macchar;

/**
 * @author ameya
 */
public interface StateMachine {

    void init();
    void onEvent(Event event);
    void shutDown();
}
