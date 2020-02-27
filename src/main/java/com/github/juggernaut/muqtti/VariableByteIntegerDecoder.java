package com.github.juggernaut.muqtti;

import com.github.juggernaut.muqtti.exception.DecodingException;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class VariableByteIntegerDecoder {

    // Max value of a variable byte integer is 268,435,455 so it will fit in an int
    int value = 0;

    int lengthSoFar = 0;

    private boolean finished;

    public boolean decode(final ByteBuffer buffer) {
        if (finished) {
            // TODO: throw exception
        }
        while (buffer.hasRemaining()) {
            byte digit = buffer.get();
            boolean hasMore = (digit & 0x80) != 0;
            int data = digit & 0x7f;
            value |= (data << (7 * lengthSoFar));
            lengthSoFar++;
            if (!hasMore) {
                finished = true;
                break;
            }
            // Variable byte integer can't be more than 4 bytes
            if (lengthSoFar == 4) {
                throw new DecodingException("Invalid variable byte integer");
            }
        }
        return finished;
    }

    public int getValue() {
        if (!finished) {
            throw new IllegalArgumentException("should not call getValue before finished");
        }
        return value;
    }

    public void reset() {
        finished = false;
        lengthSoFar = 0;
        value = 0;
    }
}
