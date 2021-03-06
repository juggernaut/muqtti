Muqtti
===

A zero-dependency [MQTT v5.0](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html) broker implementation in Java 11.

Features
---

* In-memory storage of QoS 1 messages
* Certificate-based Mutual TLS
* Most MQTT v5.0 features like:
    * Shared subscriptions
    * Message properties
    * Flow control
    * NACK packets

Not Supported
---

* QoS 2
* Retained messages
* AUTH packet
* Websockets

Use cases
---

* As a lightweight broker for local development
* Embedded broker for unit tests (JUnit rule coming soon)
* Extensible broker for custom authentication schemes


Getting Started
---

You'll need at least [Java 11](https://openjdk.java.net/projects/jdk/11/) to run Muqtti. The easiest way to get started is to download the [latest release](https://github.com/juggernaut/muqtti/releases/tag/v1.0.0) jar and run it:

```
java -jar muqtti-1.0.0.jar
```

Muqtti is a standard maven project, so you can also build from source using:
```
mvn -B package
```

Usage
---

Once you have Muqtti running, you can use pub/sub commands bundled with [mosquitto](https://mosquitto.org) as clients to Muqtti.

To subscribe:
```
mosquitto_pub -V 5 -t vehicles/teslas/1 
```

To publish:
```
mosquitto_pub -V 5 -t vehicles/teslas/1 -m 'wake up'
```

*NOTE*: You'll need to specify `-V 5` because `mosquitto` tries to connect using MQTT 3.1.1 by default, and Muqtti only supports MQTT v5.0

### Shared subscriptions

An interesting addition to MQTT v5.0 is the concept of shared subscriptions. With shared subscriptions, you can have multiple clients subscribed
to the same topic filter and have messages load-balanced among the clients. You can test this feature out by running two separate 
`mosquitto_sub` clients with the command line:

```
mosquitto_sub -V 5 -t '$share/group1/#'
```

Then publish messages to any topic - you should see the messages alternate between the clients. You can read more about shared subscriptions in
the [spec](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901250)

Configuration
---

Muqtti is configured using Java system properties.

### TLS
To enable TLS, pass it the following properties:

```
-Dmuqtti.port=8883 -Dmuqtti.certfile=/path/to/servercert.pem -Dmuqtti.keyfile=/path/to/server.pkcs8.private.key -Dmuqtti.cafile=/path/to/rootcacert.pem
```
These properties are equivalent to the `certfile`, `keyfile` and `cafile` configuration options in [mosquitto](https://mosquitto.org/man/mosquitto-conf-5.html)

Note that only PKCS#8 RSA private keys are supported - if you have keys generated by `openssl` you can converted them to PKCS#8 using:

```
openssl pkcs8 -topk8 -nocrypt -in <openssl_generated_key> -out <pkcs8_key>
```

### Logging

Muqtti uses `java.util.logging` (aka j.u.l) that comes with the JDK. You can specify a custom logging.properties file using:

```
-Djava.util.logging.config.file=/path/to/custom-logging.properties
```

A logging configuration file that enables verbose logging is included in the repo as an example (`muqtti-logging-example.properties`)

Motivation
---

I've written my fair share of networked services professionally. However, all my projects were built on top of open source networking libraries
like the excellent [Netty Project](https://netty.io/). My goal was to create a project using only the standard JDK so I could delve into lower
level aspects like protocol encoding/decoding using `ByteBuffer`s and asynchronous I/O using `Selector`s.

It also demonstrates the power of the Java platform and shows its relevance even when it is losing popularity among the trendier tech demographics. This is not to say that the platform doesn't have its rough edges (hello, `SSLEngine`), but overall, it provides a solid base for  programmers to build software of moderate-to-high complexity without having to tear their hair out. 

Scale-out MQTT v5.0 broker
---

If you're looking for a horizontally scalable, easily deployable (in the cloud or on-prem) MQTT v5.0 broker that plays well with Kubernetes, contact me
at lokare.ameya@gmail.com
