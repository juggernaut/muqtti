package com.github.juggernaut.macchar.fsm;

import com.github.juggernaut.macchar.Configuration;
import com.github.juggernaut.macchar.fsm.events.*;
import com.github.juggernaut.macchar.MqttChannel;
import com.github.juggernaut.macchar.QoS;
import com.github.juggernaut.macchar.packet.*;
import com.github.juggernaut.macchar.session.Session;
import com.github.juggernaut.macchar.session.SessionManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.juggernaut.macchar.fsm.MqttChannelStateMachine.State.*;
import static com.github.juggernaut.macchar.fsm.MqttChannelStateMachine.Transition.transition;

/**
 * @author ameya
 */
public class MqttChannelStateMachine extends ActorStateMachine {

    private MqttChannel mqttChannel;

    private final SessionManager sessionManager;

    private State currentState;

    private Session session;

    private int currentPublishPacketId = 1;
    private int earliestOutstandingPacketId = -1;

    private int clientReceiveMaximum = 10; // default

    private int outstandingQoS1Messages = 0;

    private boolean matchingQoS1MessagesAvailable = false;

    public enum State {
        INIT,
        ESTABLISHED,
        DISCONNECTED,
        CHANNEL_DISCONNECTED
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
            transition(INIT, ESTABLISHED, PacketReceivedEvent.class, this::isConnect, this::handleConnect),
            transition(ESTABLISHED, ESTABLISHED, PacketReceivedEvent.class, this::isSubscribe, this::handleSubscribe),
            transition(ESTABLISHED, ESTABLISHED, SendQoS0PublishEvent.class, p -> true, this::handleSendQoS0Publish),
            transition(ESTABLISHED, DISCONNECTED, PacketReceivedEvent.class, this::isPublishQoS2, this::handleQoS2PublishReceived),
            transition(ESTABLISHED, ESTABLISHED, PacketReceivedEvent.class, this::isPublish, this::handlePublishReceived),
            transition(ESTABLISHED, ESTABLISHED, QoS1PublishMatchedEvent.class, p -> true, this::handleQoS1PublishMatched),
            transition(ESTABLISHED, ESTABLISHED, PacketReceivedEvent.class, this::isPubAck, this::handlePubAckReceived),
            transition(ESTABLISHED, ESTABLISHED, PacketReceivedEvent.class, this::isPingReq, this::handlePingReq),
            transition(INIT, INIT, ChannelWriteReadyEvent.class, p -> true, e -> mqttChannel.flushWriteBuffer()), // for TLS handshake
            transition(ESTABLISHED, ESTABLISHED, ChannelWriteReadyEvent.class, p -> true, e -> mqttChannel.flushWriteBuffer()),
            transition(ESTABLISHED, CHANNEL_DISCONNECTED, ChannelDisconnectedEvent.class, p -> true, this::handleChannelDisconnected),
            transition(ESTABLISHED, DISCONNECTED, SendDisconnectEvent.class, p -> true, this::handleSendDisconnected),
            transition(DISCONNECTED, CHANNEL_DISCONNECTED, ChannelDisconnectedEvent.class, p -> true, p -> {})
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

    private boolean isPublish(final PacketReceivedEvent event) {
        return event.getPacket().getPacketType() == MqttPacket.PacketType.PUBLISH;
    }

    private boolean isPublishQoS2(final PacketReceivedEvent event) {
        return isPublish(event) && ((Publish) event.getPacket()).getQoS() == QoS.EXACTLY_ONCE;
    }

    private boolean isPubAck(final PacketReceivedEvent event) {
        return event.getPacket().getPacketType() == MqttPacket.PacketType.PUBACK;
    }

    private boolean isPingReq(final PacketReceivedEvent event) {
        return event.getPacket().getPacketType() == MqttPacket.PacketType.PINGREQ;
    }

