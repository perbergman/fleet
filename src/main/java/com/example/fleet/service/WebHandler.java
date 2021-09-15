package com.example.fleet.service;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import google.maps.fleetengine.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class WebHandler {

    @Value("${fleet.api}")
    private String fleetApi;

    @Value("${jwt.server}")
    private String jwtServer;

    @Value("${project}")
    private String project;

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @GetMapping(value = "/vehicle/{id}")
    public ResponseEntity<String> queryVehicle(@PathVariable("id") String id) {
        log.info("queryVehicle --> " + id);

        String ret = null;
        Optional<String> token = getToken(id);

        if(token.isPresent()) {
            String arg = String.format("providers/%s/vehicles/%s", project, id);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(fleetApi).build();
            GetVehicleRequest vehicleRequest = GetVehicleRequest.newBuilder().setName(arg).build();
            var vehicle = VehicleServiceGrpc.newBlockingStub(channel).withCallCredentials(new MyCallCredentials(token.get())).getVehicle(vehicleRequest);
            try {
                ret = JsonFormat.printer().print(vehicle);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        log.info("queryVehicle <-- " + ret);
        return ResponseEntity.ok(ret);
    }

    @GetMapping(value = "/trips/{id}")
    public ResponseEntity<String> queryTrips(@PathVariable("id") String id) {
        log.info("queryTrips --> " + id);

        String ret = null;
        Optional<String> token = getToken(id);

        if(token.isPresent()) {
            String arg = String.format("providers/%s", project);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(fleetApi).build();

            SearchTripsRequest searchTripsRequest = SearchTripsRequest.newBuilder()
                    .setParent(arg)
                    .setActiveTripsOnly(true)
                    .setVehicleId(id)
                    .build();

            var trip = TripServiceGrpc.newBlockingStub(channel).withCallCredentials(new MyCallCredentials(token.get())).searchTrips(searchTripsRequest).getTripsList().get(0);
            try {
                ret = JsonFormat.printer().print(trip);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            };
        }

        log.info("queryTrips <-- " + ret);
        return ResponseEntity.ok(ret);
    }

    private Optional<String> getToken(String id) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("%s/token/%s", jwtServer, id);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            String body = response.getBody();
            JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
            return Optional.of(jsonObject.get("VehicleServiceToken").getAsString());
        }

        return Optional.empty();
    }

}
