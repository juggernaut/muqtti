package com.github.juggernaut.macchar.packet;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class WillData {

    private final QoS willQoS;
    private final WillProperties willProperties;
    private final String willTopic;
    private final ByteBuffer willPayload;

    public WillData(QoS willQoS, WillProperties willProperties, String willTopic, ByteBuffer willPayload) {
        this.willQoS = willQoS;
        this.willProperties = willProperties;
        this.willTopic = willTopic;
        this.willPayload = willPayload;
    }

    public QoS getWillQoS() {
        return willQoS;
    }

    public WillProperties getWillProperties() {
        return willProperties;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public ByteBuffer getWillPayload() {
        return willPayload;
    }
}
