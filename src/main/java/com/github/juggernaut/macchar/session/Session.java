package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.Actor;
import com.github.juggernaut.macchar.packet.Disconnect;
import com.github.juggernaut.macchar.packet.Publish;
import com.github.juggernaut.macchar.packet.Subscribe;
import com.github.juggernaut.macchar.packet.Unsubscribe;

import java.util.List;

/**
 * @author ameya
 */
public interface Session {

    enum DisconnectCause {
        NORMAL_CLIENT_INITIATED,
        ABNORMAL_CLIENT_INITIATED,
        SERVER_INITIATED,
        UNCEREMONIOUS,
        KEEP_ALIVE_TIMED_OUT
    }

    /**
     * This is really the same as the client identifier
     * @return
     */
    String getId();

    boolean isExpired();

    boolean isConnected();

    void onDisconnect(DisconnectCause cause);

    void onSubscribe(Subscribe subscribeMsg);

    void onUnsubscribe(Unsubscribe unsubscribeMsg);

    void onPublish(Publish publishMsg);

    void sendDisconnect(Disconnect disconnect, DisconnectCause cause);

    void remove();

    List<MessageEntry> readAvailableQoS1Messages(int maxMessages);

    /**
     * Revive an existing stored session when the clientId connects again (which will be a new actor)
     *
     * @param actor
     */
    void reactivate(Actor actor);

    Actor getActor();

}
