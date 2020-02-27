package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.VariableByteIntegerProperty;

import java.nio.ByteBuffer;

import static com.github.juggernaut.muqtti.property.PropertyIdentifiers.SUBSCRIPTION_IDENTIFIER;

/**
 * @author ameya
 */
public class SubscriptionIdentifier extends VariableByteIntegerProperty {

    public SubscriptionIdentifier(int value) {
        super(SUBSCRIPTION_IDENTIFIER, value);
    }

    public static SubscriptionIdentifier fromBuffer(ByteBuffer buffer) {
        return new SubscriptionIdentifier(decodeValue(buffer));
    }
}
