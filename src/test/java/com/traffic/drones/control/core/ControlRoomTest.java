package com.traffic.drones.control.core;

import com.traffic.drones.control.drone.IDrone;
import com.traffic.drones.control.model.CommandMessage;
import com.traffic.drones.control.model.MoveToMessage;
import com.traffic.drones.control.service.MessagingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.of;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ControlRoomTest {

    @Mock
    private MessagingService messagingService;

    @Mock
    private IDrone drone;

    private List<IDrone> droneList;

    private ControlRoom controlRoom;

    @Before
    public void setUp() {
        droneList = of(drone);
        controlRoom = new ControlRoom(droneList, messagingService);
    }

    @Test
    public void testLaunch() {
        ArgumentCaptor<Map> tubeMapCaptor = ArgumentCaptor.forClass(Map.class);
        controlRoom.launch();
        verify(drone).setTubeMap(tubeMapCaptor.capture());
    }


    @Test
    public void shutdown() {
        ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CommandMessage> messageCaptor = ArgumentCaptor.forClass(CommandMessage.class);
        controlRoom.shutdown();
        verify(messagingService, times(2)).send(queueNameCaptor.capture(), messageCaptor.capture());
    }

    @Test
    public void sendDroneTo() {
        ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MoveToMessage> messageCaptor = ArgumentCaptor.forClass(MoveToMessage.class);
        controlRoom.sendDroneTo("droneId", 0.0, 0.0);
        verify(messagingService).send(queueNameCaptor.capture(), messageCaptor.capture());

    }

}