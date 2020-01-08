package com.github.juggernaut.macchar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author ameya
 */
public class MqttServer {

    private static final int PORT = 1883;

    private final Function<SocketChannel, ChannelListener> channelListenerFactory;

    // TODO: figure out optimal size of buffer
    private final ByteBuffer readBuffer = ByteBuffer.allocate(64 * 1024);

    public MqttServer(Function<SocketChannel, ChannelListener> channelListenerFactory) {
        this.channelListenerFactory = Objects.requireNonNull(channelListenerFactory);
    }

    public void start() throws IOException {
        final var serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        // There is probably a race condition here between the bind and registration
        serverSocketChannel.bind(new InetSocketAddress(PORT));

        final var selector = Selector.open();
        final var key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Going into select loop..");
        while(true) {
            selector.select(selectedKey -> {
                if (selectedKey.isAcceptable()) {
                    try {
                        final var newSocket = ((ServerSocketChannel) selectedKey.channel()).accept();
                        if (newSocket != null) { // don't think this will ever happen, but defensive programming
                            System.out.println("Accepted new connection from " + newSocket.getRemoteAddress());
                            newSocket.configureBlocking(false);
                            final var newChannelKey = newSocket.register(selector, SelectionKey.OP_READ);
                            final var channelListener = channelListenerFactory.apply(newSocket);
                            newChannelKey.attach(channelListener);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (selectedKey.isReadable()) {
                    final var socketChannel = (SocketChannel) selectedKey.channel();
                    try {
                        int numberReadBytes = socketChannel.read(readBuffer);
                        while (numberReadBytes > 0) {
                            readBuffer.flip();
                            /*
                            while (readBuffer.hasRemaining()) {
                                socketChannel.write(readBuffer);
                            }
                            */
                            final var channelListener = (ChannelListener) selectedKey.attachment();
                            // It is expected that all of the buffer is read by the listener
                            channelListener.onRead(readBuffer);
                            if (readBuffer.hasRemaining()) {
                                System.err.println("ERROR: remaining bytes in read buffer, this a serious programming error");
                            }
                            readBuffer.clear();
                            numberReadBytes = socketChannel.read(readBuffer);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }, 50);


        }
    }
}
