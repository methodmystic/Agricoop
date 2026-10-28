package com.agricoop.model;

import java.sql.Timestamp;

public class Order {
    private int id;
    private String userEmail;
    private String productName;
    private String category;
    private double price;
    private String farmerName;
    private String status;
    private Timestamp createdAt;

    public Order() {}

    public Order(int id, String userEmail, String productName, String category,
                 double price, String farmerName, String status, Timestamp createdAt) {
        this.id = id;
        this.userEmail = userEmail;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.farmerName = farmerName;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
