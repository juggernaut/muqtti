package com.github.juggernaut.macchar;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class TestUtils {
    static ByteBuffer hexToByteBuf(final String hexStream) {
        assert hexStream.length() % 2 == 0;
        int dataLen = hexStream.length() / 2;
        byte[] data = new byte[dataLen];
        for (int i = 0; i < hexStream.length(); i+=2) {
            byte b = 0;
            int higherNibble = getNibble(hexStream.charAt(i));
            b |= (higherNibble << 4);
            int lowerNibble = getNibble(hexStream.charAt(i + 1));
            b |= lowerNibble;
            data[i / 2] = b;
        }
        return ByteBuffer.wrap(data);
    }

    private static int getNibble(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'a' && c <= 'f') {
            return (c - 'a') + 10;
        } else {
            throw new IllegalArgumentException("Illegal hex character " + c);
        }
    }

    public static ByteBuffer intArrayToByteBuf(final int[] data) {
        final var buffer = ByteBuffer.allocate(data.length);
        for (int i = 0 ; i < data.length; i++) {
            buffer.put((byte) data[i]);
        }
        return buffer.flip();
    }
}
