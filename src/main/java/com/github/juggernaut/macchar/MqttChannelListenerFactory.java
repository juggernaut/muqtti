package com.github.juggernaut.macchar;

import java.nio.channels.SocketChannel;
import java.util.function.Function;

/**
 * @author ameya
 */
public class MqttChannelListenerFactory implements Function<SocketChannel, ChannelListener> {

    @Override
    public ChannelListener apply(SocketChannel socketChannel) {
        return MqttChannel.create(socketChannel);
    }
}
