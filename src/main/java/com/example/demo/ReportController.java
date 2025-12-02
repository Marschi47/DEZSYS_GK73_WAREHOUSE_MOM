package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Profile("consumer")
public class ReportController {

    @Autowired
    private MessageConsumer consumer;

    // Wir ändern den Rückgabetyp von List auf Map
    @GetMapping("/central/report")
    public Map<String, List<WarehouseData>> getReport() {

        // 1. Wir holen die unsortierte Liste ("Alles auf einem Haufen")
        List<WarehouseData> alleDaten = consumer.getAllData();

        // 2. Wir gruppieren die Liste nach dem Ort ("Sortieren in Kisten")
        // Das Ergebnis ist eine Map: "Schlüssel" ist der Ort, "Wert" ist die Liste der Produkte dort.
        Map<String, List<WarehouseData>> nachStandortSortiert = alleDaten.stream()
                .collect(Collectors.groupingBy(WarehouseData::getLocation));

        return nachStandortSortiert;
    }
}