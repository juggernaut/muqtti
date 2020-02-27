package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.TwoByteIntegerProperty;

import java.nio.ByteBuffer;

import static com.github.juggernaut.muqtti.property.PropertyIdentifiers.TOPIC_ALIAS;

/**
 * @author ameya
 */
public class TopicAlias extends TwoByteIntegerProperty {

    public TopicAlias(int value) {
        super(TOPIC_ALIAS, value);
    }

    public static TopicAlias fromBuffer(ByteBuffer buffer) {
        return new TopicAlias(decodeValue(buffer));
    }
}
