package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.packet.Connect;
import com.github.juggernaut.macchar.packet.MqttPacket;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A class that decodes the incoming byte stream into MQTT packets
 *
 * @author ameya
 */
public class MqttDecoder {

    private ByteBuffer remainingPacketBuffer;
    private int flags;

    private final VariableByteIntegerDecoder variableByteIntegerDecoder = new VariableByteIntegerDecoder();

    enum State {
        INIT,
        READING_REMAINING_LENGTH,
        READING_REMAINING_PACKET
    }

    private State state = State.INIT;

    private MqttPacket.PacketType packetType;

    private Consumer<MqttPacket> packetConsumer;

    public MqttDecoder(Consumer<MqttPacket> packetConsumer) {
        this.packetConsumer = Objects.requireNonNull(packetConsumer);
    }

    public MqttDecoder() {}

    public void setPacketConsumer(final Consumer<MqttPacket> packetConsumer) {
        this.packetConsumer = Objects.requireNonNull(packetConsumer);
    }

    public void onRead(final ByteBuffer incoming) {

        switch (state) {
            case INIT:
                readPacketTypeAndFlags(incoming);
                state = State.READING_REMAINING_LENGTH;
                break;
            case READING_REMAINING_LENGTH:
                final boolean decoded = variableByteIntegerDecoder.decode(incoming);
                if (decoded) {
                    final int remainingLength = variableByteIntegerDecoder.getValue();
                    variableByteIntegerDecoder.reset();
                    if (remainingLength > 0) {
                        remainingPacketBuffer = ByteBuffer.allocate(remainingLength);
                        state = State.READING_REMAINING_PACKET;
                    } else {
                        // TODO: check if remaining length of 0 is valid for this packet type
                        state = State.INIT;
                    }
                }
                break;
            case READING_REMAINING_PACKET:
                final boolean atCapacity = ByteBufferUtil.copyToCapacity(incoming, remainingPacketBuffer);
                if (atCapacity) {
                    decodeRemainingPacket();
                    state = State.INIT;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown state");
        }
        // Make sure we always read the entire incoming buffer
        if (incoming.hasRemaining()) {
            onRead(incoming);
        }

    }

    private void readPacketTypeAndFlags(final ByteBuffer buf) {
        byte packetTypeAndFlags = buf.get();
        int packetTypeRaw = (packetTypeAndFlags >> 4) & 0x0f;
        packetType = MqttPacket.PacketType.fromInt(packetTypeRaw);
        flags = packetTypeAndFlags & 0x0f;
    }

    private void decodeRemainingPacket() {
        switch (packetType) {
            case CONNECT:
                remainingPacketBuffer.flip();
                final MqttPacket connectPkt = Connect.fromBuffer(flags, remainingPacketBuffer);
                if (packetConsumer != null) {
                    packetConsumer.accept(connectPkt);
                }
                break;
            default:
                throw new IllegalArgumentException("Only CONNECT parsing has been implemented");
        }
    }
}
