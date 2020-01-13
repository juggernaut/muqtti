package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.packet.Publish;
import com.github.juggernaut.macchar.packet.Subscribe;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ameya
 */
public class SessionManager {

    private final ConcurrentMap<String, Session> sessionMap = new ConcurrentHashMap<>();

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
        public void onSubscribe(Subscribe subscribeMsg) {
            System.out.println("on subscribe called..");
        }
    }



}
