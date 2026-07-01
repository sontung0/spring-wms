package com.example.integration.domain;

public class Order {
    private String id;
    private String category;
    private int quantity;

    public Order(String id, String category, int quantity) {
        this.id = id;
        this.category = category;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}