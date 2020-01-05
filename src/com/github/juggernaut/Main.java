package com.github.juggernaut;

public class Main {

    public static void main(String[] args) throws Exception {
	// write your code here
        final var echoServer = new EchoServer();
        echoServer.start();
    }
}
