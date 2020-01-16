package com.github.juggernaut.macchar.session;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author ameya
 */
public class CircularBufferTest {

    @Test
    public void testBufferEmpty() {
        final var buffer = new CircularBuffer<String>(3);
        final var result = new ArrayList<String>();
        int readUntilPos = buffer.take(-1, result);
        assertEquals(-1, readUntilPos);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testReadSingleItem() {
        final var buffer = new CircularBuffer<String>(3);
        int nextPos = buffer.put("hello");
        assertEquals(1, nextPos);
        final var result = new ArrayList<String>();
        int readUntilPos = buffer.take(-1, result);
        assertEquals(0, readUntilPos);
        assertEquals(List.of("hello"), result);
    }

    @Test
    public void testMultipleReads() {
        // First put a single item, and read it
        final var buffer = new CircularBuffer<String>(3);
        int nextPos = buffer.put("hello");
        assertEquals(1, nextPos);
        final var result = new ArrayList<String>();
        int readUntilPos = buffer.take(-1, result);
        assertEquals(0, readUntilPos);
        assertEquals(List.of("hello"), result);

        // Next put two more items (this fills up the buffer and the position wraps around)
        buffer.put("world");
        nextPos = buffer.put("foo");
        // wrap-around
        assertEquals(0, nextPos);

        final var result2 = new ArrayList<String>();
        readUntilPos = buffer.take(readUntilPos, result2);
        assertEquals(2, readUntilPos);
        assertEquals(List.of("world", "foo"), result2);

        // Read again, should come up empty
        final var result3 = new ArrayList<String>();
        readUntilPos = buffer.take(readUntilPos, result3);
        // no change in readUntilPos
        assertEquals(2, readUntilPos);
        assertTrue(result3.isEmpty());
    }
}
