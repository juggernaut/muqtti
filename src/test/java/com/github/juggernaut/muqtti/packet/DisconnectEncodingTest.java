package com.github.juggernaut.muqtti.packet;

import com.github.juggernaut.muqtti.TestUtils;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * @author ameya
 */
public class DisconnectEncodingTest {

    @Test
    public void testDisconnectEncode() {
        String expected = "e0" + // header + flags
                "02" + // remaining length
                "8e" + // reason code
                "00"; // property length
        final var disconnect = Disconnect.create(Disconnect.ReasonCode.SESSION_TAKEN_OVER);
        final ByteBuffer[] encoded = disconnect.encode();
        assertEquals(1, encoded.length);
        assertEquals(expected, TestUtils.byteBufToHex(encoded[0]));
    }
}
