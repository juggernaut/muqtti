package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.fsm.events.ChannelDisconnectedEvent;
import com.github.juggernaut.macchar.fsm.events.PacketReceivedEvent;
import com.github.juggernaut.macchar.packet.MqttPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author ameya
 */
public class MqttChannel implements ChannelListener, Consumer<MqttPacket> {

    protected final SocketChannel socketChannel;
    private final MqttDecoder mqttDecoder;
    private final Actor mqttChannelActor;

    // TODO: better timeouts using a timer wheel
    private static final ScheduledExecutorService timerService = Executors.newSingleThreadScheduledExecutor();
    private volatile ScheduledFuture<?> timerFuture;
    private volatile long keepAliveTimeout = -1;

    protected MqttChannel(SocketChannel socketChannel, MqttDecoder mqttDecoder, Actor mqttChannelActor) {
        this.socketChannel = Objects.requireNonNull(socketChannel);
        this.mqttDecoder = Objects.requireNonNull(mqttDecoder);
        this.mqttChannelActor = Objects.requireNonNull(mqttChannelActor);
    }

    public static MqttChannel create(final SocketChannel socketChannel, final Actor mqttChannelActor) {
        final var mqttDecoder = new MqttDecoder();
        final var mqttChannel = new MqttChannel(socketChannel, mqttDecoder, mqttChannelActor);
        mqttDecoder.setPacketConsumer(mqttChannel);
        return mqttChannel;
    }

    @Override
    public void onRead(ByteBuffer buffer) {
        updateTimer();
        try {
            mqttDecoder.onRead(buffer);
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to parse MQTT packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTimer() {
        if (timerFuture != null) {
            timerFuture.cancel(false);
            scheduleKeepAliveTimeout();
        }
    }

    private void scheduleKeepAliveTimeout() {
        timerFuture = timerService.schedule(() -> {
            System.out.println("Disconnecting channel because keepalive timeout expired");
            try {
                socketChannel.close();
            } catch (IOException e) {
                // TODO: log warn here
                e.printStackTrace();
            }
        }, keepAliveTimeout, TimeUnit.SECONDS);
    }

    public void setKeepAliveTimeout(final long timeout) {
        assert timeout > 0 && timeout <= 600;
        // Can only be set once (during CONNECT handshake)
        assert keepAliveTimeout == -1 && timerFuture == null;
        keepAliveTimeout = timeout;
        scheduleKeepAliveTimeout();
    }

    @Override
    public void accept(MqttPacket mqttPacket) {
        System.out.println("Successfully decoded packet of type " + mqttPacket.getPacketType());
        mqttChannelActor.sendMessage(new PacketReceivedEvent(mqttPacket));
    }

    public void sendPacket(MqttPacket packet) {
        final ByteBuffer[] encoded = packet.encode();
        try {
            // TODO: handle partial writes here
            socketChannel.write(encoded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPacketAndDisconnect(MqttPacket packet) {
        final ByteBuffer[] encoded = packet.encode();
        try {
            // TODO: handle partial writes here
            socketChannel.write(encoded);
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnect() {
        mqttChannelActor.sendMessage(new ChannelDisconnectedEvent());
    }
}
