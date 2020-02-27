package com.github.juggernaut.muqtti;

import com.github.juggernaut.muqtti.exception.DecodingException;

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

    public static int decodeTwoByteInteger(final ByteBuffer buf) {
        assert buf.remaining() >= 2;
        short s = buf.getShort();
        return Short.toUnsignedInt(s);
    }

    public static void encodeTwoByteInteger(final ByteBuffer buf, int length) {
        assert length >= 0 && length <= 65535;
        buf.putShort((short) length);
    }

    public static long decodeFourByteInteger(final ByteBuffer buf) {
        assert buf.remaining() >= 4;
        int i = buf.getInt();
        return Integer.toUnsignedLong(i);
    }

    public static String decodeUTF8String(final ByteBuffer buf) {
        int strLen = decodeTwoByteInteger(buf);
        if (strLen == 0) {
            return ""; // this is f'ing crazy but the spec allows this
        }
        // No bounds checking here, assuming that buffer has enough
        final byte[] strBytes = new byte[strLen];
        buf.get(strBytes);
        return new String(strBytes, StandardCharsets.UTF_8);
    }

    public static byte[] decodeBinaryData(final ByteBuffer buf) {
        int binaryDataLen = decodeTwoByteInteger(buf);
        if (binaryDataLen == 0) {
            // This is allowed according to spec
            return new byte[0];
        }
        // Again no bounds checking
        byte[] binaryData = new byte[binaryDataLen];
        buf.get(binaryData);
        return binaryData;
    }

    public static ByteBuffer extractBinaryDataAsByteBuffer(final ByteBuffer buf) {
        int binaryDataLen = decodeTwoByteInteger(buf);
        if (binaryDataLen == 0) {
            // This is allowed according to spec
            return ByteBuffer.allocate(0);
        }
        if (buf.remaining() < binaryDataLen) {
            throw new DecodingException("Less data available in bytebuffer than length indicate in binary data prefix");
        }
        final ByteBuffer sliced = buf.slice();
        sliced.limit(binaryDataLen);
        buf.position(buf.position() + binaryDataLen);
        return sliced;
    }

    public static void encodeBinaryData(final ByteBuffer buf, final byte[] value) {
        assert value.length <= 65535;
        encodeTwoByteInteger(buf, value.length);
        if (value.length > 0) {
            buf.put(value);
        }
    }

    public static int getEncodedBinaryDataLength(final byte[] value) {
        return 2 + value.length;
    }

    public static int getEncodedVariableByteIntegerLength(final int i) {
        assert i >= 0 && i < 268435455;
        // From table 1-1 in spec
        if (i < 128) {
            return 1;
        }
        if (i < 16384) {
            return 2;
        }
        if (i < 2097152) {
            return 3;
        }
        return 4;
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
        encodeTwoByteInteger(buffer, bytes.length);
        buffer.put(bytes);
    }

    public static int getEncodedUTF8StringLenghInBytes(final String s) {
        return 2 + getUTF8StringLengthInBytes(s);
    }

    private static int getUTF8StringLengthInBytes(final String s) {
        return s.getBytes(StandardCharsets.UTF_8).length;
    }

    public static void encodeFourByteInteger(ByteBuffer buffer, long value) {
        buffer.putInt((int) value);
    }
}
