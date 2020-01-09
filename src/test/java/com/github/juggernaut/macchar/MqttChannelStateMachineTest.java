package com.github.juggernaut.macchar;

import com.github.juggernaut.macchar.events.PacketReceivedEvent;
import com.github.juggernaut.macchar.packet.Connect;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.github.juggernaut.macchar.MqttChannelStateMachine.State.CONNECTION_ESTABLISHED;
import static com.github.juggernaut.macchar.packet.MqttPacket.PacketType.CONNECT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author ameya
 */
@RunWith(MockitoJUnitRunner.class)
public class MqttChannelStateMachineTest {

    @Mock private MqttChannel channel;
    @Mock private Connect connect;

    private MqttChannelStateMachine fsm;


    @Before
    public void setUp() {
        fsm = new MqttChannelStateMachine(channel);
        fsm.init();
        when(connect.getPacketType()).thenReturn(CONNECT);
    }

    @Test
    public void testInitToConnected() {
        assertTrue(fsm.onEvent(new PacketReceivedEvent(connect)));
        assertEquals(CONNECTION_ESTABLISHED, fsm.getState());
    }
}
