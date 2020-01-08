package com.github.juggernaut.macchar;

/**
 * @author ameya
 */
public interface Actor {

    String getId();
    void sendMessage(Event event);
    void destroy();
}
