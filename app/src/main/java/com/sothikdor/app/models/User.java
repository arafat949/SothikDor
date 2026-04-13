package com.sothikdor.app.models;

public class User {
    private String userId;
    private String name;
    private String phone;
    private String preferredMarketId;
    private String preferredMarketName;
    private double latitude;
    private double longitude;

    public User() {}

    public User(String userId, String name, String phone) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getPreferredMarketId() { return preferredMarketId; }
    public String getPreferredMarketName() { return preferredMarketName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPreferredMarketId(String preferredMarketId) { this.preferredMarketId = preferredMarketId; }
    public void setPreferredMarketName(String preferredMarketName) { this.preferredMarketName = preferredMarketName; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
