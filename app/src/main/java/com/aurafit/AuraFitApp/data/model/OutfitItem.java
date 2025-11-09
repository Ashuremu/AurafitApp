package com.aurafit.AuraFitApp.data.model;

import java.io.Serializable;

/**
 * Model class for individual items within an outfit
 */
public class OutfitItem implements Serializable {
    private String itemId;
    private String name;
    private String category; // "Top", "Bottom", "Shoes", "Accessories"
    private String subcategory; // "Shirt", "Pants", "Sneakers", "Watch"
    private String imageUrl;
    private String arModelUrl; // URL or path to the 3D AR model
    private int confidence; // Confidence score for this item in the outfit
    
    public OutfitItem() {
    }
    
    public OutfitItem(String itemId, String name, String category, String subcategory, 
                     String imageUrl, String arModelUrl, int confidence) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.imageUrl = imageUrl;
        this.arModelUrl = arModelUrl;
        this.confidence = confidence;
    }
    
    // Getters and Setters
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSubcategory() {
        return subcategory;
    }
    
    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getArModelUrl() {
        return arModelUrl;
    }
    
    public void setArModelUrl(String arModelUrl) {
        this.arModelUrl = arModelUrl;
    }
    
    public int getConfidence() {
        return confidence;
    }
    
    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }
}
