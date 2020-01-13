package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.fsm.MqttChannelStateMachine;
import com.github.juggernaut.macchar.session.SessionManager;

import java.nio.channels.SocketChannel;
import java.util.function.Function;

/**
 * @author ameya
 */
public class MqttChannelFactory implements Function<SocketChannel, ChannelListener> {

    private final ActorSystem actorSystem;
    private final SessionManager sessionManager;

    public MqttChannelFactory(ActorSystem actorSystem, SessionManager sessionManager) {
        this.actorSystem = actorSystem;
        this.sessionManager = sessionManager;
    }

    @Override
    public ChannelListener apply(SocketChannel socketChannel) {
        final var fsm = new MqttChannelStateMachine(sessionManager);
        final var actor = actorSystem.createActor(fsm);
        final var channel =  MqttChannel.create(socketChannel, actor);
        fsm.setMqttChannel(channel);
        return channel;
    }
}
