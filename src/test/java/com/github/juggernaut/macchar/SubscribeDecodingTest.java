package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.packet.MqttPacket;
import com.github.juggernaut.macchar.packet.Subscribe;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.juggernaut.macchar.packet.MqttPacket.PacketType.SUBSCRIBE;
import static org.junit.Assert.*;

/**
 * @author ameya
 */
public class SubscribeDecodingTest {

    @Test
    public void testSubscribeFromSpec() {
        // Figure 3‑21 - Payload byte format non-normative example
        final int[] packet = {
                0x82, // fixed header
                15, // remaining length
                0, 10, // packetId
                0, // property length
                0, 3, // filter length
                0x61, 0x2F, 0x62, // 'a/b'
                1, // sub options
                0, 3, // filter length
                0x63, 0x2F, 0x64, // 'c/d'
                2 // sub options
        };


        final ByteBuffer binData = TestUtils.intArrayToByteBuf(packet);
        final AtomicReference<MqttPacket> consumed = new AtomicReference<>();
        final var mqttDecoder = new MqttDecoder(consumed::set);
        mqttDecoder.onRead(binData);
        final MqttPacket decodedPacket = consumed.get();
        assertFalse(binData.hasRemaining());
        assertNotNull(decodedPacket);
        assertEquals(SUBSCRIBE, decodedPacket.getPacketType());
        final var subscribe = (Subscribe) decodedPacket;
        assertEquals(10, subscribe.getPacketId());
        final var topicFilters = subscribe.getSubscriptions();
        assertEquals(2, topicFilters.size());
        final var filter1 = topicFilters.get(0);
        assertEquals("a/b", filter1.getFilter());
        assertEquals(QoS.AT_LEAST_ONCE, filter1.getQoS());
        assertFalse(filter1.hasNoLocalOption());
        assertFalse(filter1.hasRetainAsPublishedOption());
        assertEquals(0, filter1.getRetainHandlingOption());

        final var filter2 = topicFilters.get(1);
        assertEquals("c/d", filter2.getFilter());
        assertEquals(QoS.EXACTLY_ONCE, filter2.getQoS());
        assertFalse(filter2.hasNoLocalOption());
        assertFalse(filter2.hasRetainAsPublishedOption());
        assertEquals(0, filter2.getRetainHandlingOption());
    }
}
