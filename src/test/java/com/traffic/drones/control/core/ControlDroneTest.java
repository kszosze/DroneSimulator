package com.traffic.drones.control.core;

import com.traffic.drones.control.model.CommandMessage;
import com.traffic.drones.control.model.MoveToMessage;
import com.traffic.drones.control.service.MessagingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ControlDroneTest {

    private ControlDrone controlDrone;


    private Path coordinatesFile;

    @Mock
    private MessagingService messageService;

    @Test
    public void testSendMovementCommand() {

        MoveToMessage expectedMessage = MoveToMessage.builder()
                .droneId("6043")
                .longitude(51.479012)
                .latitude(-0.172525)
                .datetime("2011-03-22 07:49:15")
                .build();
        InputStream fileStream = ClassLoader.getSystemResourceAsStream("intimeroute.csv");

        ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MoveToMessage> messageCaptor = ArgumentCaptor.forClass(MoveToMessage.class);

        controlDrone = new ControlDrone(fileStream, messageService);
        controlDrone.run();
        verify(messageService).send(queueNameCaptor.capture(), messageCaptor.capture());

        MoveToMessage moveToMessage = messageCaptor.getValue();
        String queueName = queueNameCaptor.getValue();

        assertThat(queueName).isEqualTo("moves-queue");
        assertThat(moveToMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void testSendShutDownCommand() {
        CommandMessage expectedCommand = CommandMessage.builder().droneId("6043").command("SHUTDOWN").build();
        InputStream fileStream = ClassLoader.getSystemResourceAsStream("outoftimeroute.csv");

        ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CommandMessage> messageCaptor = ArgumentCaptor.forClass(CommandMessage.class);

        controlDrone = new ControlDrone(fileStream, messageService);
        controlDrone.run();
        verify(messageService).send(queueNameCaptor.capture(), messageCaptor.capture());

        String queueName = queueNameCaptor.getValue();
        CommandMessage commandMessage = messageCaptor.getValue();

        assertThat(queueName).isEqualTo("commands-queue");
        assertThat(commandMessage).isEqualTo(expectedCommand);
    }

}