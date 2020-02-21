package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.property.types.UTF8Property;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class AuthenticationMethod extends UTF8Property {

    public AuthenticationMethod(String value) {
        super(PropertyIdentifiers.AUTHENTICATION_METHOD, value);
    }

    public static AuthenticationMethod fromBuffer(ByteBuffer buffer) {
        return new AuthenticationMethod(UTF8Property.decodeValue(buffer));
    }
}
