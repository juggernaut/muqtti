package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.session.SessionManager;
import com.github.juggernaut.macchar.session.SubscriptionManager;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        final int port = Integer.getInteger("port", 1883);
        final Optional<SSLContext> sslContext = getSSLContext();
        final var forkJoinPool = Executors.newWorkStealingPool();
        final var actorSystem = new ActorSystem(forkJoinPool);
        final var subscriptionManager = new SubscriptionManager();
        final var sessionManager = new SessionManager(subscriptionManager);
        final var mqttServer = new MqttServer(new MqttChannelFactory(actorSystem, sessionManager, sslContext), port);
        mqttServer.start();
    }

    private static Optional<SSLContext> getSSLContext() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        final String certFile = System.getProperty("certfile");
        final String keyFile  = System.getProperty("keyfile");

        if (certFile.isBlank() && keyFile.isBlank()) {
            return Optional.empty();
        }

        if (certFile.isBlank() || keyFile.isBlank()) {
            throw new IllegalArgumentException("Both certfile and keyfile must be specified");
        }

        final var certChain = readCertChainFromFile(new File(certFile));
        final var privateKey = readPrivateKeyFromFile(new File(keyFile));

        final KeyStore ks = buildKeyStore(privateKey, certChain);
        final var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, null);

        final var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return Optional.of(sslContext);
    }

    private static PrivateKey readPrivateKeyFromFile(File file) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("-----BEGIN PRIVATE KEY") || line.startsWith("-----END PRIVATE KEY")) {
                    continue;
                }
                sb.append(line);
            }

            final byte[] encodedKey = Base64.getDecoder().decode(sb.toString());
            // NOTE: only RSA with PKCS8 supported
            final var kf = KeyFactory.getInstance("RSA");
            final var keySpec = new PKCS8EncodedKeySpec(encodedKey);
            return kf.generatePrivate(keySpec);
        }
    }

    private static X509Certificate[] readCertChainFromFile(final File certChainFile) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Collection<X509Certificate> certChain = (Collection<X509Certificate>) certificateFactory.generateCertificates(new FileInputStream(certChainFile));
            return certChain.toArray(new X509Certificate[0]);
        } catch (CertificateException | FileNotFoundException e) {
            throw new IllegalArgumentException("Unable to read certificate chain from file " + certChainFile.getAbsolutePath() + "; " + e.getMessage());
        }
    }

    private static KeyStore buildKeyStore(final PrivateKey privateKey, final X509Certificate[] certChain) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setKeyEntry("dummyalias", privateKey, null, certChain);
        return ks;
    }


}
