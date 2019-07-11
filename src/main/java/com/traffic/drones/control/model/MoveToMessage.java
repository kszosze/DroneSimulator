package com.traffic.drones.control.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MoveToMessage {

    private String droneId;
    private Double longitude;
    private Double latitude;
    private String datetime;

}
