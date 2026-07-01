package com.example.integration;

import com.example.integration.domain.Order;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);
        
        MyGateway gateway = context.getBean(MyGateway.class);
        
        System.out.println("======================================");
        System.out.println("Testing Router with POJOs, Service Activators, Headers, and Filters:\n");
        
        // Should be routed to Electronics, Normal Priority
        gateway.processOrder(new Order("ORD-001", "ELECTRONICS", 5));
        
        // Should be routed to Clothing, HIGH Priority
        gateway.processOrder(new Order("ORD-002", "CLOTHING", 12));
        
        // Should hit default route (Unknown)
        gateway.processOrder(new Order("ORD-003", "FOOD", 100));
        
        // NEW: Should be caught by the Filter and discarded to the invalid orders channel
        gateway.processOrder(new Order("ORD-004", "ELECTRONICS", 0));
        gateway.processOrder(new Order("ORD-005", "CLOTHING", -5));
        
        System.out.println("======================================");
    }
}