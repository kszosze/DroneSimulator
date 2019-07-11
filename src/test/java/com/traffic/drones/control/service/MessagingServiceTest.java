package com.traffic.drones.control.service;

import com.traffic.drones.control.model.CommandMessage;
import com.traffic.drones.control.model.MoveToMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MessagingServiceTest {


    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private MessagingService messagingService;

    @Test
    public void sendMoveMessage() {

        messagingService.send("testQueue", MoveToMessage.builder().build());
        verify(jmsTemplate).convertAndSend(anyString(), any(MoveToMessage.class));
    }

    @Test
    public void send1() {
        messagingService.send("testQueue", CommandMessage.builder().build());
        verify(jmsTemplate).convertAndSend(anyString(), any(CommandMessage.class));

    }
}