package com.github.juggernaut.macchar;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class UserProperty {

    private final String name;
    private final String value;

    public UserProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static UserProperty fromBuffer(final ByteBuffer buffer) {
        final String name = ByteBufferUtil.getUTF8String(buffer);
        final String value = ByteBufferUtil.getUTF8String(buffer);
        return new UserProperty(name, value);
    }
}
