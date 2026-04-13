package com.sothikdor.app.models;

public class Product {
    private String productId;
    private String name;
    private String nameEn;
    private String category;
    private String unit;
    private String emoji;
    private boolean isActive;

    // Firebase requires empty constructor
    public Product() {}

    public Product(String productId, String name, String category, String unit, String emoji) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.emoji = emoji;
        this.isActive = true;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getNameEn() { return nameEn; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public String getEmoji() { return emoji; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setProductId(String productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public void setCategory(String category) { this.category = category; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public void setActive(boolean active) { isActive = active; }
}
