package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.events.PacketReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.juggernaut.macchar.MqttChannelStateMachine.State.CONNECTION_ESTABLISHED;
import static com.github.juggernaut.macchar.MqttChannelStateMachine.State.INIT;
import static com.github.juggernaut.macchar.MqttChannelStateMachine.Transition.transition;

/**
 * @author ameya
 */
public class MqttChannelStateMachine implements StateMachine {

    private MqttChannel mqttChannel;

    enum State {
        INIT,
        CONNECTION_ESTABLISHED
    }

    static class Transition<E extends Event> {
        public final State fromState;
        public final State toState;
        public final Class<E> eventClass;
        public final Predicate<E> guard;
        public final Consumer<E> action;

        private Transition(State fromState, State toState, final Class<E> eventClass, Predicate<E> guard,
                           Consumer<E> action) {
            this.fromState = fromState;
            this.toState = toState;
            this.eventClass = eventClass;
            this.guard = guard;
            this.action = action;
        }

        public static <T> Transition transition(State fromState, State toState, Class<T> eventClass, Predicate<T> guard,
                                                Consumer<T> action) {
            return new Transition(fromState, toState, eventClass, guard, action);
        }

        public E cast(final Event event) {
            if (event.getClass().equals(eventClass)) {
                return ((E) event);
            }
            throw new IllegalArgumentException("Input event is not the same class as expected event");
        }
    }

    private volatile State currentState;

    private final List<Transition> transitions = List.of(
            transition(INIT, CONNECTION_ESTABLISHED, PacketReceivedEvent.class, this::isConnect, this::sendConnAck)
    );

    public MqttChannelStateMachine(MqttChannel mqttChannel) {
        this.mqttChannel = mqttChannel;
    }

    public MqttChannelStateMachine() {}

    public void setMqttChannel(final MqttChannel mqttChannel) {
        this.mqttChannel = mqttChannel;
    }

    @Override
    public void init() {
        currentState = INIT;
        System.out.println("MQTT state machine initialized");
    }

    @Override
    public boolean onEvent(Event event) {
        final Optional<Transition> applicableTransition = transitions.stream()
                .filter(t -> t.fromState == currentState)
                .filter(t -> event.getClass().equals(t.eventClass)) // could be 'isAssignableFrom' here to be a little lenient..
                .filter(t -> t.guard.test(t.cast(event)))
                .findFirst();
        applicableTransition.ifPresentOrElse(t -> {
            try {
                t.action.accept(t.cast(event));
                currentState = t.toState;
            } catch (Exception e) {
                System.out.println("Unhandled exception during state machine action for event " + event.getClass());
                e.printStackTrace();
            }
        }, () -> {
            System.out.println("Event " + event.getClass() + " not applicable at state " + currentState);
        });
        return applicableTransition.isPresent();
    }

    private boolean isConnect(final PacketReceivedEvent event) {
        return event.getPacket().getPacketType() == MqttPacket.PacketType.CONNECT;
    }

    private void sendConnAck(final PacketReceivedEvent event) {
        assert isConnect(event);
        final String clientId = ((MqttConnect) event.getPacket()).getClientId();
        String assignedClientId = null;
        if (clientId.isEmpty()) {
            // A Server MAY allow a Client to supply a ClientID that has a length of zero bytes, however if it does so the Server MUST treat this as a special case and assign a unique ClientID to that Client [MQTT-3.1.3-6]
            assignedClientId = "auto-" + UUID.randomUUID().toString();
        }
        // we don't have sessions yet, so alwasy send false
        final var connAck = new MqttConnAck(MqttConnAck.ConnectReasonCode.SUCCESS, false, Optional.ofNullable(assignedClientId));
        mqttChannel.sendPacket(connAck);
        System.out.println("Sent CONNACK");
    }

    protected State getState() {
        return currentState;
    }


    @Override
    public void shutDown() {

    }
}
