package com.aurafit.AuraFitApp.ui.reservation.model;

import java.io.Serializable;
import java.util.Date;

public class ReservationItem implements Serializable {
    private String id;
    private String userId;
    private String itemId;
    private String itemName;
    private String itemImageUrl;
    private double itemPrice;
    private String selectedSize;
    private String itemGender;
    private String itemWeather;
    private String itemDescription;
    private Date reservedAt;
    private String status; // "reserved", "confirmed", "cancelled"
    private int quantity;

    // Default constructor required for Firestore
    public ReservationItem() {}

    public ReservationItem(String userId, String itemId, String itemName, String itemImageUrl, 
                         double itemPrice, String selectedSize, String itemGender, 
                         String itemWeather, String itemDescription) {
        // Validate required fields for UID-based authentication
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required for authentication");
        }
        if (itemId == null || itemId.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID is required");
        }
        if (selectedSize == null || selectedSize.trim().isEmpty()) {
            throw new IllegalArgumentException("Selected size is required");
        }
        
        this.userId = userId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemImageUrl = itemImageUrl;
        this.itemPrice = itemPrice;
        this.selectedSize = selectedSize;
        this.itemGender = itemGender;
        this.itemWeather = itemWeather;
        this.itemDescription = itemDescription;
        this.reservedAt = new Date();
        this.status = "reserved";
        this.quantity = 1;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemImageUrl() {
        return itemImageUrl;
    }

    public void setItemImageUrl(String itemImageUrl) {
        this.itemImageUrl = itemImageUrl;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    public String getItemGender() {
        return itemGender;
    }

    public void setItemGender(String itemGender) {
        this.itemGender = itemGender;
    }

    public String getItemWeather() {
        return itemWeather;
    }

    public void setItemWeather(String itemWeather) {
        this.itemWeather = itemWeather;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public Date getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(Date reservedAt) {
        this.reservedAt = reservedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
