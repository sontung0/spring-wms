package com.example.integration;

import com.example.integration.domain.Order;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface MyGateway {
    
    @Gateway(requestChannel = "routePojoChannel")
    void processOrder(Order order);
    
}