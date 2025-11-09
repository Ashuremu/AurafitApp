package com.aurafit.AuraFitApp.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aurafit.AuraFitApp.data.model.CommentData;
import com.aurafit.AuraFitApp.data.model.CommunityPostData;
import com.aurafit.AuraFitApp.data.model.CommunityUserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityDataManager {
    private static final String TAG = "CommunityDataManager";
    private static CommunityDataManager instance;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    
    // Collection names
    private static final String COMMUNITY_USERS_COLLECTION = "community_users";
    private static final String COMMUNITY_POSTS_COLLECTION = "community_posts";
    private static final String COMMUNITY_COMMENTS_COLLECTION = "community_comments";
    private static final String COMMUNITY_STORAGE_PATH = "community";

    private CommunityDataManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized CommunityDataManager getInstance() {
        if (instance == null) {
            instance = new CommunityDataManager();
        }
        return instance;
    }

    // Callback interfaces
    public interface UserDataCallback {
        void onSuccess(CommunityUserData userData);
        void onError(String error);
    }

    public interface PostsCallback {
        void onSuccess(List<CommunityPostData> posts);
        void onError(String error);
    }
    
    public interface PaginatedPostsCallback {
        void onSuccess(List<CommunityPostData> posts, DocumentSnapshot lastDocument);
        void onError(String error);
    }

    public interface PostUpdateCallback {
        void onSuccess(CommunityPostData updatedPost);
        void onError(String error);
    }

    public interface PostCallback {
        void onSuccess(CommunityPostData post);
        void onError(String error);
    }

    public interface CommentsCallback {
        void onSuccess(List<CommentData> comments);
        void onError(String error);
    }

    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public interface UpdateCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface RatingCallback {
        void onSuccess(double rating);
        void onError(String error);
    }

    public interface RatingStatsCallback {
        void onSuccess(Map<String, Object> stats);
        void onError(String error);
    }

    // Initialize or update user data in community
    public void initializeUserData(String displayName, String username, UserDataCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection(COMMUNITY_USERS_COLLECTION).document(userId);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // User exists, update last active
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("lastActiveAt", System.currentTimeMillis());
                        if (displayName != null) updates.put("displayName", displayName);
                        if (username != null) updates.put("username", username);
                        
                        userRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                CommunityUserData userData = document.toObject(CommunityUserData.class);
                                callback.onSuccess(userData);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                callback.onError("Failed to update user data: " + e.getMessage());
                            }
                        });
                    } else {
                        // Create new user
                        CommunityUserData newUser = new CommunityUserData(userId, displayName, username);
                        userRef.set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                callback.onSuccess(newUser);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                callback.onError("Failed to create user data: " + e.getMessage());
                            }
                        });
                    }
                } else {
                    callback.onError("Failed to check user data: " + task.getException().getMessage());
                }
            }
        });
    }

    // Get user data
    public void getUserData(String userId, UserDataCallback callback) {
        db.collection(COMMUNITY_USERS_COLLECTION).document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                CommunityUserData userData = document.toObject(CommunityUserData.class);
                                callback.onSuccess(userData);
                            } else {
                                callback.onError("User not found");
                            }
                        } else {
                            callback.onError("Failed to get user data: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Upload image to Firebase Storage
    public void uploadPostImage(byte[] imageData, String userId, ImageUploadCallback callback) {
        String fileName = "post_" + System.currentTimeMillis() + ".jpg";
        String path = COMMUNITY_STORAGE_PATH + "/" + userId + "/" + fileName;
        StorageReference imageRef = storage.getReference().child(path);

        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        callback.onSuccess(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError("Failed to get download URL: " + e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError("Failed to upload image: " + e.getMessage());
            }
        });
    }

    // Create a new post
    public void createPost(String caption, String imageUrl, UpdateCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String postId = db.collection("community_posts").document().getId();

        // Get user data from users collection first (source of truth)
        db.collection("users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot userDoc = task.getResult();
                            String displayName = userDoc.getString("displayName");
                            String username = userDoc.getString("username");
                            String profileImageUrl = userDoc.getString("profileImageUrl");
                            
                            // Create post with data from users collection
                            CommunityPostData post = new CommunityPostData(
                                    postId,
                                    userId,
                                    displayName != null ? displayName : "User",
                                    username != null ? username : "user",
                                    caption,
                                    imageUrl
                            );
                            post.setAuthorProfileImageUrl(profileImageUrl);

                            // Create post directly in community_posts collection
                            db.collection("community_posts").document(postId)
                                    .set(post)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Update user's post count
                                            updateUserPostCount(userId, 1);
                                            callback.onSuccess("Post created successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            callback.onError("Failed to create post: " + e.getMessage());
                                        }
                                    });
                        } else {
                            callback.onError("Failed to get user data: " + (task.getException() != null ? task.getException().getMessage() : "User not found"));
                        }
                    }
                });
    }

    // Get all posts from all users
    public void getAllPosts(PostsCallback callback) {
        getAllPosts(null, new PaginatedPostsCallback() {
            @Override
            public void onSuccess(List<CommunityPostData> posts, DocumentSnapshot lastDocument) {
                callback.onSuccess(posts);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    // Get all posts from all users with pagination
    public void getAllPosts(DocumentSnapshot lastDocument, PaginatedPostsCallback callback) {
        Query query = db.collection("community_posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20); // Load 20 posts at a time
        
        if (lastDocument != null) {
            query = query.startAfter(lastDocument);
        }
        
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<CommunityPostData> allPosts = new ArrayList<>();
                            
                            for (QueryDocumentSnapshot postDoc : task.getResult()) {
                                CommunityPostData post = postDoc.toObject(CommunityPostData.class);
                                allPosts.add(post);
                            }
                            
                            // Get the last document for pagination
                            DocumentSnapshot lastDoc = null;
                            if (!task.getResult().isEmpty()) {
                                lastDoc = task.getResult().getDocuments().get(task.getResult().size() - 1);
                            }
                            
                            callback.onSuccess(allPosts, lastDoc);
                        } else {
                            callback.onError("Failed to get posts: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Get user's own posts with pagination
    public void getUserPosts(String userId, PostsCallback callback) {
        getUserPosts(userId, null, new PaginatedPostsCallback() {
            @Override
            public void onSuccess(List<CommunityPostData> posts, DocumentSnapshot lastDocument) {
                callback.onSuccess(posts);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    // Get user's liked posts
    public void getLikedPosts(String userId, PostsCallback callback) {
        getLikedPosts(userId, null, new PaginatedPostsCallback() {
            @Override
            public void onSuccess(List<CommunityPostData> posts, DocumentSnapshot lastDocument) {
                callback.onSuccess(posts);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    // Get user's liked posts with pagination support
    public void getLikedPosts(String userId, DocumentSnapshot lastDocument, PaginatedPostsCallback callback) {
        Query query = db.collection("community_posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20); // Load 20 posts at a time for better filtering
        
        if (lastDocument != null) {
            query = query.startAfter(lastDocument);
        }
        
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<CommunityPostData> allPosts = new ArrayList<>();
                            List<CommunityPostData> likedPosts = new ArrayList<>();
                            
                            // Get all posts first
                            for (QueryDocumentSnapshot postDoc : task.getResult()) {
                                CommunityPostData post = postDoc.toObject(CommunityPostData.class);
                                allPosts.add(post);
                            }
                            
                            // Filter posts where user has liked
                            for (CommunityPostData post : allPosts) {
                                if (post.getLikedBy() != null && post.getLikedBy().contains(userId)) {
                                    likedPosts.add(post);
                                }
                            }
                            
                            // Get the last document for pagination
                            DocumentSnapshot lastDoc = null;
                            if (!task.getResult().isEmpty()) {
                                lastDoc = task.getResult().getDocuments().get(task.getResult().size() - 1);
                            }
                            
                            callback.onSuccess(likedPosts, lastDoc);
                        } else {
                            callback.onError("Failed to get liked posts: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Refresh user data for a specific post
    public void refreshPostAuthorData(String postId, PostUpdateCallback callback) {
        DocumentReference postRef = db.collection(COMMUNITY_POSTS_COLLECTION).document(postId);
        
        postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    CommunityPostData post = task.getResult().toObject(CommunityPostData.class);
                    if (post != null) {
                        // Get fresh user data from users collection
                        db.collection("users").document(post.getAuthorId()).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> userTask) {
                                        if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                            DocumentSnapshot userDoc = userTask.getResult();
                                            String displayName = userDoc.getString("displayName");
                                            String username = userDoc.getString("username");
                                            String profileImageUrl = userDoc.getString("profileImageUrl");
                                            
                                            // Update post with fresh user data
                                            post.setAuthorDisplayName(displayName != null ? displayName : "User");
                                            post.setAuthorUsername(username != null ? username : "user");
                                            post.setAuthorProfileImageUrl(profileImageUrl);
                                            
                                            // Update the post in Firestore
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("authorDisplayName", post.getAuthorDisplayName());
                                            updates.put("authorUsername", post.getAuthorUsername());
                                            updates.put("authorProfileImageUrl", post.getAuthorProfileImageUrl());
                                            
                                            postRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    callback.onSuccess(post);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    callback.onError("Failed to update post: " + e.getMessage());
                                                }
                                            });
                                        } else {
                                            callback.onError("User not found");
                                        }
                                    }
                                });
                    } else {
                        callback.onError("Post not found");
                    }
                } else {
                    callback.onError("Failed to get post: " + task.getException().getMessage());
                }
            }
        });
    }
    
    // Get user's own posts with pagination support
    public void getUserPosts(String userId, DocumentSnapshot lastDocument, PaginatedPostsCallback callback) {
        Query query = db.collection("community_posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20); // Load 20 posts at a time for better filtering
        
        if (lastDocument != null) {
            query = query.startAfter(lastDocument);
        }
        
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<CommunityPostData> allPosts = new ArrayList<>();
                            List<CommunityPostData> userPosts = new ArrayList<>();
                            
                            // Get all posts first
                            for (QueryDocumentSnapshot postDoc : task.getResult()) {
                                CommunityPostData post = postDoc.toObject(CommunityPostData.class);
                                allPosts.add(post);
                            }
                            
                            // Filter posts where authorId matches the current user
                            for (CommunityPostData post : allPosts) {
                                if (post.getAuthorId() != null && post.getAuthorId().equals(userId)) {
                                    userPosts.add(post);
                                }
                            }
                            
                            // Get the last document for pagination
                            DocumentSnapshot lastDoc = null;
                            if (!task.getResult().isEmpty()) {
                                lastDoc = task.getResult().getDocuments().get(task.getResult().size() - 1);
                            }
                            
                            callback.onSuccess(userPosts, lastDoc);
                        } else {
                            callback.onError("Failed to get user posts: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Like/Unlike a post
    public void togglePostLike(String postId, UpdateCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        
        // Get the post directly from community_posts collection
        DocumentReference postRef = db.collection("community_posts").document(postId);
        
        postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    CommunityPostData post = document.toObject(CommunityPostData.class);
                    List<String> likedBy = post.getLikedBy();
                    if (likedBy == null) {
                        likedBy = new ArrayList<>();
                    }

                    boolean isLiked = likedBy.contains(currentUserId);
                    if (isLiked) {
                        likedBy.remove(currentUserId);
                    } else {
                        likedBy.add(currentUserId);
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("likedBy", likedBy);
                    updates.put("likesCount", likedBy.size());

                    postRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            callback.onSuccess(isLiked ? "Post unliked" : "Post liked");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onError("Failed to update like: " + e.getMessage());
                        }
                    });
                } else {
                    callback.onError("Post not found");
                }
            }
                });
    }

    // Rate a post (unique rating per user, like likes system)
    public void ratePost(String postId, double rating, UpdateCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        if (rating < 1.0 || rating > 5.0) {
            callback.onError("Rating must be between 1.0 and 5.0");
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        
        // Get the post directly from community_posts collection
        DocumentReference postRef = db.collection("community_posts").document(postId);
        
        postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    CommunityPostData post = document.toObject(CommunityPostData.class);
                    List<String> ratedBy = post.getRatedBy();
                    if (ratedBy == null) {
                        ratedBy = new ArrayList<>();
                    }

                    boolean hasRated = ratedBy.contains(currentUserId);
                    double currentTotalRating = post.getTotalRating();
                    int currentRatingCount = post.getRatingCount();
                    
                    double newTotalRating;
                    int newRatingCount;
                    double newAverageRating;

                    if (hasRated) {
                        // User has already rated, remove their previous rating and add new one
                        // For simplicity, we'll recalculate from scratch
                        // In a real implementation, you'd track individual user ratings
                        ratedBy.remove(currentUserId);
                        newRatingCount = currentRatingCount - 1;
                        newTotalRating = currentTotalRating - post.getRating(); // Subtract old average
                        newTotalRating = newTotalRating + rating; // Add new rating
                        newRatingCount = newRatingCount + 1;
                        ratedBy.add(currentUserId);
                    } else {
                        // New rating
                        ratedBy.add(currentUserId);
                        newTotalRating = currentTotalRating + rating;
                        newRatingCount = currentRatingCount + 1;
                    }

                    // Calculate new average rating
                    newAverageRating = newTotalRating / newRatingCount;

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("ratedBy", ratedBy);
                    updates.put("ratingCount", newRatingCount);
                    updates.put("totalRating", newTotalRating);
                    updates.put("rating", newAverageRating);

                    postRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            callback.onSuccess("Post rated successfully");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onError("Failed to update rating: " + e.getMessage());
                        }
                    });
                } else {
                    callback.onError("Post not found");
                }
            }
        });
    }

    // Get user's rating for a specific post
    public void getUserRatingForPost(String postId, String userId, RatingCallback callback) {
        DocumentReference postRef = db.collection("community_posts").document(postId);
        
        postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    CommunityPostData post = document.toObject(CommunityPostData.class);
                    
                    if (post.getRatedBy() != null && post.getRatedBy().contains(userId)) {
                        // User has rated this post, but we need to get the actual rating
                        // For now, we'll return the average rating as a placeholder
                        // In a real implementation, you'd store individual user ratings
                        callback.onSuccess(post.getRating());
                    } else {
                        callback.onSuccess(0.0); // User hasn't rated
                    }
                } else {
                    callback.onError("Post not found");
                }
            }
        });
    }

    // Get posts sorted by rating (using simple query to avoid index requirements)
    public void getPostsByRating(int limit, PostsCallback callback) {
        db.collection("community_posts")
                .whereEqualTo("isPublic", true)
                .limit(limit)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<CommunityPostData> posts = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                CommunityPostData post = document.toObject(CommunityPostData.class);
                                if (post != null) {
                                    posts.add(post);
                                }
                            }
                            // Sort by rating in memory
                            posts.sort((post1, post2) -> Double.compare(post2.getRating(), post1.getRating()));
                            callback.onSuccess(posts);
                        } else {
                            callback.onError("Failed to get posts: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Get user's rated posts (using simple query to avoid index requirements)
    public void getUserRatedPosts(String userId, PostsCallback callback) {
        db.collection("community_posts")
                .whereEqualTo("isPublic", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<CommunityPostData> posts = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                CommunityPostData post = document.toObject(CommunityPostData.class);
                                if (post != null && post.getRatedBy() != null && post.getRatedBy().contains(userId)) {
                                    posts.add(post);
                                }
                            }
                            // Sort by timestamp in memory
                            posts.sort((post1, post2) -> Long.compare(post2.getTimestamp(), post1.getTimestamp()));
                            callback.onSuccess(posts);
                        } else {
                            callback.onError("Failed to get rated posts: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Get a specific post by ID
    public void getPostById(String postId, PostCallback callback) {
        db.collection("community_posts")
                .document(postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            CommunityPostData post = task.getResult().toObject(CommunityPostData.class);
                            if (post != null) {
                                callback.onSuccess(post);
                            } else {
                                callback.onError("Failed to parse post data");
                            }
                        } else {
                            callback.onError("Post not found: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    // Get rating statistics for a user
    public void getUserRatingStats(String userId, RatingStatsCallback callback) {
        db.collection("community_posts")
                .whereEqualTo("authorId", userId)
                .whereEqualTo("isPublic", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double totalRating = 0.0;
                            int totalPosts = 0;
                            int totalRatings = 0;
                            double averageRating = 0.0;
                            
                            for (DocumentSnapshot document : task.getResult()) {
                                CommunityPostData post = document.toObject(CommunityPostData.class);
                                if (post != null) {
                                    totalPosts++;
                                    totalRatings += post.getRatingCount();
                                    totalRating += post.getTotalRating();
                                }
                            }
                            
                            if (totalRatings > 0) {
                                averageRating = totalRating / totalRatings;
                            }
                            
                            Map<String, Object> stats = new HashMap<>();
                            stats.put("totalPosts", totalPosts);
                            stats.put("totalRatings", totalRatings);
                            stats.put("averageRating", averageRating);
                            
                            callback.onSuccess(stats);
                        } else {
                            callback.onError("Failed to get rating stats: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Add comment to post
    public void addComment(String postId, String content, UpdateCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String commentId = db.collection(COMMUNITY_COMMENTS_COLLECTION).document().getId();

        // Get user data first
        getUserData(userId, new UserDataCallback() {
            @Override
            public void onSuccess(CommunityUserData userData) {
                CommentData comment = new CommentData(
                        commentId,
                        postId,
                        userId,
                        userData.getDisplayName(),
                        userData.getUsername(),
                        content
                );
                comment.setAuthorProfileImageUrl(userData.getProfileImageUrl());

                db.collection(COMMUNITY_COMMENTS_COLLECTION).document(commentId)
                        .set(comment)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                callback.onSuccess("Comment added");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                callback.onError("Failed to add comment: " + e.getMessage());
                            }
                        });
            }

            @Override
            public void onError(String error) {
                callback.onError("Failed to get user data: " + error);
            }
        });
    }

    // Get comments for a post
    public void getPostComments(String postId, CommentsCallback callback) {
        db.collection(COMMUNITY_COMMENTS_COLLECTION)
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<CommentData> comments = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                CommentData comment = document.toObject(CommentData.class);
                                comments.add(comment);
                            }
                            callback.onSuccess(comments);
                        } else {
                            callback.onError("Failed to get comments: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Update user's post count in both collections
    private void updateUserPostCount(String userId, int increment) {
        // Update community users collection
        DocumentReference communityUserRef = db.collection(COMMUNITY_USERS_COLLECTION).document(userId);
        communityUserRef.update("postsCount", com.google.firebase.firestore.FieldValue.increment(increment));
        
        // Update main users collection as well
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("postsCount", com.google.firebase.firestore.FieldValue.increment(increment));
    }

    /**
     * Delete all community posts by a specific user
     * This is called when a user account is deleted
     */
    public void deleteUserCommunityPosts(String userId, UpdateCallback callback) {
        // Get all posts by this user
        db.collection(COMMUNITY_POSTS_COLLECTION)
                .whereEqualTo("authorId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // No posts to delete
                                callback.onSuccess("No community posts found for user");
                                return;
                            }
                            
                            // Delete all posts in batch
                            WriteBatch batch = db.batch();
                            final int postCount = task.getResult().size();
                            
                            for (QueryDocumentSnapshot postDoc : task.getResult()) {
                                String postId = postDoc.getId();
                                
                                // Delete the post document
                                batch.delete(db.collection(COMMUNITY_POSTS_COLLECTION).document(postId));
                                
                                // Delete all comments for this post
                                batch.delete(db.collection(COMMUNITY_COMMENTS_COLLECTION).document(postId));
                            }
                            
                            // Commit the batch deletion
                            batch.commit()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Deleted " + postCount + " community posts for user: " + userId);
                                            callback.onSuccess("Deleted " + postCount + " community posts");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Failed to delete community posts", e);
                                            callback.onError("Failed to delete community posts: " + e.getMessage());
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Failed to get user posts for deletion", task.getException());
                            callback.onError("Failed to get user posts: " + task.getException().getMessage());
                        }
                    }
                });
    }
    
    /**
     * Delete user's community data (posts, comments, follows)
     * This is called when a user account is deleted
     */
    public void deleteUserCommunityData(String userId, UpdateCallback callback) {
        // Delete community posts
        deleteUserCommunityPosts(userId, new UpdateCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "Community posts deleted: " + message);
                
                // Delete user from community_users collection
                db.collection(COMMUNITY_USERS_COLLECTION).document(userId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "User removed from community_users collection");
                                callback.onSuccess("All community data deleted successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to delete user from community_users", e);
                                callback.onError("Failed to delete community user data: " + e.getMessage());
                            }
                        });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to delete community posts: " + error);
                callback.onError("Failed to delete community posts: " + error);
            }
        });
    }

    // Follow/Unfollow user
    public void toggleFollow(String targetUserId, UpdateCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        if (currentUserId.equals(targetUserId)) {
            callback.onError("Cannot follow yourself");
            return;
        }

        // Update both users' follow data
        DocumentReference currentUserRef = db.collection(COMMUNITY_USERS_COLLECTION).document(currentUserId);
        DocumentReference targetUserRef = db.collection(COMMUNITY_USERS_COLLECTION).document(targetUserId);

        // Check if already following
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        CommunityUserData userData = document.toObject(CommunityUserData.class);
                        List<String> following = userData.getFollowing();
                        if (following == null) {
                            following = new ArrayList<>();
                        }

                        boolean isFollowing = following.contains(targetUserId);
                        if (isFollowing) {
                            // Unfollow
                            following.remove(targetUserId);
                            currentUserRef.update("following", following, "followingCount", following.size());
                            targetUserRef.update("followersCount", com.google.firebase.firestore.FieldValue.increment(-1));
                            callback.onSuccess("Unfollowed user");
                        } else {
                            // Follow
                            following.add(targetUserId);
                            currentUserRef.update("following", following, "followingCount", following.size());
                            targetUserRef.update("followersCount", com.google.firebase.firestore.FieldValue.increment(1));
                            callback.onSuccess("Followed user");
                        }
                    } else {
                        callback.onError("User data not found");
                    }
                } else {
                    callback.onError("Failed to check follow status: " + task.getException().getMessage());
                }
            }
        });
    }
}
