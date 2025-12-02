package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // Simulating a Warehouse sending its data
    @GetMapping("/warehouse/send")
    public String sendData(
            @RequestParam int id,
            @RequestParam String loc,
            @RequestParam int amount) {

        try {
            // Create the object
            WarehouseData data = new WarehouseData(id, loc, amount, java.time.LocalDateTime.now().toString());

            // Convert to JSON String
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(data);

            // Send to Kafka
            kafkaTemplate.send("warehouse-events", jsonMessage);

            return "Sent data for Warehouse: " + loc;
        } catch (Exception e) {
            return "Error sending message: " + e.getMessage();
        }
    }
}