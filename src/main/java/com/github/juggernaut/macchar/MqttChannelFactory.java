package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.fsm.MqttChannelStateMachine;
import com.github.juggernaut.macchar.session.SessionManager;

import javax.net.ssl.SSLEngine;
import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author ameya
 */
public class MqttChannelFactory implements Function<SelectionKey, ChannelListener> {

    private final ActorSystem actorSystem;
    private final SessionManager sessionManager;
    private final Optional<Supplier<SSLEngine>> sslEngineSupplier;

    // NOTE: not supporting TLSv1.3 yet..
    private static final String[] ENABLED_PROTOCOLS = new String[] {"TLSv1.1", "TLSv1.2"};

    public MqttChannelFactory(ActorSystem actorSystem, SessionManager sessionManager, Optional<Supplier<SSLEngine>> sslEngineSupplier) {
        this.actorSystem = actorSystem;
        this.sessionManager = sessionManager;
        this.sslEngineSupplier = sslEngineSupplier;
    }

    @Override
    public ChannelListener apply(SelectionKey selectionKey) {
        final var fsm = new MqttChannelStateMachine(sessionManager);
        final var actor = actorSystem.createActor(fsm);
        final MqttChannel channel = sslEngineSupplier
                .map(ctx -> {
                    final var sslEngine = ctx.get();
                    return (MqttChannel) MqttTlsChannel.create(selectionKey, actor, sslEngine);
                })
                .orElseGet(() -> MqttChannel.create(selectionKey, actor));
        fsm.setMqttChannel(channel);
        return channel;
    }
}
