package com.example.fleet;

import com.example.fleet.service.MyCallCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import google.maps.fleetengine.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FleetApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(FleetApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }

}