    private void handleConnect(final PacketReceivedEvent event) {
        assert isConnect(event);
        final Connect connect = ((Connect) event.getPacket());
        final String clientId = connect.getClientId();
        String assignedClientId = null;
        if (clientId.isEmpty()) {
            // A Server MAY allow a Client to supply a ClientID that has a length of zero bytes, however if it does so the Server MUST treat this as a special case and assign a unique ClientID to that Client [MQTT-3.1.3-6]
            assignedClientId = "auto-" + UUID.randomUUID().toString();
        }
        final String effectiveClientId = clientId.isEmpty() ? assignedClientId : clientId;
        final boolean sessionPresent = getOrCreateSession(effectiveClientId, connect);
        final Optional<Integer> keepAliveProperty;
        final int keepAliveInSeconds;

        if (connect.getKeepAlive() > Configuration.MAX_KEEP_ALIVE) {
            keepAliveInSeconds = Configuration.MAX_KEEP_ALIVE;
            keepAliveProperty = Optional.of(Configuration.MAX_KEEP_ALIVE);
        } else {
            keepAliveInSeconds = connect.getKeepAlive();
            keepAliveProperty = Optional.empty();
        }

        mqttChannel.setKeepAliveTimeout((int) (keepAliveInSeconds * 1.5));

        final var connAck = new ConnAck(ConnAck.ConnectReasonCode.SUCCESS, sessionPresent, Optional.ofNullable(assignedClientId), keepAliveProperty);
        mqttChannel.sendPacket(connAck);
        System.out.println("Sent CONNACK");

        if (sessionPresent) {
            System.out.println("Detected existing session for " + session.getId());
            session.reactivate(getActor());
            // If there are available stored QoS1 messages for this session, start sending them
            System.out.println("Sending stored QoS1 messages if available");
            readAndSendQoS1MessagesIfAvailable();
        }
    }

    /**
     *
     * @param effectiveClientId
     * @param connect
     * @return true if existing (and valid) session, false if new session
     */
    private boolean getOrCreateSession(final String effectiveClientId, final Connect connect) {
        final long sessionExpiryInterval = connect.getConnectProperties()
                .map(ConnectProperties::getSessionExpiryInterval)
                // 3.1.2.11.2: If the Session Expiry Interval is absent the value 0 is used
                .orElse(0L);
        final Session oldSession = sessionManager.getSession(effectiveClientId);
        boolean newSessionRequired = false;
        if (oldSession != null) {
            if (oldSession.isConnected()) {
                // If the ClientID represents a Client already connected to the Server, the Server sends a DISCONNECT packet to the existing Client with Reason Code of 0x8E (Session taken over) as described in section 4.13 and MUST close the Network Connection of the existing Client [MQTT-3.1.4-3]
                oldSession.sendDisconnect(Disconnect.create(ReasonCode.SESSION_TAKEN_OVER), Session.DisconnectCause.SERVER_INITIATED);
                newSessionRequired = oldSession.isExpired();
            }
            if (connect.hasCleanStartFlag()) {
                // NOTE that this may be a redundant remove if the session expiry of the old session was 0 (it would have
                // removed itself)
                oldSession.remove();
                newSessionRequired = true;
            }
        } else {
            newSessionRequired = true;
        }

        if (newSessionRequired) {
            session = sessionManager.newSession(effectiveClientId, getActor(), sessionExpiryInterval, connect.getWillData());
        } else {
            session = oldSession;
        }
        return !newSessionRequired;
    }

    private void handleSubscribe(final PacketReceivedEvent event) {
        assert isSubscribe(event);
        assert session != null;
        final var subscribe = ((Subscribe) event.getPacket());
        session.onSubscribe(subscribe);
        // The QoS of Application Messages sent in response to a Subscription MUST be the minimum of the QoS of the originally published message and the Maximum QoS granted by the Server [MQTT-3.8.4-8]
        final var reasonCodes = subscribe.getSubscriptions().stream()
                .map(subscription -> Math.min(Configuration.MAX_SUPPORTED_QOS.getIntValue(), subscription.getQoS().getIntValue()))
                .map(ReasonCode::fromIntValue)
                .collect(Collectors.toList());
        final var subAck = new SubAck(subscribe.getPacketId(), reasonCodes);
        mqttChannel.sendPacket(subAck);
        System.out.println("Sent SUBACK");
    }

    private void handlePublishReceived(final PacketReceivedEvent event) {
        assert isPublish(event);
        assert session != null;
        final var publish = ((Publish) event.getPacket());
        System.out.println("Received PUBLISH, pubbing it internally");
        session.onPublish(publish);
        if (publish.getQoS() == QoS.AT_LEAST_ONCE) {
            sendPubAck(publish);
        }
    }

    private void sendPubAck(final Publish publish) {
        if (publish.getPacketId().isEmpty()) {
            throw new IllegalStateException("Publish packet with QoS 1 has no packet identifier, this is a serious programming error");
        }
        final var pubAck = PubAck.create(publish.getPacketId().get(), PubAck.ReasonCode.SUCCESS);
        mqttChannel.sendPacket(pubAck);
    }

