package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.packet.ConnectProperties;
import com.github.juggernaut.macchar.packet.Connect;
import com.github.juggernaut.macchar.packet.MqttPacket;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.github.juggernaut.macchar.QoS.AT_MOST_ONCE;
import static org.junit.Assert.*;

/**
 * @author ameya
 */
public class ConnectDecodingTest {

    @Test
    public void testConnectPacket() {
        // Taken from wireshark
        String hexStream = "101000044d5154540502003c032100140000";
        final var binData = TestUtils.hexToByteBuf(hexStream);
        final AtomicReference<MqttPacket> consumed = new AtomicReference<>();
        final var mqttDecoder = new MqttDecoder(consumed::set);
        mqttDecoder.onRead(binData);
        final MqttPacket decodedPacket = consumed.get();
        assertNotNull(decodedPacket);
        assertEquals(MqttPacket.PacketType.CONNECT, decodedPacket.getPacketType());
        final Connect connectPkt = (Connect) decodedPacket;
        assertEquals(AT_MOST_ONCE, connectPkt.getWillQoS());
        assertTrue(connectPkt.getConnectProperties().isPresent());
        final int receiveMax = connectPkt.getConnectProperties().map(ConnectProperties::getReceiveMaximum).orElse(-1);
        assertEquals(20, receiveMax);
        assertTrue(connectPkt.getClientId().isEmpty());
        assertEquals(60, connectPkt.getKeepAlive());
        assertTrue(connectPkt.hasCleanStartFlag());
        assertFalse(connectPkt.hasWillFlag());
        assertFalse(connectPkt.hasWillRetain());
        assertTrue(connectPkt.getUserName().isEmpty());
        assertTrue(connectPkt.getPassword().isEmpty());
    }
}
