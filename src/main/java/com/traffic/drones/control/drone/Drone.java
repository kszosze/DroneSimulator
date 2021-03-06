package com.traffic.drones.control.drone;

import com.traffic.drones.control.enums.ReportStatus;
import com.traffic.drones.control.model.CommandMessage;
import com.traffic.drones.control.model.MoveToMessage;
import com.traffic.drones.control.model.ReportMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Payload;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.traffic.drones.control.config.ActiveMQConfig.*;

@Slf4j
public class Drone implements IDrone {

    @Autowired
    private JmsTemplate jmsTemplate;

    private String droneId;

    private Map<String, Pair<Double, Double>> tubeMap;

    private AtomicBoolean shutdown = new AtomicBoolean(false);

    private Pair<Double, Double> position = Pair.of(0.0,0.0);

    private ArrayBlockingQueue<MoveToMessage> messagesQueue = new ArrayBlockingQueue<>(10);

    public Drone(String droneId) {
        this.droneId = droneId;
    }

    @JmsListener(destination = DRONES_COMMANDS_QUEUE, subscription = DRONES_MOVEMENT_QUEUE)
    public void processCommandsListener(@Payload CommandMessage message) {
        if (droneId.equals(message.getDroneId()) &&
            "SHUTDOWN".equals(message.getCommand())) {
                shutdown.set(true);
        }
    }

    @JmsListener(destination=DRONES_MOVEMENT_QUEUE, subscription = DRONES_MOVEMENT_QUEUE)
    public void processMovementsListener(@Payload MoveToMessage message) {

        if (droneId.equals(message.getDroneId())) {
            try {
                log.info("Receive next movement point to {}", message);
                messagesQueue.put(message);
            } catch (InterruptedException e) {
                log.error("Drone id {} cannot store message {}", droneId, message);
            }

        }

    }

    @Override
    public Pair<String, Pair<Double, Double>> getPosition() {
        return Pair.of(droneId, position);
    }

    void send(ReportMessage message) {
        log.info("sending report message  < {} >", message);
        jmsTemplate.convertAndSend(DRONES_REPORT_QUEUE, message);
    }

    @Override
    public void setTubeMap(Map<String, Pair<Double, Double>> tubeMap) {
        this.tubeMap = tubeMap;
    }

    @Override
    public void run() {

            while(!shutdown.get()) {
                MoveToMessage message = messagesQueue.poll();
                if (message != null) {
                    position = Pair.of(message.getLongitude(), message.getLatitude());
                    tubeMap.entrySet()
                            .stream()
                            .filter(tubeData -> isInRange(tubeData.getValue(), position))
                            .findFirst()
                            .ifPresent(tubeData ->
                                this.send(ReportMessage
                                        .builder()
                                        .droneId(droneId)
                                        .time(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(ZonedDateTime.now()))
                                        .speed("60mph")
                                        .tubeName(tubeData.getKey())
                                        .status(ReportStatus.values()[new Random().nextInt(3)])
                                        .build())
                            );
                }

            }

    }

    private boolean isInRange(Pair<Double, Double> tubeData, Pair<Double, Double> dronePosition) {
        return (15000/1000) >= calculateDistanceToPoint(tubeData.getRight(),
                tubeData.getLeft(),
                dronePosition.getRight(),
                dronePosition.getLeft());
    }

    private double calculateDistanceToPoint(double lat1, double lng1, double lat2, double lng2) {

        double rndLat1 = lat1 * Math.PI / 180;
        double rndLong1 = lng1 * Math.PI / 180;
        double rndLat2 = lat2 * Math.PI / 180;
        double rndLong2 = lng2 * Math.PI / 180;
        return 6378l * Math.acos( Math.cos( rndLat1 ) * Math.cos(( rndLat2 ) * Math.cos(( rndLong2 - rndLong1 ) + Math.sin( rndLat1 ) * Math.sin( rndLat2 ))));
    }
}
