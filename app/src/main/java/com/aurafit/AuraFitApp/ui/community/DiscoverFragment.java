package com.aurafit.AuraFitApp.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class DiscoverFragment extends Fragment {

    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private CommunityDataManager communityDataManager;
    private List<CommunityPostData> allPosts;
    
    // Pagination variables
    private DocumentSnapshot lastDocument;
    private boolean isLoading = false;
    private boolean hasMorePosts = true;
    
    // Auto-refresh variables
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL = 30000; // 30 seconds

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        
        communityDataManager = CommunityDataManager.getInstance();
        allPosts = new ArrayList<>();
        
        initializeViews(view);
        loadAllPosts();
        startAutoRefresh();
        
        return view;
    }
    
    private void initializeViews(View view) {
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        postAdapter = new PostAdapter(allPosts, getContext());
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
    
    private void loadAllPosts() {
        // Reset pagination
        lastDocument = null;
        hasMorePosts = true;
        isLoading = false;
        
        // Load all posts (will be sorted by timestamp, rating sorting removed to avoid index issues)
        communityDataManager.getAllPosts(new CommunityDataManager.PostsCallback() {
            @Override
            public void onSuccess(List<CommunityPostData> posts) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allPosts.clear();
                        allPosts.addAll(posts);
                        postAdapter.notifyDataSetChanged();
                        
                        // Update pagination state
                        if (posts.size() < 10) {
                            hasMorePosts = false;
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to load posts: " + error, Toast.LENGTH_SHORT).show();
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
        
        communityDataManager.getAllPosts(lastDocument, new CommunityDataManager.PaginatedPostsCallback() {
            @Override
            public void onSuccess(List<CommunityPostData> posts, DocumentSnapshot lastDoc) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (posts.size() > 0) {
                            allPosts.addAll(posts);
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
                        Toast.makeText(getContext(), "Failed to load more posts: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    private void startAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null && isAdded()) {
                    // Refresh all posts to get updated ratings
                    if (postAdapter != null) {
                        postAdapter.refreshAllPosts();
                    }
                    
                    // Schedule next refresh
                    refreshHandler.postDelayed(this, REFRESH_INTERVAL);
                }
            }
        };
        
        // Start the first refresh after the interval
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }
    
    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoRefresh();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        stopAutoRefresh();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        startAutoRefresh();
    }

    public void refreshData() {
        if (getActivity() != null) {
            // Refresh all posts
            loadAllPosts();
        }
    }
}
