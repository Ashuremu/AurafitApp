package com.aurafit.AuraFitApp.data.model;

import java.util.Date;

/**
 * User model for Firestore database storage
 */
public class User {
    private String userId;
    private String username;
    private String email;
    private String displayName;
    private Integer age;
    private Date createdAt;
    private Date lastLoginAt;
    private boolean onboardingCompleted;
    private String preferredStyle;
    private String bodyMeasurements;
    // Flat measurement fields in centimeters
    private Float height;     // cm
    private Float chest;      // cm
    private Float waist;      // cm
    private Float hips;       // cm
    private Float shoeSize;   // numeric shoe size
    private Boolean body_measurements_captured;
    private String profileImageUrl;
    
    // Style preference fields
    private String clothingPreference; // "Male", "Female", "Mixed"
    private String stylePreference; // "Casual", "Elegant", "Chic"
    
    // Community stats
    private Integer postsCount; // Number of posts created by user

    // Default constructor required for Firestore
    public User() {}

    public User(String userId, String username, String email, String displayName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = new Date();
        this.lastLoginAt = new Date();
        this.onboardingCompleted = false;
        this.preferredStyle = null;
        this.bodyMeasurements = null;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Date lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public String getPreferredStyle() {
        return preferredStyle;
    }

    public void setPreferredStyle(String preferredStyle) {
        this.preferredStyle = preferredStyle;
    }

    public String getBodyMeasurements() {
        return bodyMeasurements;
    }

    public void setBodyMeasurements(String bodyMeasurements) {
        this.bodyMeasurements = bodyMeasurements;
    }

    public Float getHeight() { return height; }
    public void setHeight(Float height) { this.height = height; }
    public Float getChest() { return chest; }
    public void setChest(Float chest) { this.chest = chest; }
    public Float getWaist() { return waist; }
    public void setWaist(Float waist) { this.waist = waist; }
    public Float getHips() { return hips; }
    public void setHips(Float hips) { this.hips = hips; }
    public Float getShoeSize() { return shoeSize; }
    public void setShoeSize(Float shoeSize) { this.shoeSize = shoeSize; }
    public Boolean getBody_measurements_captured() { return body_measurements_captured; }
    public void setBody_measurements_captured(Boolean captured) { this.body_measurements_captured = captured; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    // Style preference getters and setters
    public String getClothingPreference() { return clothingPreference; }
    public void setClothingPreference(String clothingPreference) { this.clothingPreference = clothingPreference; }
    public String getStylePreference() { return stylePreference; }
    public void setStylePreference(String stylePreference) { this.stylePreference = stylePreference; }
    
    // Community stats getters and setters
    public Integer getPostsCount() { return postsCount; }
    public void setPostsCount(Integer postsCount) { this.postsCount = postsCount; }
}
