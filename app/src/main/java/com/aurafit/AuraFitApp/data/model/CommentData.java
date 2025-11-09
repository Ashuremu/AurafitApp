package com.aurafit.AuraFitApp.data.model;

public class CommentData {
    private String commentId;
    private String postId;
    private String authorId;
    private String authorDisplayName;
    private String authorUsername;
    private String authorProfileImageUrl;
    private String content;
    private long timestamp;
    private int likesCount;
    private boolean isEdited;

    // Default constructor required for Firestore
    public CommentData() {}

    // Constructor with required fields
    public CommentData(String commentId, String postId, String authorId, 
                      String authorDisplayName, String authorUsername, String content) {
        this.commentId = commentId;
        this.postId = postId;
        this.authorId = authorId;
        this.authorDisplayName = authorDisplayName;
        this.authorUsername = authorUsername;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.likesCount = 0;
        this.isEdited = false;
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

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

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }
}
