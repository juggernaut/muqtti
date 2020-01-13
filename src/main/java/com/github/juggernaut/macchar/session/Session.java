package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.packet.Publish;
import com.github.juggernaut.macchar.packet.Subscribe;

/**
 * @author ameya
 */
public interface Session {

    /**
     * This is really the same as the client identifier
     * @return
     */
    String getId();

    boolean isExpired();

    void onDisconnect();

    void onSubscribe(Subscribe subscribeMsg);

}
