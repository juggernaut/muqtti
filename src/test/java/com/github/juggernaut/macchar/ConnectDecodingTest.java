package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.packet.ConnectProperties;
import com.github.juggernaut.macchar.packet.Connect;
import com.github.juggernaut.macchar.packet.MqttPacket;
import com.github.juggernaut.macchar.packet.WillData;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.juggernaut.macchar.QoS.AT_LEAST_ONCE;
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

    @Test
    public void testConnectWithWillData() {
        String hexStream = "102400044d515454050e001403210014000000000c706c616365732f696e6469610003686579";
        final var binData = TestUtils.hexToByteBuf(hexStream);
        final AtomicReference<MqttPacket> consumed = new AtomicReference<>();
        final var mqttDecoder = new MqttDecoder(consumed::set);
        mqttDecoder.onRead(binData);
        final MqttPacket decodedPacket = consumed.get();
        assertNotNull(decodedPacket);
        assertEquals(MqttPacket.PacketType.CONNECT, decodedPacket.getPacketType());
        final Connect connectPkt = (Connect) decodedPacket;
        assertTrue(connectPkt.hasWillFlag());
        assertEquals(AT_LEAST_ONCE, connectPkt.getWillQoS());
        assertTrue(connectPkt.getWillData().isPresent());
        final WillData willData = connectPkt.getWillData().get();
        assertEquals("places/india", willData.getWillTopic());
        final ByteBuffer willPayload = willData.getWillPayload();
        final byte[] rawPayload = new byte[willPayload.remaining()];
        willPayload.get(rawPayload);
        final String payloadString = new String(rawPayload, StandardCharsets.UTF_8);
        assertEquals("hey", payloadString);
    }
}
