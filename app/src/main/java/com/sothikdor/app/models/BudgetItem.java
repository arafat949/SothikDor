package com.sothikdor.app.models;

public class BudgetItem {
    private String productId;
    private String productName;
    private String emoji;
    private String unit;
    private double quantity;
    private double pricePerUnit;

    public BudgetItem() {}

    public BudgetItem(String productId, String productName, String emoji, String unit, double pricePerUnit) {
        this.productId = productId;
        this.productName = productName;
        this.emoji = emoji;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.quantity = 1;
    }

    public double getTotalCost() {
        return quantity * pricePerUnit;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getEmoji() { return emoji; }
    public String getUnit() { return unit; }
    public double getQuantity() { return quantity; }
    public double getPricePerUnit() { return pricePerUnit; }

    // Setters
    public void setProductId(String productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
}
