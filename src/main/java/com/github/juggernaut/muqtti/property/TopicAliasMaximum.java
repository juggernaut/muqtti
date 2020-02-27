package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.TwoByteIntegerProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class TopicAliasMaximum extends TwoByteIntegerProperty {

    public TopicAliasMaximum(int value) {
        super(PropertyIdentifiers.TOPIC_ALIAS_MAXIMUM, value);
    }

    public static TopicAliasMaximum fromBuffer(ByteBuffer buffer) {
        return new TopicAliasMaximum(TwoByteIntegerProperty.decodeValue(buffer));
    }
}
