package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.Actor;
import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.fsm.events.SendDisconnectEvent;
import com.github.juggernaut.macchar.packet.Disconnect;
import com.github.juggernaut.macchar.packet.Publish;
import com.github.juggernaut.macchar.packet.Subscribe;
import com.github.juggernaut.macchar.packet.WillData;
import com.github.juggernaut.macchar.property.SubscriptionIdentifier;
import com.github.juggernaut.macchar.property.types.FourByteIntegerProperty;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author ameya
 */
public class SessionManager {

    private final ConcurrentMap<String, Session> sessionMap = new ConcurrentHashMap<>();
    private final SubscriptionManager subscriptionManager;

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public SessionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    public Session newSession(final String id, final Actor actor, long sessionExpiryInterval, Optional<WillData> willData)  {
        final var candidateSession = new DefaultSession(id, actor, sessionExpiryInterval, willData);
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
        private Actor actor;
        private long sessionExpiryInterval;
        private Optional<WillData> willData;
        // topic filter -> session subscription
        private final Map<String, SessionSubscription> sessionSubscriptions = new HashMap<>();

        private Future<?> sessionExpiryTask;
        private Future<?> willExpiryTask;

        private volatile boolean connected;

        private volatile boolean expired;

        DefaultSession(String id, Actor actor, long sessionExpiryInterval, Optional<WillData> willData) {
            this.id = id;
            this.actor = actor;
            this.sessionExpiryInterval = sessionExpiryInterval;
            this.willData = willData;
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
        public void onDisconnect(DisconnectCause cause) {
            connected = false;
            // Remove will message first, because if session is to be expired right away, we need to remove the will message
            // to avoid erroneous Will message sending
            if (cause == DisconnectCause.NORMAL_CLIENT_INITIATED) {
                // The Will Message MUST be removed from the stored Session State in the Server once it has been published or the Server has received a DISCONNECT packet with a Reason Code of 0x00 (Normal disconnection) from the Client [MQTT-3.1.2-10]
                willData = Optional.empty();
            }
            if (sessionExpiryInterval == 0) {
                onSessionExpiry();
            } else {
                // Just deactivate for now, we'll reactivate them if the clientId connects again
                deactivateSubscriptions();
                sessionExpiryTask = scheduledExecutorService.schedule(this::onSessionExpiry, sessionExpiryInterval, TimeUnit.SECONDS);
            }
            if (cause != DisconnectCause.NORMAL_CLIENT_INITIATED) {
                willData.ifPresent(wd -> {
                    final long willDelayInterval = wd.getWillProperties().getWillDelayInterval()
                            .map(FourByteIntegerProperty::getValue).orElse(0L);
                    // if session expiry is less than or equal to will delay, that will take care of the Will message, no need to set any timer
                    // here. Otherwise, if the will delay is less than session expiry, then we need to schedule the timer
                    if (willDelayInterval < sessionExpiryInterval) {
                        willExpiryTask = scheduledExecutorService.schedule(this::publishWillMessageIfPresent, willDelayInterval, TimeUnit.SECONDS);
                    }
                });
            }
        }

        private synchronized void publishWillMessageIfPresent() {
            willData.ifPresent(wd -> {
                // TODO: check that will retain is false (since retain is not supported)
                final Publish willPublishMsg = Publish.create(wd.getWillQoS(), false, false, wd.getWillTopic(),
                        Optional.of(1), wd.getWillPayload()); // packetId doesn't really matter here since outgoing publish will get their own packet ids
                // fake that a publish has been received
                subscriptionManager.onPublishReceived(willPublishMsg);
                willData = Optional.empty();
            });
        }

        // Synchronized because the session could be reactivated at the same time as the expiry is triggered
        private synchronized void onSessionExpiry() {
            expired = true;
            remove();
            publishWillMessageIfPresent();
        }

        @Override
        public void remove() {
            if (isConnected()) {
                throw new IllegalStateException("Cannot remove a session while it is actively connected");
            }
            deactivateSubscriptions();
            deleteSubscriptions();
            removeSession(id);
        }

        private void deactivateSubscriptions() {
            sessionSubscriptions.values().forEach(SessionSubscription::deactivate);
        }

        private void deleteSubscriptions() {
            sessionSubscriptions.values().forEach(SessionSubscription::delete);
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
            // TODO: we're only handling new subscriptions here, need to handle existing subscriptions according to
            // TODO: [MQTT-3.8.4-3]
            subscribe.getSubscriptions().stream()
                    .filter(subscription -> !sessionSubscriptions.containsKey(subscription.getFilter()))
                    .forEach(subscription -> {
                        final var subscriptionState = subscriptionManager.getOrCreateSubscription(subscription.getFilter());
                        final var sessionSubscription = SessionSubscription.from(subscription,
                                subscribe.getProperties().getSubscriptionIdentifier().map(SubscriptionIdentifier::getValue),
                                this,
                                subscriptionState);
                        sessionSubscriptions.put(subscription.getFilter(), sessionSubscription);
                        System.out.println("Added session subscription for filter " + subscription.getFilter());
                    });
        }

        @Override
        public void sendDisconnect(Disconnect disconnect, DisconnectCause cause) {
            actor.sendMessage(new SendDisconnectEvent(disconnect));
            // Do this here, don't wait for the socket to be disconnected because the caller needs to know if
            // the session expired
            onDisconnect(cause);
        }

        @Override
        public List<Publish> readAvailableQoS1Messages(int maxMessages) {
            assert maxMessages > 0;
            final List<Publish> messages = new ArrayList<>();
            int remaining = maxMessages;
            for (SessionSubscription s: sessionSubscriptions.values()) {
                if (s.getSubscriptionMaxQoS() == QoS.AT_MOST_ONCE) {
                    continue;
                }
                s.readQoS1Messages(messages, remaining);
                remaining = maxMessages - messages.size();
                if (remaining <= 0) {
                    break;
                }
            }
            return messages;
        }

        @Override
        public synchronized void reactivate(Actor actor) {
            if (expired) {
                throw new IllegalStateException("Cannot reactivate expired session");
            }
            System.out.println("Reactivating session " + id);
            if (sessionExpiryTask != null) {
                sessionExpiryTask.cancel(true);
                sessionExpiryTask = null;
            }
            // If a new Network Connection to this Session is made before the Will Delay Interval has passed, the Server MUST NOT send the Will Message [MQTT-3.1.3-9]
            if (willExpiryTask != null) {
                willExpiryTask.cancel(true);
                willExpiryTask = null;
            }
            this.actor = actor;
            sessionSubscriptions.values().forEach(SessionSubscription::reactivate);
        }

        @Override
        public Actor getActor() {
            return actor;
        }
    }

}
