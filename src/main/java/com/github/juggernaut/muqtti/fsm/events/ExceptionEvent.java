package com.github.juggernaut.muqtti.fsm.events;

import com.github.juggernaut.muqtti.exception.MqttException;

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
