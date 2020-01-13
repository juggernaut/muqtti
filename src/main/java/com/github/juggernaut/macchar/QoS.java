package com.github.juggernaut.macchar;

import java.util.Arrays;

/**
 * @author ameya
 */
public enum QoS {
    AT_MOST_ONCE(0),
    AT_LEAST_ONCE(1),
    EXACTLY_ONCE(2);

    private final int intValue;

    QoS(int intValue) {
        this.intValue = intValue;
    }

    public static QoS fromIntValue(int input) {
        return Arrays.stream(values()).filter(v -> v.intValue == input)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Illegal value " + input + " for QoS"));
    }

    public int getIntValue() {
        return intValue;
    }
}
