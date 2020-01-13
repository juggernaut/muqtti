package com.github.juggernaut.macchar.fsm;

import com.github.juggernaut.macchar.Event;
import com.github.juggernaut.macchar.MqttChannel;
import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.fsm.events.PacketReceivedEvent;
import com.github.juggernaut.macchar.packet.*;
import com.github.juggernaut.macchar.session.Session;
import com.github.juggernaut.macchar.session.SessionManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.juggernaut.macchar.fsm.MqttChannelStateMachine.State.CONNECTION_ESTABLISHED;
import static com.github.juggernaut.macchar.fsm.MqttChannelStateMachine.State.INIT;
import static com.github.juggernaut.macchar.fsm.MqttChannelStateMachine.Transition.transition;

/**
 * @author ameya
 */
public class MqttChannelStateMachine implements StateMachine {

    private MqttChannel mqttChannel;

    private final SessionManager sessionManager;

    private State currentState;

    private Session session;

    private static final QoS MAX_SUPPORTED_QOS = QoS.AT_LEAST_ONCE;

    public enum State {
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


    private final List<Transition> transitions = List.of(
            transition(INIT, CONNECTION_ESTABLISHED, PacketReceivedEvent.class, this::isConnect, this::handleConnect),
            transition(CONNECTION_ESTABLISHED, CONNECTION_ESTABLISHED, PacketReceivedEvent.class, this::isSubscribe, this::handleSubscribe)
    );

    public MqttChannelStateMachine(MqttChannel mqttChannel, SessionManager sessionManager) {
        this.mqttChannel = mqttChannel;
        this.sessionManager = sessionManager;
    }

    public MqttChannelStateMachine(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

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

    private boolean isSubscribe(final PacketReceivedEvent event) {
        return event.getPacket().getPacketType() == MqttPacket.PacketType.SUBSCRIBE;
    }

    private void handleConnect(final PacketReceivedEvent event) {
        assert isConnect(event);
        final String clientId = ((Connect) event.getPacket()).getClientId();
        String assignedClientId = null;
        if (clientId.isEmpty()) {
            // A Server MAY allow a Client to supply a ClientID that has a length of zero bytes, however if it does so the Server MUST treat this as a special case and assign a unique ClientID to that Client [MQTT-3.1.3-6]
            assignedClientId = "auto-" + UUID.randomUUID().toString();
        }
        final String effectiveClientId = clientId.isEmpty() ? assignedClientId : clientId;
        session = sessionManager.newSession(effectiveClientId);
        // we aren't handing existing sessions yet, so always send SessionPresent = false
        final var connAck = new ConnAck(ConnAck.ConnectReasonCode.SUCCESS, false, Optional.ofNullable(assignedClientId));
        mqttChannel.sendPacket(connAck);
        System.out.println("Sent CONNACK");
    }

    private void handleSubscribe(final PacketReceivedEvent event) {
        assert isSubscribe(event);
        assert session != null;
        final var subscribe = ((Subscribe) event.getPacket());
        session.onSubscribe(subscribe);
        // The QoS of Application Messages sent in response to a Subscription MUST be the minimum of the QoS of the originally published message and the Maximum QoS granted by the Server [MQTT-3.8.4-8]
        final var reasonCodes = subscribe.getSubscriptions().stream()
                .map(subscription -> Math.min(MAX_SUPPORTED_QOS.getIntValue(), subscription.getQoS().getIntValue()))
                .map(SubAck.ReasonCode::fromIntValue)
                .collect(Collectors.toList());
        final var subAck = new SubAck(subscribe.getPacketId(), reasonCodes);
        mqttChannel.sendPacket(subAck);
        System.out.println("Sent SUBACK");
    }

    public State getState() {
        return currentState;
    }


    @Override
    public void shutDown() {

    }
}
