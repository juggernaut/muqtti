package com.github.juggernaut.macchar.fsm.events;

import com.github.juggernaut.macchar.exception.MqttException;

/**
 * @author ameya
 */
public class ExceptionEvent implements Event {

    private final MqttException exception;

    public ExceptionEvent(MqttException exception) {
        this.exception = exception;
    }

    public MqttException getException() {
        return exception;
    }
}
