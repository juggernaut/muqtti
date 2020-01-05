package com.github.juggernaut;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author ameya
 */
public class EchoServer {

    private static final int PORT = 6010;

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
                            newSocket.register(selector, SelectionKey.OP_READ);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (selectedKey.isReadable()) {
                    final var socketChannel = (SocketChannel) selectedKey.channel();
                    final var buf = ByteBuffer.allocate(64);
                    try {
                        int numberReadBytes = socketChannel.read(buf);
                        while (numberReadBytes > 0) {
                            buf.flip();
                            /*
                            while(buf.hasRemaining()) {
                                char data = (char) buf.get();
                                System.out.print(data);
                            }
                            */
                            socketChannel.write(buf);
                            buf.clear();
                            numberReadBytes = socketChannel.read(buf);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

            }, 50);


        }
    }
}
