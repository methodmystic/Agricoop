package com.agricoop.model;

import java.sql.Timestamp;

public class Event {
    private int id;
    private String title;
    private String date;
    private String location;
    private String type;
    private String description;
    private Timestamp createdAt;

    public Event() {}

    public Event(int id, String title, String date, String location, String type, String description, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.location = location;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
