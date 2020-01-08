package com.github.juggernaut.macchar;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public interface ChannelListener {

    void onRead(ByteBuffer buffer);
}
