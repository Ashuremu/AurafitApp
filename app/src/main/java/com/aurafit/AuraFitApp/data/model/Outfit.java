package com.aurafit.AuraFitApp.data.model;

import java.io.Serializable;
import java.util.List;

/**
 * Model class for a complete outfit recommendation
 */
public class Outfit implements Serializable {
    private String outfitId;
    private String name;
    private String style; // "Casual", "Formal", "Sporty", etc.
    private String weatherCondition; // "sunny", "rainy"
    private Long createdAt; // Timestamp when outfit was created
    private List<OutfitItem> items;
    private int confidence; // AI confidence score 0-100
    
    public Outfit() {
    }
    
    public Outfit(String outfitId, String name, String style, List<OutfitItem> items, int confidence) {
        this.outfitId = outfitId;
        this.name = name;
        this.style = style;
        this.items = items;
        this.confidence = confidence;
    }
    
    // Getters and Setters
    public String getOutfitId() {
        return outfitId;
    }
    
    public void setOutfitId(String outfitId) {
        this.outfitId = outfitId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
    
    public List<OutfitItem> getItems() {
        return items;
    }
    
    public void setItems(List<OutfitItem> items) {
        this.items = items;
    }
    
    public int getConfidence() {
        return confidence;
    }
    
    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }
    
    public String getWeatherCondition() {
        return weatherCondition;
    }
    
    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }
    
    public Long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
