package com.traffic.drones.control.core;

import com.traffic.drones.control.model.CommandMessage;
import com.traffic.drones.control.model.MoveToMessage;
import com.traffic.drones.control.service.MessagingService;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.traffic.drones.control.config.ActiveMQConfig.DRONES_COMMANDS_QUEUE;
import static com.traffic.drones.control.config.ActiveMQConfig.DRONES_MOVEMENT_QUEUE;

@Slf4j
public class ControlDrone implements Runnable {

    private InputStream coordinatesFile;

    private MessagingService messageService;

    ControlDrone(InputStream coordinatesFile, MessagingService messageService) {
        this.coordinatesFile = coordinatesFile;
        this.messageService = messageService;
    }

    @Override
    public void run() {
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(coordinatesFile))) {
            final AtomicBoolean flag = new AtomicBoolean();
            flag.set(false);
            reader.lines().forEach(command -> {
                String[] strCommand = command.replace("\"", "").split(",");

                if (!isEndDay(strCommand[3])) {
                    MoveToMessage message = MoveToMessage
                            .builder()
                            .droneId(strCommand[0])
                            .longitude(Double.valueOf(strCommand[1]))
                            .latitude(Double.valueOf(strCommand[2]))
                            .datetime(strCommand[3])
                            .build();

                    messageService.send(DRONES_MOVEMENT_QUEUE, message);
                } else {
                    if (!flag.get()) {
                        flag.set(true);
                        log.info("Sending Shutdown to {} because time reach 8:10", strCommand[0]);
                        messageService.send(DRONES_COMMANDS_QUEUE,
                                CommandMessage
                                        .builder()
                                        .droneId(strCommand[0])
                                        .command("SHUTDOWN")
                                        .build());
                    }
                }
            });

        } catch (IOException e) {
            log.error("Coordinates file cannot be open ", e);
        }
    }

    private boolean isEndDay(String timestamp) {
        TemporalAccessor temporalAccessor = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(timestamp);
        return (temporalAccessor.get(ChronoField.HOUR_OF_DAY) == 8 &&
                temporalAccessor.get(ChronoField.MINUTE_OF_HOUR) >= 10);
    }
}
