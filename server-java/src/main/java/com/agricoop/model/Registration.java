package com.agricoop.model;

import java.sql.Timestamp;

public class Registration {
    private int id;
    private String firstName;
    private String lastName;
    private String farmLocation;
    private String cropType;
    private String email;
    private String phone;
    private Double farmSize;
    private Integer userId;
    private Timestamp createdAt;

    public Registration() {}

    public Registration(int id, String firstName, String lastName, String farmLocation, String cropType,
                        String email, String phone, Double farmSize, Integer userId, Timestamp createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.farmLocation = farmLocation;
        this.cropType = cropType;
        this.email = email;
        this.phone = phone;
        this.farmSize = farmSize;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFarmLocation() { return farmLocation; }
    public void setFarmLocation(String farmLocation) { this.farmLocation = farmLocation; }

    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getFarmSize() { return farmSize; }
    public void setFarmSize(Double farmSize) { this.farmSize = farmSize; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
