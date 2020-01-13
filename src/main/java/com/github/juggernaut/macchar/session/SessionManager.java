package com.github.juggernaut.macchar.session;

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

    public Session newSession(final String id)  {
        final var candidateSession = new DefaultSession(id);
        final var oldSessoin = sessionMap.putIfAbsent(id, candidateSession);
        if (oldSessoin != null) {
            throw new SessionIdAlreadyExists("Session id " + id + " already exists");
        }
        return candidateSession;
    }

    public Session removeSession(final String id) {
       return sessionMap.remove(id);
    }

    class DefaultSession implements Session {

        private final String id;
        // topic filter -> session subscription
        private final Map<String, SessionSubscription> sessionSubscriptions = new HashMap<>();

        DefaultSession(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public void onDisconnect() {

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
                        final var cursor = subscriptionState.newCursor();
                        final var sessionSubscription = SessionSubscription.from(subscription,
                                subscribe.getProperties().getSubscriptionIdentifier().map(SubscriptionIdentifier::getValue),
                                cursor);
                        sessionSubscriptions.put(subscription.getFilter(), sessionSubscription);
                        System.out.println("Added session subscription for filter " + subscription.getFilter());
                    });
        }
    }



}
