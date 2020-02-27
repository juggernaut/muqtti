package com.github.juggernaut.muqtti.session;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author ameya
 */
// TODO: revisit this whole circular buffer implementation
public class CircularBuffer<E> {

    private final Object[] array;
    private final int capacity;
    // The next position that is writeable
    private volatile int position = 0;

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
    public int put(final E item) {
        array[position] = item;
        position = (position + 1) % capacity;
        return position;
    }

    public int getPosition() {
        return position;
    }

    /**
     * Read the buffer up to the current position (exclusive) starting from a specified position (exclusive) i.e
     * startPos is the position until which data is already assumed to have been read
     *
     * @param startPos position in the buffer until which data has already been read
     * @return the position until which data has been read
     */
    public int take(int startPos, List<E> result, Predicate<E> filter) {
        return take(startPos, result, -1, filter);
    }

    public int take(int startPos, List<E> result, int numElements, Predicate<E> filter) {
        assert startPos >= -1 && startPos < capacity;
        assert numElements == -1 || numElements > 0;
        int elementsReadSoFar = 0;
        int readUntilPos = startPos;
        int i = (startPos + 1) % capacity;
        while (i != position && (numElements == -1 || elementsReadSoFar < numElements)) {
            final E item = (E) array[i];
            if (filter.test(item)) {
                result.add(item);
                elementsReadSoFar++;
            }
            readUntilPos = (readUntilPos + 1) % capacity;
            i = (i + 1) % capacity;
        }
        return readUntilPos;
    }
}
