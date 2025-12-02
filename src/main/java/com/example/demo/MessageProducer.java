package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("producer")
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Neue URL-Struktur erwartet jetzt auch "product"
    @GetMapping("/warehouse/send")
    public String sendData(
            @RequestParam int id,
            @RequestParam String loc,
            @RequestParam String product, // <--- NEU
            @RequestParam int amount) {

        try {
            // Objekt erstelln
            WarehouseData data = new WarehouseData(id, loc, product, amount);

            // Umwandeln in JSON
            String jsonMessage = objectMapper.writeValueAsString(data);

            // Senden
            kafkaTemplate.send("quickstart-events", jsonMessage);

            return "Gesendet: " + amount + "x " + product + " aus " + loc;

        } catch (Exception e) {
            return "Fehler: " + e.getMessage();
        }
    }
}