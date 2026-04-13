package com.sothikdor.app.models;

public class Market {
    private String marketId;
    private String name;
    private String area;
    private double latitude;
    private double longitude;
    private boolean isActive;

    public Market() {}

    public Market(String marketId, String name, String area, double latitude, double longitude) {
        this.marketId = marketId;
        this.name = name;
        this.area = area;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isActive = true;
    }

    // Getters
    public String getMarketId() { return marketId; }
    public String getName() { return name; }
    public String getArea() { return area; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setMarketId(String marketId) { this.marketId = marketId; }
    public void setName(String name) { this.name = name; }
    public void setArea(String area) { this.area = area; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setActive(boolean active) { isActive = active; }
}
