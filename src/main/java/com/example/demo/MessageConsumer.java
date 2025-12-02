package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List; // <--- WICHTIG: Import nicht vergessen

@Component
@Profile("consumer")
public class MessageConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Die Liste ist "private", von außen kommt niemand ran
    private final List<WarehouseData> warehouseDatabase = new ArrayList<>();

    @KafkaListener(topics = "quickstart-events", groupId = "central-group")
    public void processMessage(String content) {
        try {
            WarehouseData data = objectMapper.readValue(content, WarehouseData.class);
            System.out.println("Zentrale hat empfangen: " + data);
            warehouseDatabase.add(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- HIER IST DAS FEHLENDE STÜCK ---
    // Diese Methode hat gefehlt. Sie erlaubt dem Controller,
    // die private Liste zu lesen.
    public List<WarehouseData> getAllData() {
        return warehouseDatabase;
    }
    // -----------------------------------
}