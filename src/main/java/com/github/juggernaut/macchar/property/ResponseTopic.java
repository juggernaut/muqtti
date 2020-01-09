package com.github.juggernaut.macchar.property;

import com.github.juggernaut.macchar.property.types.UTF8Property;

import java.nio.ByteBuffer;

import static com.github.juggernaut.macchar.property.PropertyIdentifiers.RESPONSE_TOPIC;

/**
 * @author ameya
 */
public class ResponseTopic extends UTF8Property {

    public ResponseTopic(String value) {
        super(RESPONSE_TOPIC, value);
    }

    public static ResponseTopic fromBuffer(ByteBuffer buffer) {
        return new ResponseTopic(decodeValue(buffer));
    }
}
