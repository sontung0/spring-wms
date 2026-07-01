package com.example.integration;

import com.example.integration.domain.Order;
import com.example.integration.service.OrderFulfillmentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
public class BasicFlowConfig {

    // --- LESSON 5, 6 & 7: Filters, Headers, Routing ---

    @Bean
    public IntegrationFlow orderRoutingFlow(OrderFulfillmentService fulfillmentService) {
        return IntegrationFlow.from("routePojoChannel")
            
            // Lesson 7: Filter out invalid orders (quantity <= 0)
            // If it fails, send it to the 'invalidOrdersChannel'
            .<Order>filter(order -> order.getQuantity() > 0, 
                e -> e.discardChannel("invalidOrdersChannel")
            )
            
            // Lesson 6: Add a header dynamically based on the payload (Order quantity)
            .enrichHeaders(h -> h.headerFunction("PRIORITY", 
                message -> {
                    Order order = (Order) message.getPayload();
                    return order.getQuantity() > 10; // High volume orders get priority
                }))
                
            // Route based on the Order's category
            .<Order, String>route(Order::getCategory, mapping -> mapping
                .channelMapping("ELECTRONICS", "electronicsChannel")
                .channelMapping("CLOTHING", "clothingChannel")
                .defaultOutputToParentFlow() 
            )
            .handle(fulfillmentService, "handleUnknown")
            .get();
    }
    
    // Sub-flow to handle discarded messages from the Filter
    @Bean
    public IntegrationFlow invalidOrdersFlow() {
        return IntegrationFlow.from("invalidOrdersChannel")
            .handle(message -> {
                System.out.println("❌ REJECTED BY FILTER: Invalid Order Data -> " + message.getPayload());
            })
            .get();
    }
    
    @Bean
    public IntegrationFlow electronicsFlow(OrderFulfillmentService fulfillmentService) {
        return IntegrationFlow.from("electronicsChannel")
            // Delegate to our Service Bean
            .handle(fulfillmentService, "fulfillElectronics")
            .get();
    }

    @Bean
    public IntegrationFlow clothingFlow(OrderFulfillmentService fulfillmentService) {
        return IntegrationFlow.from("clothingChannel")
            // Delegate to our Service Bean
            .handle(fulfillmentService, "fulfillClothing")
            .get();
    }
}