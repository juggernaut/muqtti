package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.packet.Publish;

/**
 * @author ameya
 */
public interface Session {

    /**
     * This is really the same as the client identifier
     * @return
     */
    String getId();

    void onPublishReceived(Publish msg);

    boolean isExpired();

    void onDisconnect();
}
