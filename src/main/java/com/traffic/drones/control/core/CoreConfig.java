package com.traffic.drones.control.core;


import com.traffic.drones.control.drone.Drone;
import com.traffic.drones.control.drone.IDrone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfig {



    @Bean
    public IDrone createDrone1(){
       return new Drone("6043");
    }

    @Bean
    public IDrone createDrone2(){
       return new Drone("5937");
    }
}
