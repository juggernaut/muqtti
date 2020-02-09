package com.github.juggernaut.macchar;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.util.function.Supplier;

/**
 * @author ameya
 */
public class SSLEngineSupplier implements Supplier<SSLEngine> {

    public SSLEngineSupplier(SSLContext sslContext, SSLParameters sslParameters) {
        this.sslContext = sslContext;
        this.sslParameters = sslParameters;
    }

    private final SSLContext sslContext;
    private final SSLParameters sslParameters;

    @Override
    public SSLEngine get() {
        final var engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        engine.setSSLParameters(sslParameters);
        return engine;
    }
}
