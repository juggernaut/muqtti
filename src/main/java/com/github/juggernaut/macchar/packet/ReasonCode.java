package com.github.juggernaut.macchar.packet;

import java.util.Arrays;

/**
 * @author ameya
 */
public enum ReasonCode {
    GRANTED_QOS_0(0x0),
    GRANTED_QOS_1(0x01),
    SESSION_TAKEN_OVER(0x8E),
    ;

    private final int value;

    ReasonCode(final int value) {
        this.value = value;
    }

    public static ReasonCode fromIntValue(final int input) {
        return Arrays.stream(values()).filter(v -> v.value == input)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Illegal value " + input + " for Reason code"));
    }

    public int getValue() {
        return value;
    }
}
