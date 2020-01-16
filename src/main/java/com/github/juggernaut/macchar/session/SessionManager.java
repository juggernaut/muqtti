package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.Actor;
import com.github.juggernaut.macchar.fsm.events.SendDisconnectEvent;
import com.github.juggernaut.macchar.packet.Disconnect;
import com.github.juggernaut.macchar.packet.Publish;
import com.github.juggernaut.macchar.packet.Subscribe;
import com.github.juggernaut.macchar.property.SubscriptionIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ameya
 */
public class SessionManager {

    private final ConcurrentMap<String, Session> sessionMap = new ConcurrentHashMap<>();
    private final SubscriptionManager subscriptionManager;

    public SessionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    public Session newSession(final String id, final Actor actor, long sessionExpiryInterval)  {
        final var candidateSession = new DefaultSession(id, actor, sessionExpiryInterval);
        final var oldSessoin = sessionMap.putIfAbsent(id, candidateSession);
        if (oldSessoin != null) {
            throw new SessionIdAlreadyExists("Session id " + id + " already exists");
        }
        return candidateSession;
    }

    public Session removeSession(final String id) {
        System.out.println("Removing session " + id);
        return sessionMap.remove(id);
    }

    public Session getSession(final String id) {
        return sessionMap.get(id);
    }

    class DefaultSession implements Session {

        private final String id;
        private final Actor actor;
        private long sessionExpiryInterval;
        // topic filter -> session subscription
        private final Map<String, SessionSubscription> sessionSubscriptions = new HashMap<>();

        private volatile boolean connected;

        private volatile boolean expired;

        DefaultSession(String id, Actor actor, long sessionExpiryInterval) {
            this.id = id;
            this.actor = actor;
            this.sessionExpiryInterval = sessionExpiryInterval;
            connected = true;
            expired = false;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isExpired() {
            return expired;
        }

        @Override
        public void onDisconnect() {
            connected = false;
            // TODO: handle non-zero session expiry interval
            if (sessionExpiryInterval == 0) {
                expired = true;
                remove();
            }
        }

        @Override
        public void remove() {
            if (isConnected()) {
                throw new IllegalStateException("Cannot remove a session while it is actively connected");
            }
            sessionSubscriptions.values().forEach(SessionSubscription::deactivate);
            removeSession(id);
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public void onPublish(Publish publishMsg) {
            subscriptionManager.onPublishReceived(publishMsg);
        }

        @Override
        public void onSubscribe(Subscribe subscribe) {
            System.out.println("on subscribe called..");
            // TODO: we're only handling new subscriptions here, need to handle existing subscriptions according to
            // TODO: [MQTT-3.8.4-3]
            subscribe.getSubscriptions().stream()
                    .filter(subscription -> !sessionSubscriptions.containsKey(subscription.getFilter()))
                    .forEach(subscription -> {
                        final var subscriptionState = subscriptionManager.getOrCreateSubscription(subscription.getFilter());
                        final var sessionSubscription = SessionSubscription.from(subscription,
                                subscribe.getProperties().getSubscriptionIdentifier().map(SubscriptionIdentifier::getValue),
                                actor,
                                subscriptionState);
                        sessionSubscriptions.put(subscription.getFilter(), sessionSubscription);
                        System.out.println("Added session subscription for filter " + subscription.getFilter());
                    });
        }

        @Override
        public void sendDisconnect(Disconnect disconnect) {
            actor.sendMessage(new SendDisconnectEvent(disconnect));
            // Do this here, don't wait for the socket to be disconnected because the caller needs to know if
            // the session expired
            onDisconnect();
        }

        @Override
        public List<Publish> readAvailableQoS1Messages(int maxMessages) {
            assert maxMessages > 0;
            final List<Publish> messages = new ArrayList<>();
            int remaining = maxMessages;
            for (SessionSubscription s: sessionSubscriptions.values()) {
                s.readQoS1Messages(messages, remaining);
                remaining = maxMessages - messages.size();
                if (remaining <= 0) {
                    break;
                }
            }
            return messages;
        }
    }



}
