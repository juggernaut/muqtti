package com.github.juggernaut.macchar.property;

/**
 * @author ameya
 */
public abstract class FourByteIntegerProperty extends MqttProperty {

    private final int value;

    protected FourByteIntegerProperty(int propertyIdentifier, int value) {
        super(propertyIdentifier);
        this.value = value;
    }

    @Override
    public int getEncodedLength() {
        // 2.2.2.2: Although the Property Identifier is defined as a Variable Byte Integer, in this version of the specification all of the Property Identifiers are one byte long
        return 1 + 4;
    }
}
