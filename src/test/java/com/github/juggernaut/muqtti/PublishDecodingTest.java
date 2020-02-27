package com.github.juggernaut.muqtti;

import com.github.juggernaut.muqtti.packet.MqttPacket;
import com.github.juggernaut.muqtti.packet.Publish;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.juggernaut.muqtti.packet.QoS.AT_MOST_ONCE;
import static com.github.juggernaut.muqtti.packet.MqttPacket.PacketType.PUBLISH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author ameya
 */
public class PublishDecodingTest {

    @Test
    public void testPublishPacket() {
        // taken from wireshark
        String hexStream = "3011000c706c616365732f696e646961006869";
        final var binData = TestUtils.hexToByteBuf(hexStream);
        final AtomicReference<MqttPacket> consumed = new AtomicReference<>();
        final var mqttDecoder = new MqttDecoder(consumed::set);
        mqttDecoder.onRead(binData);
        final MqttPacket decodedPacket = consumed.get();
        assertNotNull(decodedPacket);
        assertEquals(PUBLISH, decodedPacket.getPacketType());
        final Publish pkt = (Publish) decodedPacket;
        assertEquals(AT_MOST_ONCE, pkt.getQoS());
        assertEquals("places/india", pkt.getTopicName());
        final var payload = pkt.getPayload();
        final byte[] rawPayload = new byte[payload.remaining()];
        payload.get(rawPayload);
        final String payloadString = new String(rawPayload, StandardCharsets.UTF_8);
        assertEquals("hi", payloadString);
    }
}
