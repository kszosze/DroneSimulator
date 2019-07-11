package com.traffic.drones.control.core;

import com.google.common.annotations.VisibleForTesting;
import com.traffic.drones.control.drone.IDrone;
import com.traffic.drones.control.model.CommandMessage;
import com.traffic.drones.control.model.MoveToMessage;
import com.traffic.drones.control.model.ReportMessage;
import com.traffic.drones.control.service.MessagingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static com.traffic.drones.control.config.ActiveMQConfig.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Slf4j
@ShellComponent
public class ControlRoom {

    @Autowired
    private List<IDrone> droneList;

    @Autowired
    private MessagingService messageService;

    private AbstractMap<String, Pair<Double, Double>> tubeMap = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newFixedThreadPool(5);

    private Boolean dronesInAir = FALSE;

    @VisibleForTesting
    ControlRoom(List<IDrone> droneList, MessagingService messageService) {
        this.droneList = droneList;
        this.messageService = messageService;
    }

    @JmsListener(destination = DRONES_REPORT_QUEUE)
    private void reportListener(@Payload ReportMessage reportMessage){
        log.info("## Report received from {} at tube station : {} status : {}",
                reportMessage.getDroneId(),
                reportMessage.getTubeName(),
                reportMessage.getStatus());
    }

    @ShellMethod("Launch drones")
    public String launch() {

        try {
            Path path = Paths.get(ClassLoader.getSystemResource("tube.csv").toURI());
            try(Stream<String> lines = Files.lines(path)) {
                lines.forEach(tubeInfo -> {
                    String[] tubeInfoArr = tubeInfo.split(",");
                    tubeMap.put(tubeInfoArr[0],
                            Pair.of(Double.valueOf(tubeInfoArr[1]),
                                    Double.valueOf(tubeInfoArr[2])));
                });
            }
        } catch (URISyntaxException | IOException e) {
            log.error("Upps, something wrong happen", e);
        }

        List<Future> futureList = new ArrayList<>();

        droneList.forEach(drone -> {
            drone.setTubeMap(tubeMap);
            futureList.add(executor.submit(drone));
        });
        dronesInAir = TRUE;
        return "Launching drones";
    }

    @ShellMethod("List actual Drones Positions")
    @ShellMethodAvailability("dronesInAir")
    public String locateDrones() {
        StringBuilder sb = new StringBuilder();
        droneList.stream().map(IDrone::getPosition).map(position ->
                position.getLeft() + " - " + position.getRight().getLeft() + ":" + position.getRight().getRight())
                .forEach(sb::append);
        return sb.toString();
    }

    @ShellMethod("Shutdown drones")
    public String shutdown() {
        if (!executor.isTerminated() && !executor.isShutdown()) {
            messageService.send(DRONES_COMMANDS_QUEUE,
                    CommandMessage
                            .builder()
                            .droneId("6043")
                            .command("SHUTDOWN")
                            .build());
            messageService.send(DRONES_COMMANDS_QUEUE,
                    CommandMessage
                            .builder()
                            .droneId("5937")
                            .command("SHUTDOWN")
                            .build());
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            //System.exit(0);
            return "System shutdown";
        } else {
            return "System is already shutting down, please wait";
        }
    }

    @ShellMethod("Send position to drone -  droneId, longitude, latitude")
    @ShellMethodAvailability("dronesInAir")
    public String sendDroneTo(String droneId, double longitude, double latitude) {
        MoveToMessage message = MoveToMessage
                .builder()
                .droneId(droneId)
                .longitude(longitude)
                .latitude(latitude)
                .datetime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(ZonedDateTime.now()))
                .build();
        messageService.send(DRONES_MOVEMENT_QUEUE, message);

        return "Sending drone " + droneId + " to " + longitude + ":" + latitude;
    }

    @ShellMethod("Start automatic fly mode")
    @ShellMethodAvailability("dronesInAir")
    public String autonomousFly() {

        try {
            executor.submit(new ControlDrone(
                    Paths.get(ClassLoader.getSystemResource("6043.csv").toURI()),
                    messageService)
            );

            executor.submit(new ControlDrone(
                    Paths.get(ClassLoader.getSystemResource("5937.csv").toURI()),
                    messageService)
            );
        } catch (URISyntaxException e) {
            log.error("Had been a problem processing coordinates files", e);
        }
        return "Starting autonomous mode";
    }

    public Availability dronesInAir() {
        return dronesInAir || !executor.isTerminated() && !executor.isShutdown() ?
                  Availability.available()
                : Availability.unavailable("No Drones in Air");
    }
}
