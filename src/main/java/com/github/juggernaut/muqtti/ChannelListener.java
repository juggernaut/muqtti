package com.github.juggernaut.muqtti;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public interface ChannelListener {

    void onRead(ByteBuffer buffer);

    void onDisconnect();

    /**
     * Called when OP_WRITE is triggered i.e the channel is deemed as writeable
     */
    void onWriteReady();
}
