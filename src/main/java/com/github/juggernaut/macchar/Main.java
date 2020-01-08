package com.github.juggernaut.macchar;

public class Main {

    public static void main(String[] args) throws Exception {
        final var mqttServer = new MqttServer(new MqttChannelListenerFactory());
        mqttServer.start();
    }
}
