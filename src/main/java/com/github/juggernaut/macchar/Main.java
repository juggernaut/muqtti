package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.session.SessionManager;
import com.github.juggernaut.macchar.session.SubscriptionManager;

import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        final var forkJoinPool = Executors.newWorkStealingPool();
        final var actorSystem = new ActorSystem(forkJoinPool);
        final var subscriptionManager = new SubscriptionManager();
        final var sessionManager = new SessionManager(subscriptionManager);
        final var mqttServer = new MqttServer(new MqttChannelFactory(actorSystem, sessionManager));
        mqttServer.start();
    }
}
