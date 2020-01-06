package com.github.juggernaut;

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

    public static int getTwoByteLength(final ByteBuffer buf) {
        assert buf.remaining() >= 2;
        short s = buf.getShort();
        return Short.toUnsignedInt(s);
    }

    public static long getFourByteLength(final ByteBuffer buf) {
        assert buf.remaining() >= 4;
        int i = buf.getInt();
        return Integer.toUnsignedLong(i);
    }

    public static String getUTF8String(final ByteBuffer buf) {
        int strLen = getTwoByteLength(buf);
        if (strLen == 0) {
            return ""; // this is f'ing crazy but the spec this is allowed
        }
        // No bounds checking here, assuming that buffer has enough
        final byte[] strBytes = new byte[strLen];
        buf.get(strBytes);
        return new String(strBytes, StandardCharsets.UTF_8);
    }

    public static byte[] getBinaryData(final ByteBuffer buf) {
        int binaryDataLen = getTwoByteLength(buf);
        if (binaryDataLen == 0) {
            // This is allowed according to spec
            return new byte[0];
        }
        // Again no bounds checking
        byte[] binaryData = new byte[binaryDataLen];
        buf.get(binaryData);
        return binaryData;
    }
}
