package com.aurafit.AuraFitApp.ui.community;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.CommunityDataManager;
import com.aurafit.AuraFitApp.data.model.CommunityPostData;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    
    private List<CommunityPostData> postsList;
    private Context context;
    private CommunityDataManager communityDataManager;
    private FirebaseAuth auth;
    private OnPostLikedListener onPostLikedListener;
    
    public PostAdapter(List<CommunityPostData> postsList, Context context) {
        this.postsList = postsList;
        this.context = context;
        this.communityDataManager = CommunityDataManager.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    
    public PostAdapter(List<CommunityPostData> postsList, Context context, OnPostLikedListener onPostLikedListener) {
        this.postsList = postsList;
        this.context = context;
        this.communityDataManager = CommunityDataManager.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.onPostLikedListener = onPostLikedListener;
    }
    
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPostData post = postsList.get(position);
        
        // Display author username
        if (post.getAuthorUsername() != null) {
            holder.authorUsername.setText("@" + post.getAuthorUsername());
        } else {
            holder.authorUsername.setText("@user");
        }
        
        // Display author profile picture
        if (post.getAuthorProfileImageUrl() != null && !post.getAuthorProfileImageUrl().isEmpty()) {
            Glide.with(context)
                .load(post.getAuthorProfileImageUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(holder.postAuthorProfilePicture);
        } else {
            holder.postAuthorProfilePicture.setImageResource(R.drawable.ic_person);
        }
        
        // Refresh user data dynamically if needed (optional - for real-time updates)
        refreshUserDataIfNeeded(post, holder);
        
        if (post.getTimestamp() > 0) {
            String timeAgo = DateUtils.getRelativeTimeSpanString(
                post.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString();
            holder.timestamp.setText("Posted " + timeAgo);
        }
        
        if (post.getCaption() != null) {
            holder.caption.setText(post.getCaption());
        }
        
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.postImage);
        } else {
            holder.postImage.setImageResource(R.drawable.placeholder_image);
        }
        
        holder.likesCount.setText(post.getLikesCount() + " Likes");
        
        // Set initial like button state based on whether current user has liked the post
        if (auth.getCurrentUser() != null && post.getLikedBy() != null && 
            post.getLikedBy().contains(auth.getCurrentUser().getUid())) {
            holder.likeButton.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.likeButton.setImageResource(R.drawable.ic_heart);
        }
        
        // Set rating display with proper star indicators
        updateStarDisplay(holder, post.getRating());
        
        // Update average rating text
        if (post.getRating() > 0) {
            holder.averageRatingText.setText(String.format("%.1f", post.getRating()));
        } else {
            holder.averageRatingText.setText("0.0");
        }
        
        // Update rating count
        holder.ratingCount.setText("(" + post.getRatingCount() + ")");
        
        // Show if current user has rated this post
        updateUserRatingStatus(holder, post);
        
        holder.likeButton.setOnClickListener(v -> {
            // Disable button temporarily to prevent multiple clicks
            holder.likeButton.setEnabled(false);
            
            // Check current like state before toggling
            boolean isCurrentlyLiked = auth.getCurrentUser() != null && 
                post.getLikedBy() != null && 
                post.getLikedBy().contains(auth.getCurrentUser().getUid());
            
            communityDataManager.togglePostLike(post.getPostId(), new CommunityDataManager.UpdateCallback() {
                @Override
                public void onSuccess(String message) {
                    // Update the local post object and UI based on the toggle
                    if (isCurrentlyLiked) {
                        // User was liked, now unliked
                        post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
                        
                        // Remove current user from likedBy list
                        if (post.getLikedBy() != null && auth.getCurrentUser() != null) {
                            post.getLikedBy().remove(auth.getCurrentUser().getUid());
                        }
                        
                        // Update UI - show empty heart
                        holder.likeButton.setImageResource(R.drawable.ic_heart);
                        
                    } else {
                        // User was not liked, now liked
                        post.setLikesCount(post.getLikesCount() + 1);
                        
                        // Add current user to likedBy list
                        if (post.getLikedBy() == null) {
                            post.setLikedBy(new java.util.ArrayList<>());
                        }
                        if (auth.getCurrentUser() != null && !post.getLikedBy().contains(auth.getCurrentUser().getUid())) {
                            post.getLikedBy().add(auth.getCurrentUser().getUid());
                        }
                        
                        // Update UI - show filled heart
                        holder.likeButton.setImageResource(R.drawable.ic_heart_filled);
                    }
                    
                    // Update the likes count display
                    holder.likesCount.setText(post.getLikesCount() + " Likes");
                    
                    // Update the post in the list to maintain data consistency
                    updatePostInList(post);
                    
                    // Notify listener about the like action
                    if (onPostLikedListener != null) {
                        onPostLikedListener.onPostLiked(post.getPostId(), !isCurrentlyLiked);
                    }
                    
                    // Re-enable button
                    holder.likeButton.setEnabled(true);
                }

                @Override
                public void onError(String error) {
                    // Re-enable button on error
                    holder.likeButton.setEnabled(true);
                    Toast.makeText(context, "Failed to like post: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        
        holder.downloadButton.setOnClickListener(v -> {
            downloadPostImage(post);
        });
        
        // Add rating functionality - enhanced 5-star rating
        holder.averageRatingText.setOnClickListener(v -> {
            // Show a simple rating dialog
            showRatingDialog(post.getPostId(), holder);
        });
        
        // Add click listeners to individual stars for better UX
        setupStarClickListeners(holder, post.getPostId());
    }
    
    private void showRatingDialog(String postId, PostViewHolder holder) {
        // Find the post to check if user has already rated
        CommunityPostData post = null;
        for (CommunityPostData p : postsList) {
            if (p.getPostId().equals(postId)) {
                post = p;
                break;
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        // Check if user has already rated
        boolean hasRated = post != null && auth.getCurrentUser() != null && 
                          post.getRatedBy() != null && 
                          post.getRatedBy().contains(auth.getCurrentUser().getUid());
        
        if (hasRated) {
            builder.setTitle("Update Rating");
            builder.setMessage("You have already rated this post. Choose a new rating or remove your rating.");
        } else {
            builder.setTitle("Rate this post");
            builder.setMessage("How would you rate this post?");
        }
        
        // Create rating buttons with star indicators
        String[] ratings = {"⭐ 1 Star", "⭐⭐ 2 Stars", "⭐⭐⭐ 3 Stars", "⭐⭐⭐⭐ 4 Stars", "⭐⭐⭐⭐⭐ 5 Stars"};
        double[] ratingValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        
        builder.setItems(ratings, (dialog, which) -> {
            double rating = ratingValues[which];
            communityDataManager.ratePost(postId, rating, new CommunityDataManager.UpdateCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(context, "Post rated successfully!", Toast.LENGTH_SHORT).show();
                    // Update the rating display
                    updateRatingDisplay(holder, postId);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(context, "Failed to rate post: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        if (hasRated) {
            // Add option to remove rating
            builder.setNeutralButton("Remove Rating", (dialog, which) -> {
                removeRating(postId, holder);
            });
        }
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void removeRating(String postId, PostViewHolder holder) {
        // Remove user from ratedBy list and recalculate average
        for (CommunityPostData post : postsList) {
            if (post.getPostId().equals(postId)) {
                String currentUserId = auth.getCurrentUser().getUid();
                List<String> ratedBy = post.getRatedBy();
                if (ratedBy != null && ratedBy.contains(currentUserId)) {
                    ratedBy.remove(currentUserId);
                    
                    // Recalculate average rating
                    if (ratedBy.size() > 0) {
                        // Keep existing total rating but reduce count
                        post.setRatingCount(ratedBy.size());
                        // Note: In a real implementation, you'd need to track individual ratings
                        // For now, we'll keep the current average
                    } else {
                        // No ratings left
                        post.setRating(0.0);
                        post.setRatingCount(0);
                        post.setTotalRating(0.0);
                    }
                    
                    post.setRatedBy(ratedBy);
                    
                    // Update UI
                    updateRatingDisplay(holder, postId);
                    Toast.makeText(context, "Rating removed", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
    
    // Method to update star display based on rating
    private void updateStarDisplay(PostViewHolder holder, double rating) {
        ImageView[] stars = {holder.star1, holder.star2, holder.star3, holder.star4, holder.star5};
        
        for (int i = 0; i < stars.length; i++) {
            if (rating >= i + 1) {
                // Full star
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else if (rating > i) {
                // Half star
                stars[i].setImageResource(R.drawable.ic_star_half);
            } else {
                // Empty star
                stars[i].setImageResource(R.drawable.ic_star_empty);
            }
        }
    }
    
    private void setupStarClickListeners(PostViewHolder holder, String postId) {
        ImageView[] stars = {holder.star1, holder.star2, holder.star3, holder.star4, holder.star5};
        
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i + 1;
            stars[i].setOnClickListener(v -> {
                // Rate the post with the clicked star value
                ratePost(postId, starIndex, holder);
            });
        }
    }
    
    private void ratePost(String postId, int rating, PostViewHolder holder) {
        // Disable all star buttons temporarily to prevent multiple clicks
        ImageView[] stars = {holder.star1, holder.star2, holder.star3, holder.star4, holder.star5};
        for (ImageView star : stars) {
            star.setEnabled(false);
        }
        
        communityDataManager.ratePost(postId, (double) rating, new CommunityDataManager.UpdateCallback() {
            @Override
            public void onSuccess(String message) {
                // Re-enable star buttons
                for (ImageView star : stars) {
                    star.setEnabled(true);
                }
                
                Toast.makeText(context, "Post rated successfully!", Toast.LENGTH_SHORT).show();
                
                // Update the rating display immediately with optimistic update
                updateRatingOptimistically(postId, rating, holder);
                
                // Also refresh from server to get accurate data
                refreshPostRatingFromServer(postId, holder);
            }

            @Override
            public void onError(String error) {
                // Re-enable star buttons
                for (ImageView star : stars) {
                    star.setEnabled(true);
                }
                Toast.makeText(context, "Failed to rate post: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateRatingOptimistically(String postId, int newRating, PostViewHolder holder) {
        // Find the post in the list and update it optimistically
        for (CommunityPostData post : postsList) {
            if (post.getPostId().equals(postId)) {
                String currentUserId = auth.getCurrentUser().getUid();
                List<String> ratedBy = post.getRatedBy();
                if (ratedBy == null) {
                    ratedBy = new ArrayList<>();
                }
                
                boolean hasRated = ratedBy.contains(currentUserId);
                double currentRating = post.getRating();
                int currentCount = post.getRatingCount();
                double currentTotalRating = post.getTotalRating();
                
                double newAverageRating;
                int newCount;
                double newTotalRating;
                
                if (hasRated) {
                    // User has already rated, update their rating
                    // Remove old average and add new rating
                    newTotalRating = currentTotalRating - currentRating + newRating;
                    newCount = currentCount;
                    newAverageRating = newTotalRating / newCount;
                } else {
                    // New rating
                    ratedBy.add(currentUserId);
                    newTotalRating = currentTotalRating + newRating;
                    newCount = currentCount + 1;
                    newAverageRating = newTotalRating / newCount;
                }
                
                // Update post data
                post.setRating(newAverageRating);
                post.setRatingCount(newCount);
                post.setTotalRating(newTotalRating);
                post.setRatedBy(ratedBy);
                
                // Update UI immediately
                updateRatingDisplay(holder, postId);
                break;
            }
        }
    }
    
    private void refreshPostRatingFromServer(String postId, PostViewHolder holder) {
        // Get fresh data from server to ensure accuracy
        communityDataManager.getPostById(postId, new CommunityDataManager.PostCallback() {
            @Override
            public void onSuccess(CommunityPostData updatedPost) {
                // Update the post in the list
                for (int i = 0; i < postsList.size(); i++) {
                    if (postsList.get(i).getPostId().equals(postId)) {
                        postsList.set(i, updatedPost);
                        break;
                    }
                }
                
                // Update UI with server data
                updateRatingDisplay(holder, postId);
            }

            @Override
            public void onError(String error) {
                // If server refresh fails, keep the optimistic update
                // The optimistic update is already shown to the user
            }
        });
    }
    
    private void updateUserRatingStatus(PostViewHolder holder, CommunityPostData post) {
        // Check if current user has rated this post
        if (auth.getCurrentUser() != null && post.getRatedBy() != null && 
            post.getRatedBy().contains(auth.getCurrentUser().getUid())) {
            // User has rated this post - show visual feedback
            holder.averageRatingText.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            holder.ratingCount.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            // User hasn't rated this post
            holder.averageRatingText.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.ratingCount.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }
    }
    
    private void updateRatingDisplay(PostViewHolder holder, String postId) {
        // Find the post in the list and update its rating display
        for (CommunityPostData post : postsList) {
            if (post.getPostId().equals(postId)) {
                // Update star display
                updateStarDisplay(holder, post.getRating());
                
                // Update average rating text
                if (post.getRating() > 0) {
                    holder.averageRatingText.setText(String.format("%.1f", post.getRating()));
                } else {
                    holder.averageRatingText.setText("0.0");
                }
                
                // Update rating count
                holder.ratingCount.setText("(" + post.getRatingCount() + ")");
                
                // Update user rating status
                updateUserRatingStatus(holder, post);
                break;
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return postsList.size();
    }
    
    // Method to refresh all posts periodically to keep ratings updated
    public void refreshAllPosts() {
        for (int i = 0; i < postsList.size(); i++) {
            CommunityPostData post = postsList.get(i);
            communityDataManager.getPostById(post.getPostId(), new CommunityDataManager.PostCallback() {
                @Override
                public void onSuccess(CommunityPostData updatedPost) {
                    // Update the post in the list
                    for (int j = 0; j < postsList.size(); j++) {
                        if (postsList.get(j).getPostId().equals(updatedPost.getPostId())) {
                            postsList.set(j, updatedPost);
                            // Notify adapter of the change
                            notifyItemChanged(j);
                            break;
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    // Silently fail for background refresh
                }
            });
        }
    }
    
    // Method to update post in the list to maintain data consistency
    private void updatePostInList(CommunityPostData updatedPost) {
        for (int i = 0; i < postsList.size(); i++) {
            if (postsList.get(i).getPostId().equals(updatedPost.getPostId())) {
                postsList.set(i, updatedPost);
                break;
            }
        }
    }
    
    // Method to refresh like state for a specific post
    public void refreshLikeState(String postId, PostViewHolder holder) {
        for (CommunityPostData post : postsList) {
            if (post.getPostId().equals(postId)) {
                // Update like button state
                if (auth.getCurrentUser() != null && post.getLikedBy() != null && 
                    post.getLikedBy().contains(auth.getCurrentUser().getUid())) {
                    holder.likeButton.setImageResource(R.drawable.ic_heart_filled);
                } else {
                    holder.likeButton.setImageResource(R.drawable.ic_heart);
                }
                
                // Update likes count
                holder.likesCount.setText(post.getLikesCount() + " Likes");
                break;
            }
        }
    }
    
    // Method to refresh rating state for a specific post
    public void refreshRatingState(String postId, PostViewHolder holder) {
        for (CommunityPostData post : postsList) {
            if (post.getPostId().equals(postId)) {
                // Update rating display
                updateRatingDisplay(holder, postId);
                break;
            }
        }
    }
    
    // Method to refresh a specific post from server
    public void refreshPostFromServer(String postId, PostViewHolder holder) {
        communityDataManager.getPostById(postId, new CommunityDataManager.PostCallback() {
            @Override
            public void onSuccess(CommunityPostData updatedPost) {
                // Update the post in the list
                for (int i = 0; i < postsList.size(); i++) {
                    if (postsList.get(i).getPostId().equals(postId)) {
                        postsList.set(i, updatedPost);
                        break;
                    }
                }
                
                // Update UI with fresh data
                updateRatingDisplay(holder, postId);
                refreshLikeState(postId, holder);
            }

            @Override
            public void onError(String error) {
                // Silently fail - don't show error to user for background refresh
            }
        });
    }
    
    // Method to refresh user data dynamically
    private void refreshUserDataIfNeeded(CommunityPostData post, PostViewHolder holder) {
        // Check if user data is missing or outdated
        if (post.getAuthorUsername() == null || post.getAuthorUsername().isEmpty() || 
            post.getAuthorDisplayName() == null || post.getAuthorDisplayName().isEmpty()) {
            
            // Refresh user data from the source of truth (users collection)
            communityDataManager.refreshPostAuthorData(post.getPostId(), new CommunityDataManager.PostUpdateCallback() {
                @Override
                public void onSuccess(CommunityPostData updatedPost) {
                    // Update the UI with fresh data
                    if (updatedPost.getAuthorUsername() != null) {
                        holder.authorUsername.setText("@" + updatedPost.getAuthorUsername());
                    }
                    
                    if (updatedPost.getAuthorProfileImageUrl() != null && !updatedPost.getAuthorProfileImageUrl().isEmpty()) {
                        Glide.with(context)
                            .load(updatedPost.getAuthorProfileImageUrl())
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .circleCrop()
                            .into(holder.postAuthorProfilePicture);
                    }
                    
                    // Update the post in the list
                    int position = postsList.indexOf(post);
                    if (position >= 0) {
                        postsList.set(position, updatedPost);
                    }
                }
                
                @Override
                public void onError(String error) {
                    // Silently fail - don't show error to user for background refresh
                    // The post will still display with existing data
                }
            });
        }
    }

    private void downloadPostImage(CommunityPostData post) {
        if (post.getImageUrl() == null || post.getImageUrl().isEmpty()) {
            Toast.makeText(context, "No image to download", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading message
        Toast.makeText(context, "Downloading image...", Toast.LENGTH_SHORT).show();
        
        // Use Glide to download and save the image
        Glide.with(context)
            .asBitmap()
            .load(post.getImageUrl())
            .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                @Override
                public void onResourceReady(@NonNull android.graphics.Bitmap resource, 
                                         com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                    // Save the bitmap to device storage
                    saveImageToGallery(resource, post);
                }
                
                @Override
                public void onLoadFailed(android.graphics.drawable.Drawable errorDrawable) {
                    Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {
                    // Called when the load is cleared
                }
            });
    }

    private void saveImageToGallery(android.graphics.Bitmap bitmap, CommunityPostData post) {
        try {
            // Create filename with timestamp
            String fileName = "AURAfit_post_" + System.currentTimeMillis() + ".jpg";
            
            // Save to Pictures directory
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, 
                      android.os.Environment.DIRECTORY_PICTURES + "/AURAfit");
            
            android.net.Uri uri = context.getContentResolver().insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            
            if (uri != null) {
                java.io.OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream);
                    outputStream.close();
                    
                    // Notify gallery about the new image
                    context.sendBroadcast(new android.content.Intent(
                        android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                    
                    Toast.makeText(context, "Image saved to gallery!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postAuthorProfilePicture;
        TextView authorUsername;
        TextView timestamp;
        TextView caption;
        ImageView postImage;
        TextView likesCount;
        ImageView likeButton;
        ImageView downloadButton;
        TextView averageRatingText;
        TextView ratingCount;
        ImageView star1, star2, star3, star4, star5;
        
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            
            postAuthorProfilePicture = itemView.findViewById(R.id.postAuthorProfilePicture);
            authorUsername = itemView.findViewById(R.id.postAuthorUsername);
            timestamp = itemView.findViewById(R.id.postTimestamp);
            caption = itemView.findViewById(R.id.postCaption);
            postImage = itemView.findViewById(R.id.postImage);
            likesCount = itemView.findViewById(R.id.likesCount);
            likeButton = itemView.findViewById(R.id.likeButton);
            downloadButton = itemView.findViewById(R.id.downloadButton);
            averageRatingText = itemView.findViewById(R.id.averageRatingText);
            ratingCount = itemView.findViewById(R.id.ratingCount);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }
    }
    
    /**
     * Callback interface for when a post is liked/unliked
     */
    public interface OnPostLikedListener {
        void onPostLiked(String postId, boolean isLiked);
    }
}
