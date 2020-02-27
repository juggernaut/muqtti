package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.UTF8Property;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class AssignedClientIdentifier extends UTF8Property {

    public AssignedClientIdentifier(String value) {
        super(PropertyIdentifiers.ASSIGNED_CLIENT_IDENTIFIER, value);
    }

    public AssignedClientIdentifier fromBuffer(ByteBuffer buffer) {
        return new AssignedClientIdentifier(decodeValue(buffer));
    }
}