    private void handleSendQoS0Publish(final SendQoS0PublishEvent event) {
        final var receivedPublish = event.getMsg();
        final var payloadToSend = receivedPublish.getPayload().slice();
        final var publishToSend = Publish.create(QoS.AT_MOST_ONCE, false, false, receivedPublish.getTopicName(),
                Optional.empty(), payloadToSend);
        mqttChannel.sendPacket(publishToSend);
        System.out.println("Sent QoS0 publish msg");
    }

    private void handleChannelDisconnected(final ChannelDisconnectedEvent event) {
        // channel can get disconnected even before we have an actual session
        if (session != null) {
            System.out.println("Session " + session.getId() + " disconnected unceremoniously");
            session.onDisconnect(Session.DisconnectCause.UNCEREMONIOUS);
        }
    }

    private void handleSendDisconnected(final SendDisconnectEvent event) {
        sendDisconnect(event.getMsg());
    }

    private void handleQoS2PublishReceived(final PacketReceivedEvent event) {
        assert isPublishQoS2(event);
        // [MQTT-3.2.2-11] It is a Protocol Error if the Server receives a PUBLISH packet with a QoS greater than the Maximum QoS it specified. In this case use DISCONNECT with Reason Code 0x9B (QoS not supported)
        final var disconnect = Disconnect.create(ReasonCode.QOS_NOT_SUPPORTED);
        sendDisconnect(disconnect);
    }

    private void sendDisconnect(final Disconnect disconnect) {
        System.out.println("Sending DISCONNECT to " + session.getId());
        mqttChannel.sendPacket(disconnect);
        mqttChannel.disconnect();
    }

    private void handleQoS1PublishMatched(final QoS1PublishMatchedEvent event) {
        matchingQoS1MessagesAvailable = true;
        readAndSendQoS1MessagesIfAvailable();
    }

    private void readAndSendQoS1MessagesIfAvailable() {
        int numCanSend = clientReceiveMaximum - outstandingQoS1Messages;
        if (numCanSend == 0) {
            System.out.println("Already at receive maximum, can't send more QoS1 messages");
            return;
        }
        final var availableMessages = session.readAvailableQoS1Messages(numCanSend);
        if (availableMessages.size() < numCanSend) {
            matchingQoS1MessagesAvailable = false;
        }
        if (availableMessages.size() > 0) {
            earliestOutstandingPacketId = currentPublishPacketId;
            availableMessages.stream()
                    .map(m -> Publish.create(QoS.AT_LEAST_ONCE, false, false, m.getTopicName(), Optional.of(incrementPublishPacketId()), m.getPayload().slice()))
                    .forEach(publish -> mqttChannel.sendPacket(publish));
            outstandingQoS1Messages += availableMessages.size();
        }
    }

    private void handlePubAckReceived(final PacketReceivedEvent event) {
        assert isPubAck(event);
        final var pubAck = (PubAck) event.getPacket();
        // Clients MUST send PUBACKs in order the PUBLISHes were received
        if (pubAck.getPacketId() != earliestOutstandingPacketId) {
            // TODO: handle this better
            throw new IllegalStateException("Invalid PUBACK received");
        }
        outstandingQoS1Messages--;
        earliestOutstandingPacketId = (earliestOutstandingPacketId + 1) % 65535;
        if (earliestOutstandingPacketId == 0) {
            earliestOutstandingPacketId = 1;
        }
        if (earliestOutstandingPacketId == currentPublishPacketId) {
            // This means nothing is outstanding
            earliestOutstandingPacketId = -1;
        }
        // this PUBACK may have freed up our send quota
        if (matchingQoS1MessagesAvailable) {
            readAndSendQoS1MessagesIfAvailable();
        }

    }

    private int incrementPublishPacketId() {
        final int currentId = currentPublishPacketId;
        currentPublishPacketId = (currentPublishPacketId + 1) % 65535;
        if (currentPublishPacketId == 0) {
            // Only non-zero packet ids are allowed according to spec
            currentPublishPacketId = 1;
        }
        return currentId;
    }

    private void handlePingReq(final PacketReceivedEvent event) {
        mqttChannel.sendPacket(PingResp.INSTANCE);
    }

    public State getState() {
        return currentState;
    }

    @Override
    public void shutDown() {

    }
}
