package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.property.types.UTF8Property;

import java.nio.ByteBuffer;

import static com.github.juggernaut.macchar.property.PropertyIdentifiers.CONTENT_TYPE;

/**
 * @author ameya
 */
public class ContentType extends UTF8Property {

    public ContentType(String value) {
        super(CONTENT_TYPE, value);
    }

    public ContentType fromBuffer(ByteBuffer buffer) {
        return new ContentType(decodeValue(buffer));
    }
}
