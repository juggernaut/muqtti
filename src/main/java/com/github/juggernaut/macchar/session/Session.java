package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.packet.Disconnect;
import com.github.juggernaut.macchar.packet.Publish;
import com.github.juggernaut.macchar.packet.Subscribe;

import java.util.List;

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

    boolean isConnected();

    void onDisconnect();

    void onSubscribe(Subscribe subscribeMsg);

    void onPublish(Publish publishMsg);

    void sendDisconnect(Disconnect disconnect);

    void remove();

    List<Publish> readAvailableQoS1Messages(int maxMessages);

}
