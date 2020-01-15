package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.fsm.MqttChannelStateMachine;
import com.github.juggernaut.macchar.fsm.events.PacketReceivedEvent;
import com.github.juggernaut.macchar.packet.Connect;
import com.github.juggernaut.macchar.session.Session;
import com.github.juggernaut.macchar.session.SessionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.github.juggernaut.macchar.fsm.MqttChannelStateMachine.State.ESTABLISHED;
import static com.github.juggernaut.macchar.packet.MqttPacket.PacketType.CONNECT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author ameya
 */
@RunWith(MockitoJUnitRunner.class)
public class MqttChannelStateMachineTest {

    @Mock private MqttChannel channel;
    @Mock private Connect connect;
    @Mock private SessionManager sessionManager;
    @Mock private Session session;
    @Mock private Actor actor;

    private MqttChannelStateMachine fsm;


    @Before
    public void setUp() {
        fsm = new MqttChannelStateMachine(channel, sessionManager);
        fsm.setActor(actor);
        fsm.init();
        when(connect.getPacketType()).thenReturn(CONNECT);
        when(sessionManager.newSession(anyString(), any(Actor.class), 0L)).thenReturn(session);
    }

    @Test
    public void testInitToConnected() {
        assertTrue(fsm.onEvent(new PacketReceivedEvent(connect)));
        assertEquals(ESTABLISHED, fsm.getState());
    }
}
