package com.aurafit.AuraFitApp.ui.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.CommunityDataManager;
import com.aurafit.AuraFitApp.data.UserDataManager;
import com.aurafit.AuraFitApp.data.model.CommunityPostData;
import com.aurafit.AuraFitApp.data.model.CommunityUserData;
import com.aurafit.AuraFitApp.data.model.User;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private CommunityDataManager communityDataManager;
    private UserDataManager userDataManager;
    
    // UI Components
    private TextView displayName, username;
    private TextView postsCount;
    private ImageView profilePicture;
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<CommunityPostData> postsList;
    
    // Pagination variables
    private DocumentSnapshot lastDocument;
    private boolean isLoading = false;
    private boolean hasMorePosts = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // Initialize Data Managers
        communityDataManager = CommunityDataManager.getInstance();
        userDataManager = UserDataManager.getInstance();
        
        // Initialize UI components
        initializeViews(view);
        
        // Load user profile data
        loadUserProfile();
        
        // Load posts
        loadPosts();
        
        return view;
    }
    
    private void initializeViews(View view) {
        // Profile info
        displayName = view.findViewById(R.id.displayName);
        username = view.findViewById(R.id.username);
        postsCount = view.findViewById(R.id.postsCount);
        profilePicture = view.findViewById(R.id.profilePicture);
        
        // RecyclerView
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsList = new ArrayList<>();
        postAdapter = new PostAdapter(postsList, getContext());
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
    
    private void loadUserProfile() {
        // Always use users collection as source of truth for profile data
        userDataManager.getCurrentUserProfile(new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (user != null) {
                            // Update UI with user data from users collection
                            updateUIWithUserData(user);
                            
                            // Update post count directly from user data
                            updatePostCount(user);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to load user data: " + error, Toast.LENGTH_SHORT).show();
                        postsCount.setText("0");
                    });
                }
            }
        });
    }
    
    private void updateUIWithUserData(User user) {
        // Update UI with user data from users collection
        if (user.getDisplayName() != null) {
            displayName.setText(user.getDisplayName());
        } else if (user.getUsername() != null) {
            displayName.setText(user.getUsername());
        } else {
            displayName.setText("User");
        }
        
        if (user.getUsername() != null) {
            username.setText("@" + user.getUsername());
        } else {
            username.setText("@user");
        }
        
        // Load profile image
        try {
            java.lang.reflect.Method getMethod = user.getClass().getMethod("getProfileImageUrl");
            Object urlObj = getMethod.invoke(user);
            if (urlObj instanceof String) {
                String url = (String) urlObj;
                if (url != null && !url.isEmpty()) {
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(profilePicture);
                } else {
                    profilePicture.setImageResource(R.drawable.ic_person);
                }
            } else {
                profilePicture.setImageResource(R.drawable.ic_person);
            }
        } catch (Exception e) {
            profilePicture.setImageResource(R.drawable.ic_person);
        }
    }
    
    private void updatePostCount(User user) {
        // Update post count from user data
        if (user.getPostsCount() != null) {
            postsCount.setText(String.valueOf(user.getPostsCount()));
        } else {
            postsCount.setText("0");
        }
    }
    
    
    private void loadPosts() {
        // Reset pagination
        lastDocument = null;
        hasMorePosts = true;
        isLoading = false;
        
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        communityDataManager.getUserPosts(userId, new CommunityDataManager.PostsCallback() {
            @Override
            public void onSuccess(List<CommunityPostData> posts) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        postsList.clear();
                        postsList.addAll(posts);
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
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        communityDataManager.getUserPosts(userId, lastDocument, new CommunityDataManager.PaginatedPostsCallback() {
            @Override
            public void onSuccess(List<CommunityPostData> posts, DocumentSnapshot lastDoc) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (posts.size() > 0) {
                            postsList.addAll(posts);
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

    public void refreshData() {
        if (getActivity() != null) {
            // Refresh user profile data (includes post count)
            loadUserProfile();
            
            // Refresh posts
            loadPosts();
        }
    }

    public void refreshPostCount() {
        if (getActivity() != null) {
            userDataManager.getCurrentUserProfile(new UserDataManager.ProfileCallback() {
                @Override
                public void onSuccess(User user) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updatePostCount(user);
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            postsCount.setText("0");
                        });
                    }
                }
            });
        }
    }
}
