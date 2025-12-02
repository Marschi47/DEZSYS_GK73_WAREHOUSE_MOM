package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

    @Autowired
    private MessageConsumer consumer; // Inject the consumer to access the list

    @GetMapping("/central/report")
    public java.util.List<WarehouseData> getReport() {
        return consumer.getStoredData();
    }
}
