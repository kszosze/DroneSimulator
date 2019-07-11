package com.traffic.drones.control.service;

import com.traffic.drones.control.model.CommandMessage;
import com.traffic.drones.control.model.MoveToMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessagingService {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void send(String queue, MoveToMessage message) {
        jmsTemplate.convertAndSend(queue, message);
    }

    public void send(String queue, CommandMessage message) {
        jmsTemplate.convertAndSend(queue, message);
    }
}
