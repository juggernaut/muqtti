package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.fsm.MqttChannelStateMachine;
import com.github.juggernaut.macchar.session.SessionManager;

import javax.net.ssl.SSLContext;
import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author ameya
 */
public class MqttChannelFactory implements Function<SelectionKey, ChannelListener> {

    private final ActorSystem actorSystem;
    private final SessionManager sessionManager;
    private final Optional<SSLContext> sslContext;

    // NOTE: not supporting TLSv1.3 yet..
    private static final String[] ENABLED_PROTOCOLS = new String[] {"TLSv1.1", "TLSv1.2"};

    public MqttChannelFactory(ActorSystem actorSystem, SessionManager sessionManager, Optional<SSLContext> sslContext) {
        this.actorSystem = actorSystem;
        this.sessionManager = sessionManager;
        this.sslContext = sslContext;
    }

    @Override
    public ChannelListener apply(SelectionKey selectionKey) {
        final var fsm = new MqttChannelStateMachine(sessionManager);
        final var actor = actorSystem.createActor(fsm);
        final MqttChannel channel = sslContext
                .map(ctx -> {
                    final var sslEngine = ctx.createSSLEngine();
                    sslEngine.setUseClientMode(false);
                    sslEngine.setEnabledProtocols(ENABLED_PROTOCOLS);
                    return (MqttChannel) MqttTlsChannel.create(selectionKey, actor, sslEngine);
                })
                .orElseGet(() -> MqttChannel.create(selectionKey, actor));
        fsm.setMqttChannel(channel);
        return channel;
    }
}
