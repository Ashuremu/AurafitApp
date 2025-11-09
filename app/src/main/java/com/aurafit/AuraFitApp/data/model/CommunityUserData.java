package com.aurafit.AuraFitApp.data.model;

import java.util.List;

public class CommunityUserData {
    private String userId;
    private String displayName;
    private String username;
    private String profileImageUrl;
    private int postsCount;
    private int followersCount;
    private int followingCount;
    private List<String> followers;
    private List<String> following;
    private long createdAt;
    private long lastActiveAt;

    // Default constructor required for Firestore
    public CommunityUserData() {}

    // Constructor with required fields
    public CommunityUserData(String userId, String displayName, String username) {
        this.userId = userId;
        this.displayName = displayName;
        this.username = username;
        this.postsCount = 0;
        this.followersCount = 0;
        this.followingCount = 0;
        this.createdAt = System.currentTimeMillis();
        this.lastActiveAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getPostsCount() { return postsCount; }
    public void setPostsCount(int postsCount) { this.postsCount = postsCount; }

    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public List<String> getFollowers() { return followers; }
    public void setFollowers(List<String> followers) { this.followers = followers; }

    public List<String> getFollowing() { return following; }
    public void setFollowing(List<String> following) { this.following = following; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(long lastActiveAt) { this.lastActiveAt = lastActiveAt; }
}
