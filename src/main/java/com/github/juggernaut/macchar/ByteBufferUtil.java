package com.github.juggernaut.macchar;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author ameya
 */
public class ByteBufferUtil {

    /**
     * Copy a source buffer into the destination buffer, making sure that the destination
     * buffer does not overflow. Returns whether or not the buffer was filled to capacity
     *
     * @param src source buffer
     * @param dst destination buffer
     */
    public static boolean copyToCapacity(final ByteBuffer src, final ByteBuffer dst) {
        assert src.hasRemaining();
        final int capacityRemaining = dst.capacity() - dst.position();
        final boolean dstFull = src.remaining() >= capacityRemaining;
        if (src.remaining() <= capacityRemaining) {
            dst.put(src);
        } else {
            final var sliced = src.slice();
            sliced.limit(capacityRemaining);
            dst.put(sliced);
            src.position(src.position() + capacityRemaining);
        }
        return dstFull;
    }

    public static int decodeTwoByteLength(final ByteBuffer buf) {
        assert buf.remaining() >= 2;
        short s = buf.getShort();
        return Short.toUnsignedInt(s);
    }

    public static void encodeTwoByteLength(final ByteBuffer buf, int length) {
        assert length >= 0 && length <= 65535;
        buf.putShort((short) length);
    }

    public static long decodeFourByteLength(final ByteBuffer buf) {
        assert buf.remaining() >= 4;
        int i = buf.getInt();
        return Integer.toUnsignedLong(i);
    }

    public static String decodeUTF8String(final ByteBuffer buf) {
        int strLen = decodeTwoByteLength(buf);
        if (strLen == 0) {
            return ""; // this is f'ing crazy but the spec this is allowed
        }
        // No bounds checking here, assuming that buffer has enough
        final byte[] strBytes = new byte[strLen];
        buf.get(strBytes);
        return new String(strBytes, StandardCharsets.UTF_8);
    }

    public static byte[] decodeBinaryData(final ByteBuffer buf) {
        int binaryDataLen = decodeTwoByteLength(buf);
        if (binaryDataLen == 0) {
            // This is allowed according to spec
            return new byte[0];
        }
        // Again no bounds checking
        byte[] binaryData = new byte[binaryDataLen];
        buf.get(binaryData);
        return binaryData;
    }

    public static void encodeVariableByteInteger(final ByteBuffer buffer, final int i) {
        assert i >= 0 && i < 268435455;
        // Algorithm taken from MQTT-v5.0 Section 1.5.5
        int x = i;
        do {
            int encodedByte = x % 128;
            x /= 128;
            // if there are more data to encode, set the top bit of this byte
            if (x > 0) {
                encodedByte = encodedByte | 128;
            }
            buffer.put((byte) encodedByte);
        } while (x > 0);
    }

    public static void encodeUTF8String(final ByteBuffer buffer, final String s) {
        final byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        encodeTwoByteLength(buffer, bytes.length);
        buffer.put(bytes);
    }

    public static int getUTF8StringLengthInBytes(final String s) {
        return s.getBytes(StandardCharsets.UTF_8).length;
    }
}
