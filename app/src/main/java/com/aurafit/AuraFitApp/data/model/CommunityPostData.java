package com.aurafit.AuraFitApp.data.model;

import java.util.List;

public class CommunityPostData {
    private String postId;
    private String authorId;
    private String authorDisplayName;
    private String authorUsername;
    private String authorProfileImageUrl;
    private String caption;
    private String imageUrl;
    private long timestamp;
    private int likesCount;
    private double rating;
    private int ratingCount;
    private double totalRating;
    private List<String> likedBy;
    private List<String> ratedBy;
    private List<CommentData> comments;
    private boolean isPublic;

    // Default constructor required for Firestore
    public CommunityPostData() {}

    // Constructor with required fields
    public CommunityPostData(String postId, String authorId, String authorDisplayName, 
                           String authorUsername, String caption, String imageUrl) {
        this.postId = postId;
        this.authorId = authorId;
        this.authorDisplayName = authorDisplayName;
        this.authorUsername = authorUsername;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
        this.likesCount = 0;
        this.rating = 0.0;
        this.ratingCount = 0;
        this.totalRating = 0.0;
        this.isPublic = true;
    }

    // Getters and Setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorDisplayName() { return authorDisplayName; }
    public void setAuthorDisplayName(String authorDisplayName) { this.authorDisplayName = authorDisplayName; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getAuthorProfileImageUrl() { return authorProfileImageUrl; }
    public void setAuthorProfileImageUrl(String authorProfileImageUrl) { this.authorProfileImageUrl = authorProfileImageUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public double getTotalRating() { return totalRating; }
    public void setTotalRating(double totalRating) { this.totalRating = totalRating; }

    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }

    public List<String> getRatedBy() { return ratedBy; }
    public void setRatedBy(List<String> ratedBy) { this.ratedBy = ratedBy; }

    public List<CommentData> getComments() { return comments; }
    public void setComments(List<CommentData> comments) { this.comments = comments; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
}
