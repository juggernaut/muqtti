package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.ByteBufferUtil;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class UserProperty extends MqttProperty {

    private final String name;
    private final String value;

    public UserProperty(String name, String value) {
        super(PropertyIdentifiers.USER_PROPERTY);
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int getEncodedLength() {
        return 1 + ByteBufferUtil.getEncodedUTF8StringLenghInBytes(name) + ByteBufferUtil.getEncodedUTF8StringLenghInBytes(value);
    }

    @Override
    protected void encodeValue(ByteBuffer buffer) {
        ByteBufferUtil.encodeUTF8String(buffer, name);
        ByteBufferUtil.encodeUTF8String(buffer, value);
    }

    public static UserProperty fromBuffer(final ByteBuffer buffer) {
        final String name = ByteBufferUtil.decodeUTF8String(buffer);
        final String value = ByteBufferUtil.decodeUTF8String(buffer);
        return new UserProperty(name, value);
    }
}
