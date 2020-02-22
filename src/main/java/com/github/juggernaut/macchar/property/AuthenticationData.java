package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.property.types.BinaryDataProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class AuthenticationData extends BinaryDataProperty {

    public AuthenticationData(byte[] value) {
        super(PropertyIdentifiers.AUTHENTICATION_DATA, value);
    }

    public static AuthenticationData fromBuffer(ByteBuffer buffer) {
        return new AuthenticationData(BinaryDataProperty.decodeValue(buffer));
    }
}
