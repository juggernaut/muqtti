package com.github.juggernaut.macchar.session;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ameya
 */
public class CircularBuffer<E> {

    private final Object[] array;
    private final int capacity;
    // The next position that is writeable
    private volatile int position= 0;

    public CircularBuffer(int capacity) {
        this.capacity = capacity;
        this.array = new Object[capacity];
    }

    /**
     * Adds an item into the buffer and returns the next writeable position
     * This is NON thread-safe because it is assumed that the caller takes care
     * of thread safety
     *
     * @param item
     * @return
     */
    public int add(final E item) {
        array[position] = item;
        position = (position + 1) % capacity;
        return position;
    }

    public int getPosition() {
        return position;
    }
}
