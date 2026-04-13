package com.sothikdor.app.models;

public class Price {
    private String priceId;
    private String productId;
    private String productName;
    private String productEmoji;
    private String category;
    private String unit;
    private String marketId;
    private String marketName;
    private double minPrice;
    private double maxPrice;
    private double avgPrice;
    private double previousAvgPrice; // trend হিসাবের জন্য
    private String date;
    private long timestamp;

    // Firebase requires empty constructor
    public Price() {}

    public Price(String productId, String marketId, double minPrice, double maxPrice, String date) {
        this.productId = productId;
        this.marketId = marketId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.avgPrice = (minPrice + maxPrice) / 2;
        this.date = date;
        this.timestamp = System.currentTimeMillis();
    }

    // দামের পরিবর্তন হিসাব করা (positive = বেড়েছে, negative = কমেছে)
    public double getPriceTrend() {
        if (previousAvgPrice == 0) return 0;
        return avgPrice - previousAvgPrice;
    }

    public boolean isPriceUp() { return getPriceTrend() > 0; }
    public boolean isPriceDown() { return getPriceTrend() < 0; }

    // Getters
    public String getPriceId() { return priceId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductEmoji() { return productEmoji; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public String getMarketId() { return marketId; }
    public String getMarketName() { return marketName; }
    public double getMinPrice() { return minPrice; }
    public double getMaxPrice() { return maxPrice; }
    public double getAvgPrice() { return avgPrice; }
    public double getPreviousAvgPrice() { return previousAvgPrice; }
    public String getDate() { return date; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setPriceId(String priceId) { this.priceId = priceId; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductEmoji(String productEmoji) { this.productEmoji = productEmoji; }
    public void setCategory(String category) { this.category = category; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setMarketId(String marketId) { this.marketId = marketId; }
    public void setMarketName(String marketName) { this.marketName = marketName; }
    public void setMinPrice(double minPrice) { this.minPrice = minPrice; }
    public void setMaxPrice(double maxPrice) { this.maxPrice = maxPrice; }
    public void setAvgPrice(double avgPrice) { this.avgPrice = avgPrice; }
    public void setPreviousAvgPrice(double previousAvgPrice) { this.previousAvgPrice = previousAvgPrice; }
    public void setDate(String date) { this.date = date; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
