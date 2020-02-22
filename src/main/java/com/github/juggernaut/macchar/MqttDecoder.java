package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.exception.DecodingException;
import com.github.juggernaut.macchar.packet.*;

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
                        decodeZeroRemainingLengthPacket();
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
                throw new IllegalStateException("Unknown state");
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
        remainingPacketBuffer.flip();
        MqttPacket packet = null;
        switch (packetType) {
            case CONNECT:
                packet = Connect.fromBuffer(flags, remainingPacketBuffer);
                break;
            case PUBLISH:
                packet = Publish.fromBuffer(flags, remainingPacketBuffer);
                break;
            case SUBSCRIBE:
                packet = Subscribe.fromBuffer(flags, remainingPacketBuffer);
                break;
            case PUBACK:
                packet = PubAck.fromBuffer(flags, remainingPacketBuffer);
                break;
            case DISCONNECT:
                packet = Disconnect.fromBuffer(flags, remainingPacketBuffer);
                break;
            case UNSUBSCRIBE:
                packet = Unsubscribe.fromBuffer(flags, remainingPacketBuffer);
                break;
            default:
                throw new DecodingException("Decoding packet type " + packetType + " is not yet implemented");
        }
        if (remainingPacketBuffer.hasRemaining()) {
            throw new DecodingException("Extraneous length in buffer after decoding packet; this may be a corrupt stream");
        }
        if (packetConsumer != null) {
            packetConsumer.accept(packet);
        }
    }

    private void decodeZeroRemainingLengthPacket() {
        MqttPacket packet = null;
        switch (packetType) {
            case PINGREQ:
                packet = PingReq.fromFixedHeaderOnly(flags);
                break;
            case DISCONNECT:
                packet = Disconnect.fromFixedHeaderOnly(flags);
                break;
            default:
                throw new DecodingException("Packet type " + packetType + " cannot have zero remaining length");
        }
        if (packetConsumer != null) {
            packetConsumer.accept(packet);
        }
    }
}
