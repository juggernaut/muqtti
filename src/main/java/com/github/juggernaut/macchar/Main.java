package com.github.juggernaut.macchar;

import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        final var forkJoinPool = Executors.newWorkStealingPool();
        final var actorSystem = new ActorSystem(forkJoinPool);
        final var mqttServer = new MqttServer(new MqttChannelFactory(actorSystem));
        mqttServer.start();
    }
}
