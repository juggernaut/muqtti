package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.packet.MqttPacket;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author ameya
 */
public class MqttTlsChannel extends MqttChannel {

    private final SSLEngine sslEngine;
    private ByteBuffer cipherData = ByteBuffer.allocate(2 * 1024); // these will be adjusted to the correct size dynamically
    private ByteBuffer applicationData = ByteBuffer.allocate(2 * 1024);
    private ByteBuffer outboundData = ByteBuffer.allocate(2 * 1024);

    protected MqttTlsChannel(SocketChannel socketChannel, MqttDecoder mqttDecoder, Actor mqttChannelActor, SSLEngine sslEngine) {
        super(socketChannel, mqttDecoder, mqttChannelActor);
        assert !sslEngine.getUseClientMode();
        this.sslEngine = sslEngine;
    }

    public static MqttTlsChannel create(SocketChannel socketChannel, Actor mqttChannelActor, SSLEngine sslEngine) {
        final var mqttDecoder = new MqttDecoder();
        final var mqttTlsChannel = new MqttTlsChannel(socketChannel, mqttDecoder, mqttChannelActor, sslEngine);
        mqttDecoder.setPacketConsumer(mqttTlsChannel);
        return mqttTlsChannel;
    }

    @Override
    public void onRead(ByteBuffer src) {
        // TODO: It's unlikely that network buffer is larger if we size both correctly, but handle this case for now...
        if (src.remaining() > (cipherData.capacity() - cipherData.position())) {
            ByteBuffer b = ByteBuffer.allocate(src.remaining() + cipherData.position());
            cipherData.flip();
            b.put(cipherData);
            cipherData = b;
        }
        cipherData.put(src);
        doUnwrap();
    }

    private void doUnwrap() {
        try {
            final SSLEngineResult result = sslEngine.unwrap(cipherData, applicationData);
            switch(result.getStatus()) {
                case BUFFER_OVERFLOW:
                    int appSize = sslEngine.getSession().getApplicationBufferSize();
                    ByteBuffer b = ByteBuffer.allocate(appSize + applicationData.position());
                    applicationData.flip();
                    b.put(applicationData);
                    applicationData = b;
                    doUnwrap();
                    break;
                case BUFFER_UNDERFLOW:
                    int netSize = sslEngine.getSession().getPacketBufferSize();
                    if (netSize > cipherData.capacity()) {
                        // This happens if the incoming TLS record is greater than our currently allocated byte buffer
                        ByteBuffer tmp = ByteBuffer.allocate(netSize);
                        cipherData.flip();
                        tmp.put(cipherData);
                        cipherData = tmp;
                    } else {
                        // We don't have enough data in cipherdata to make up a tls record; prepare for more incoming data
                        cipherData.compact();
                    }
                    break;
                case OK:
                    applicationData.flip();
                    super.onRead(applicationData);
                    if (applicationData.hasRemaining()) {
                        System.out.println("Application didn't read all data");
                    }
                    applicationData.clear();
                    cipherData.compact();
                    break;
                case CLOSED:
                    System.out.println("Channel already closed..");
                    break;
            }
        } catch (SSLException e) {
            e.printStackTrace();
        }

    }

    private ByteBuffer enlargeBuffer(final ByteBuffer inputBuffer, final int newSize) {
        ByteBuffer tmp = ByteBuffer.allocate(newSize);
        inputBuffer.flip();
        tmp.put(inputBuffer);
        return tmp;
    }

    @Override
    public void sendPacket(MqttPacket packet) {
        final ByteBuffer[] encoded = packet.encode();
        doWrap(encoded);
        outboundData.flip();
        try {
            // TODO: handle partial writes
            socketChannel.write(outboundData);
            outboundData.compact();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPacketAndDisconnect(MqttPacket packet) {
        sendPacket(packet);
        sslEngine.closeOutbound();
        try {
            while (!sslEngine.isOutboundDone()) {
                // TODO: this is only generating handshake data now, so what should be the source buffer? just using a dummy buffer for now
                sslEngine.wrap(ByteBuffer.allocate(0), outboundData);
                outboundData.flip();
                // TODO: handle partial writes
                socketChannel.write(outboundData);
                outboundData.compact();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doWrap(final ByteBuffer[] encoded) {
        try {
            final SSLEngineResult result = sslEngine.wrap(encoded, outboundData);
            switch (result.getStatus()) {
                case BUFFER_OVERFLOW:
                    outboundData = enlargeBuffer(outboundData, sslEngine.getSession().getPacketBufferSize());
                    doWrap(encoded);
                    break;
                case CLOSED:
                    System.out.println("Channel already closed, can't write to it");
            }
        } catch (SSLException e) {
            e.printStackTrace();
        }
    }
}
