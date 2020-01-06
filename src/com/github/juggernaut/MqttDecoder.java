package com.github.juggernaut;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * A class that decodes the incoming byte stream into MQTT packets
 *
 * @author ameya
 */
public class MqttDecoder {

    private ByteBuffer currentBuffer;

    private final VariableByteIntegerDecoder variableByteIntegerDecoder = new VariableByteIntegerDecoder();

    enum State {
        INIT,
        READING_REMAINING_LENGTH,
        READING_REMAINING_PACKET
    }

    private State state = State.INIT;

    private PacketTypes packetType;

    enum PacketTypes {
        CONNECT(1),
        CONNACK(2),
        PUBLISH(3),
        PUBACK(4),
        PUBREC(5),
        PUBREL(6),
        PUBCOMP(7),
        SUBSCRIBE(8),
        SUBACK(9),
        UNSUBSCRIBE(10),
        UNSUBACK(11),
        PINGREQ(12),
        PINGRESP(13),
        DISCONNECT(14),
        AUTH(15);

        private final int intValue;

        PacketTypes(int value) {
            this.intValue = value;
        }

        static PacketTypes fromInt(int input) {
            return Arrays.stream(values()).filter(v -> v.intValue == input).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid value " + input + " for packet type"));
        }
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
                        currentBuffer = ByteBuffer.allocate(remainingLength);
                        state = State.READING_REMAINING_PACKET;
                    } else {
                        // TODO: check if remaining length of 0 is valid for this packet type
                        state = State.INIT;
                    }
                }
                break;
            case READING_REMAINING_PACKET:
                final boolean atCapacity = ByteBufferUtil.copyToCapacity(incoming, currentBuffer);
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
        int packetTypeRaw = packetTypeAndFlags & 0xf0;
        packetType = PacketTypes.fromInt(packetTypeRaw);

        int flags = packetTypeAndFlags & 0x0f;
    }

    private void decodeRemainingPacket() {

    }

    private void parseConnect(final ByteBuffer buf, int flags) {


    }

}
