package com.traffic.drones.control.model;

import com.traffic.drones.control.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMessage {

    private String droneId;
    private String tubeName;
    private String time;
    private String speed;
    private ReportStatus status;
}
