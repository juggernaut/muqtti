package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.UTF8Property;

import java.nio.ByteBuffer;

import static com.github.juggernaut.muqtti.property.PropertyIdentifiers.CONTENT_TYPE;

/**
 * @author ameya
 */
public class ContentType extends UTF8Property {

    public ContentType(String value) {
        super(CONTENT_TYPE, value);
    }

    public static ContentType fromBuffer(ByteBuffer buffer) {
        return new ContentType(decodeValue(buffer));
    }
}
