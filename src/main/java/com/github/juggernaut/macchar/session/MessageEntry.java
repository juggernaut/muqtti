package com.github.juggernaut.macchar.session;

import com.github.juggernaut.macchar.packet.Publish;

/**
 * A stored message
 *
 * @author ameya
 */
public class MessageEntry {

    private final Publish message;
    private final long receivedTime; // epoch

    private MessageEntry(Publish message, long receivedTime) {
        this.message = message;
        this.receivedTime = receivedTime;
    }

    public static MessageEntry forReceivedMessage(Publish message) {
        return new MessageEntry(message, System.currentTimeMillis());
    }

    public boolean isExpired() {
        //  If the Message Expiry Interval has passed and the Server has not managed to start onward delivery to a matching subscriber, then it MUST delete the copy of the message for that subscriber [MQTT-3.3.2-5]
        return message.getPublishProperties().getMessageExpiryInterval()
                .map(expiryInterval -> System.currentTimeMillis() >= (receivedTime + (expiryInterval.getValue() * 1000)))
                .orElse(false);
    }

    public Publish getMessage() {
        return message;
    }

    public long getReceivedTime() {
        return receivedTime;
    }
}
