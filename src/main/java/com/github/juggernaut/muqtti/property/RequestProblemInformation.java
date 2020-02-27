package com.github.juggernaut.muqtti.property;

import com.github.juggernaut.muqtti.property.types.ByteProperty;

import java.nio.ByteBuffer;

/**
 * @author ameya
 */
public class RequestProblemInformation extends ByteProperty {

    public RequestProblemInformation(byte value) {
        super(PropertyIdentifiers.REQUEST_PROBLEM_INFORMATION, value);
    }

    public static RequestProblemInformation fromBuffer(ByteBuffer buffer) {
        return new RequestProblemInformation(ByteProperty.decodeValue(buffer));
    }
}
