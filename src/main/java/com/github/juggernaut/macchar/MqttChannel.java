package com.github.juggernaut.macchar;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author ameya
 */
public class MqttChannel implements ChannelListener, Consumer<MqttPacket> {

    private final SocketChannel socketChannel;
    private final MqttDecoder mqttDecoder;

    protected MqttChannel(SocketChannel socketChannel, MqttDecoder mqttDecoder) {
        this.socketChannel = Objects.requireNonNull(socketChannel);
        this.mqttDecoder = Objects.requireNonNull(mqttDecoder);
    }

    public static MqttChannel create(final SocketChannel socketChannel) {
        final var mqttDecoder = new MqttDecoder();
        final var mqttChannel = new MqttChannel(socketChannel, mqttDecoder);
        mqttDecoder.setPacketConsumer(mqttChannel);
        return mqttChannel;
    }

    @Override
    public void onRead(ByteBuffer buffer) {
        try {
            mqttDecoder.onRead(buffer);
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to parse MQTT packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void accept(MqttPacket mqttPacket) {
        System.out.println("Successfully decoded packet of type " + mqttPacket.getPacketType());
    }
}
