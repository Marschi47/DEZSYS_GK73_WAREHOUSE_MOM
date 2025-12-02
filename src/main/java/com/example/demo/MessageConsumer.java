package com.example.demo;

import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;

@Component
public class MessageConsumer {

    // In-memory storage for the "Central" reporting
    private final java.util.List<WarehouseData> warehouseStorage = new java.util.ArrayList<>();

    @KafkaListener(topics = "warehouse-events", groupId = "myGroup")
    public void processMessage(String content) {
        try {
            // Convert JSON back to Object
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            WarehouseData data = mapper.readValue(content, WarehouseData.class);

            System.out.println("Central received: " + data);

            // Add to storage (In a real app, this would be a Database)
            warehouseStorage.add(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getter to access the data for the REST report
    public java.util.List<WarehouseData> getStoredData() {
        return warehouseStorage;
    }
}