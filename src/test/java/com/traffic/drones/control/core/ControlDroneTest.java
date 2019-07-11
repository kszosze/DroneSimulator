package com.traffic.drones.control.core;

import com.traffic.drones.control.model.CommandMessage;
import com.traffic.drones.control.model.MoveToMessage;
import com.traffic.drones.control.service.MessagingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ControlDroneTest {

    private ControlDrone controlDrone;


    private Path coordinatesFile;

    @Mock
    private MessagingService messageService;

    @Test
    public void testSendMovementCommand() throws URISyntaxException {
        Path path = Paths.get(ClassLoader.getSystemResource("intimeroute.csv").toURI());

        ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MoveToMessage> messageCaptor = ArgumentCaptor.forClass(MoveToMessage.class);

        controlDrone = new ControlDrone(path, messageService);
        controlDrone.run();
        verify(messageService).send(queueNameCaptor.capture(), messageCaptor.capture());
    }

    @Test
    public void testSendShutDownCommand() throws URISyntaxException {
        Path path = Paths.get(ClassLoader.getSystemResource("outoftimeroute.csv").toURI());

        ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CommandMessage> messageCaptor = ArgumentCaptor.forClass(CommandMessage.class);

        controlDrone = new ControlDrone(path, messageService);
        controlDrone.run();
        verify(messageService).send(queueNameCaptor.capture(), messageCaptor.capture());
    }

}