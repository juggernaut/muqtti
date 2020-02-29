package com.github.juggernaut.muqtti;

import com.github.juggernaut.muqtti.packet.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ameya
 */
public class MqttServer {

    private final int port;
    private final Function<SelectionKey, ChannelListener> channelListenerFactory;
    private final int numSelectorThreads;

    private static final Logger LOGGER = Logger.getLogger(MqttServer.class.getName());

    public MqttServer(Function<SelectionKey, ChannelListener> channelListenerFactory, final int port, int numSelectorThreads) {
        this.channelListenerFactory = Objects.requireNonNull(channelListenerFactory);
        this.port = port;
        this.numSelectorThreads = numSelectorThreads;
    }

    public MqttServer(Function<SelectionKey, ChannelListener> channelListenerFactory, final int port) {
        this(channelListenerFactory, port, Runtime.getRuntime().availableProcessors());
    }

    public void start() throws IOException {
        for (int i = 0; i < numSelectorThreads; i++) {
            final var eventLoop = new EventLoop();
            eventLoop.setup();
            final var thread = new Thread(eventLoop);
            thread.setDaemon(false);
            thread.start();
        }
        LOGGER.info("Started Muqtti on port " + port);
    }

    class EventLoop implements Runnable {

        private Selector selector;

        // TODO: figure out optimal size of buffer
        private final ByteBuffer readBuffer = ByteBuffer.allocate(64 * 1024);

        public void setup() throws IOException {
            final var serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEPORT, true);
            // There is probably a race condition here between the bind and registration
            serverSocketChannel.bind(new InetSocketAddress(port));

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        }

        @Override
        public void run() {
            while (true) {
                try {
                    doSelect();
                } catch (Exception e) {
                    // we shouldn't really get here
                    e.printStackTrace();
                }
            }
        }

        private void doSelect() throws IOException {

                selector.select(selectedKey -> {
                    if (selectedKey.isAcceptable()) {
                        try {
                            final var newSocket = ((ServerSocketChannel) selectedKey.channel()).accept();
                            if (newSocket != null) { // don't think this will ever be null, but defensive programming
                                LOGGER.fine(() -> "Accepted new connection from " + Utils.getRemoteAddressUnchecked(newSocket));
                                newSocket.configureBlocking(false);
                                // Turn off nagle
                                newSocket.setOption(StandardSocketOptions.TCP_NODELAY, true);
                                final var newChannelKey = newSocket.register(selector, SelectionKey.OP_READ);
                                final var channelListener = channelListenerFactory.apply(newChannelKey);
                                newChannelKey.attach(channelListener);
                            }
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, e, () -> "Error accepting connection");
                        }
                    } else if (selectedKey.isReadable()) {
                        final var socketChannel = (SocketChannel) selectedKey.channel();
                        try {
                            final var channelListener = (ChannelListener) selectedKey.attachment();
                            int numberReadBytes = socketChannel.read(readBuffer);
                            if (numberReadBytes == -1) {
                                selectedKey.cancel();
                                channelListener.onDisconnect();
                            }
                            while (numberReadBytes > 0) {
                                readBuffer.flip();
                                // It is expected that all of the buffer is read by the listener
                                channelListener.onRead(readBuffer);
                                if (readBuffer.hasRemaining()) {
                                    LOGGER.severe("Remaining bytes in read buffer; the handler _should_ consume all" +
                                            " bytes. This a serious programming error!");
                                }
                                readBuffer.clear();
                                numberReadBytes = socketChannel.read(readBuffer);
                            }
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, e, () -> "I/O error reading bytes off socket " + Utils.getRemoteAddressUnchecked(socketChannel));
                        }
                    } else if (selectedKey.isWritable()) {
                        final var channelListener = (ChannelListener) selectedKey.attachment();
                        channelListener.onWriteReady();
                    }
                });

        }
    }
}
