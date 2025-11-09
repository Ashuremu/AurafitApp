package com.aurafit.AuraFitApp.ui.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.CommunityDataManager;
import com.aurafit.AuraFitApp.data.model.CommunityPostData;

import java.util.ArrayList;
import java.util.List;

public class LikesFragment extends Fragment implements PostAdapter.OnPostLikedListener {

    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private CommunityDataManager communityDataManager;
    private List<CommunityPostData> likedPosts;
    
    // Pagination variables
    private DocumentSnapshot lastDocument;
    private boolean isLoading = false;
    private boolean hasMorePosts = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_likes, container, false);
        
        communityDataManager = CommunityDataManager.getInstance();
        likedPosts = new ArrayList<>();
        
        initializeViews(view);
        loadLikedPosts();
        
        return view;
    }
    
    private void initializeViews(View view) {
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        postAdapter = new PostAdapter(likedPosts, getContext(), this);
        postsRecyclerView.setAdapter(postAdapter);
        
        // Set up infinite scrolling
        setupInfiniteScroll();
    }
    
    private void setupInfiniteScroll() {
        postsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    
                    // Check if we need to load more posts
                    if (!isLoading && hasMorePosts && 
                        (firstVisibleItemPosition + visibleItemCount) >= totalItemCount - 2) {
                        loadMorePosts();
                    }
                }
            }
        });
    }
    
    private void loadLikedPosts() {
        // Reset pagination
        lastDocument = null;
        hasMorePosts = true;
        isLoading = false;
        
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Load both liked posts and rated posts
        loadLikedAndRatedPosts(userId);
    }
    
    private void loadLikedAndRatedPosts(String userId) {
        // Load only liked posts to avoid index issues
        communityDataManager.getLikedPosts(userId, new CommunityDataManager.PostsCallback() {
            @Override
            public void onSuccess(List<CommunityPostData> likedPostsList) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        likedPosts.clear();
                        likedPosts.addAll(likedPostsList);
                        postAdapter.notifyDataSetChanged();
                        
                        // Update pagination state
                        if (likedPosts.size() < 10) {
                            hasMorePosts = false;
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to load liked posts: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    private void loadMorePosts() {
        if (isLoading || !hasMorePosts) {
            return;
        }
        
        isLoading = true;
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        communityDataManager.getLikedPosts(userId, lastDocument, new CommunityDataManager.PaginatedPostsCallback() {
            @Override
            public void onSuccess(List<CommunityPostData> posts, DocumentSnapshot lastDoc) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (posts.size() > 0) {
                            likedPosts.addAll(posts);
                            postAdapter.notifyDataSetChanged();
                            
                            // Update lastDocument for next pagination
                            lastDocument = lastDoc;
                            
                            // Update pagination state
                            if (posts.size() < 10) {
                                hasMorePosts = false;
                            }
                        } else {
                            hasMorePosts = false;
                        }
                        
                        isLoading = false;
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        isLoading = false;
                        Toast.makeText(getContext(), "Failed to load more liked posts: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    @Override
    public void onPostLiked(String postId, boolean isLiked) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isLiked) {
                    // Post was liked - add it to the liked posts if not already present
                    addPostToLikedPosts(postId);
                } else {
                    // Post was unliked - remove it from the liked posts
                    removePostFromLikedPosts(postId);
                }
            });
        }
    }

    private void addPostToLikedPosts(String postId) {
        // Check if post is already in the list
        boolean alreadyExists = false;
        for (CommunityPostData post : likedPosts) {
            if (post.getPostId().equals(postId)) {
                alreadyExists = true;
                break;
            }
        }
        
        if (!alreadyExists) {
            // Fetch the post details and add to the list
            communityDataManager.getPostById(postId, new CommunityDataManager.PostCallback() {
                @Override
                public void onSuccess(CommunityPostData post) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Add to the beginning of the list (most recent first)
                            likedPosts.add(0, post);
                            postAdapter.notifyItemInserted(0);
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    // Silently fail - don't show error to user
                }
            });
        }
    }

    private void removePostFromLikedPosts(String postId) {
        for (int i = 0; i < likedPosts.size(); i++) {
            if (likedPosts.get(i).getPostId().equals(postId)) {
                likedPosts.remove(i);
                postAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }
    
    /**
     * Public method to refresh all data when tab is selected
     */
    public void refreshData() {
        if (getActivity() != null) {
            // Refresh liked posts
            loadLikedPosts();
        }
    }
}
