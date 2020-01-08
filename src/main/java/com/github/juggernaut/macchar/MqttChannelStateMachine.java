package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.events.PacketReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.juggernaut.macchar.MqttChannelStateMachine.State.CONNECTION_ESTABLISHED;
import static com.github.juggernaut.macchar.MqttChannelStateMachine.State.INIT;
import static com.github.juggernaut.macchar.MqttChannelStateMachine.Transition.transition;

/**
 * @author ameya
 */
public class MqttChannelStateMachine implements StateMachine {

    private final MqttChannel mqttChannel;

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

    private State currentState;

    private final List<Transition> transitions = List.of(
            transition(INIT, CONNECTION_ESTABLISHED, PacketReceivedEvent.class, this::isConnect, this::sendConnAck)
    );

    public MqttChannelStateMachine(MqttChannel mqttChannel) {
        this.mqttChannel = mqttChannel;
    }

    @Override
    public void init() {
        currentState = INIT;
    }

    @Override
    public void onEvent(Event event) {
        final Optional<Transition> applicableTransition = transitions.stream()
                .filter(t -> t.fromState == currentState)
                .filter(t -> event.getClass().equals(t.eventClass)) // could be 'isAssignableFrom' here to be a little lenient..
                .filter(t -> t.guard.test(t.cast(event)))
                .findFirst();
        applicableTransition.ifPresentOrElse(t -> t.action.accept(t.cast(event)), () -> {
            System.out.println("Event " + event.getClass() + " not applicable at state " + currentState);
        });
    }

    private boolean isConnect(final PacketReceivedEvent event) {
        return event.getPacket().getPacketType() == MqttPacket.PacketType.CONNECT;
    }

    private void sendConnAck(final PacketReceivedEvent event) {
        assert isConnect(event);
        // TODO: send conn ack via channel
        System.out.println("Sent CONNACK");
    }

    protected State getState() {
        return currentState;
    }


    @Override
    public void shutDown() {

    }
}
