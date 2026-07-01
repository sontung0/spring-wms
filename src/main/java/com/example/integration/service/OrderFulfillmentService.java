package com.example.integration.service;

import com.example.integration.domain.Order;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class OrderFulfillmentService {

    public void fulfillElectronics(Order order, @Header("PRIORITY") boolean isPriority) {
        String priorityTag = isPriority ? "[URGENT] " : "";
        System.out.println("📦 WAREHOUSE A: " + priorityTag + "Packing electronics. " + order.getId());
    }

    public void fulfillClothing(Order order, @Header("PRIORITY") boolean isPriority) {
        String priorityTag = isPriority ? "[URGENT] " : "";
        System.out.println("👕 WAREHOUSE B: " + priorityTag + "Bagging clothing. " + order.getId());
    }

    public void handleUnknown(Order order) {
        System.out.println("⚠️ ERROR: Cannot fulfill unknown category. " + order);
    }
}