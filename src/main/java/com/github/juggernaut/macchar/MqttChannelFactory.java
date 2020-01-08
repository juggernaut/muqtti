package com.github.juggernaut.macchar;

import java.nio.channels.SocketChannel;
import java.util.function.Function;

/**
 * @author ameya
 */
public class MqttChannelFactory implements Function<SocketChannel, ChannelListener> {

    private final ActorSystem actorSystem;

    public MqttChannelFactory(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public ChannelListener apply(SocketChannel socketChannel) {
        final var fsm = new MqttChannelStateMachine();
        final var actor = actorSystem.createActor(fsm);
        final var channel =  MqttChannel.create(socketChannel, actor);
        fsm.setMqttChannel(channel);
        return channel;
    }
}
