package com.traffic.drones.control.drone;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public interface IDrone extends Runnable {

    Pair<String, Pair<Double, Double>> getPosition();

    void setTubeMap(Map<String, Pair<Double, Double>> tubeMap);
}
