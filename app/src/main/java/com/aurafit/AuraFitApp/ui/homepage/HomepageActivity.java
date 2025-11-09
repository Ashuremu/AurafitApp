package com.aurafit.AuraFitApp.ui.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.UserDataManager;
import com.aurafit.AuraFitApp.data.FirestoreManager;
import com.aurafit.AuraFitApp.data.model.User;
import com.aurafit.AuraFitApp.ui.homepage.adapter.RecommendationAdapter;
import com.aurafit.AuraFitApp.ui.homepage.adapter.WardrobeItemAdapter;
import com.aurafit.AuraFitApp.ui.homepage.model.Recommendation;
import com.aurafit.AuraFitApp.ui.homepage.model.WardrobeItem;
import com.aurafit.AuraFitApp.ui.settings.SettingsActivity;
import com.aurafit.AuraFitApp.ui.community.CommunityTabbedActivity;
import com.aurafit.AuraFitApp.ui.reservation.ReservedItemsActivity;
import com.aurafit.AuraFitApp.ui.category.CategoryClothesActivity;
import com.aurafit.AuraFitApp.ui.wardrobe.WardrobeActivity;

import java.util.ArrayList;
import java.util.List;

public class HomepageActivity extends AppCompatActivity {

    private RecyclerView recommendationRecyclerView;
    private SearchView searchView;
    private ImageView hamburgerMenu;
    private ImageView bookmarkIcon;
    private ImageView drawerIcon;
    private ImageView communityIcon;
    private ImageView cameraIcon;
    private ImageView galleryIcon;
    
    // Category components
    private LinearLayout categoryTop;
    private LinearLayout categoryBottom;
    private LinearLayout categoryShoes;
    private LinearLayout categoryAccessories;
    
    // Onboarding components
    private LinearLayout onboardingContainer;
    private LinearLayout recommendationContainer;
    private UserDataManager userDataManager;
    private FirestoreManager firestoreManager;
    private WardrobeItemAdapter wardrobeItemAdapter;
    private List<WardrobeItem> allWardrobeItems = new ArrayList<>();
    private List<WardrobeItem> filteredWardrobeItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        userDataManager = UserDataManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        
        initializeViews();
        setupRecommendationRecyclerView();
        setupClickListeners();
        checkOnboardingStatus();
    }

    private void initializeViews() {
        recommendationRecyclerView = findViewById(R.id.recommendationRecyclerView);
        searchView = findViewById(R.id.searchView);
        hamburgerMenu = findViewById(R.id.hamburgerMenu);
        bookmarkIcon = findViewById(R.id.bookmarkIcon);
        drawerIcon = findViewById(R.id.drawerIcon);
        communityIcon = findViewById(R.id.communityIcon);
        cameraIcon = findViewById(R.id.cameraIcon);
        galleryIcon = findViewById(R.id.galleryIcon);
        
        // Category views
        categoryTop = findViewById(R.id.categoryTop);
        categoryBottom = findViewById(R.id.categoryBottom);
        categoryShoes = findViewById(R.id.categoryShoes);
        categoryAccessories = findViewById(R.id.categoryAccessories);
        
        // Onboarding views
        onboardingContainer = findViewById(R.id.onboardingContainer);
        recommendationContainer = findViewById(R.id.recommendationContainer);
    }


    private void setupRecommendationRecyclerView() {
        // Initialize with empty list
        List<WardrobeItem> wardrobeItems = new ArrayList<>();
        wardrobeItemAdapter = new WardrobeItemAdapter(wardrobeItems);
        
        // Set up click listener for wardrobe items
        wardrobeItemAdapter.setOnItemClickListener(wardrobeItem -> {
            // Handle item click - you can add navigation to item details here
            Toast.makeText(this, "Selected: " + wardrobeItem.getName(), Toast.LENGTH_SHORT).show();
        });
        
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recommendationRecyclerView.setLayoutManager(gridLayoutManager);
        recommendationRecyclerView.setAdapter(wardrobeItemAdapter);
        
        // Setup infinite scrolling
        setupInfiniteScrolling(gridLayoutManager);
        
        // Fetch wardrobe items from Firestore
        loadWardrobeItems();
    }

    private void setupInfiniteScrolling(GridLayoutManager layoutManager) {
        // No pagination needed - just load all items at once
        // Scrolling is enabled for browsing all items
    }

    private void setupClickListeners() {
        hamburgerMenu.setOnClickListener(v -> openSettings());
        bookmarkIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservedItemsActivity.class);
            startActivity(intent);
        });
        
        drawerIcon.setOnClickListener(v -> {
            // Gallery icon - already on homepage, do nothing or show feedback
            Toast.makeText(this, "Already on homepage", Toast.LENGTH_SHORT).show();
        });
        communityIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommunityTabbedActivity.class);
            startActivity(intent);
        });
        cameraIcon.setOnClickListener(v -> handleCameraClick());
        galleryIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, WardrobeActivity.class);
            startActivity(intent);
        });
        
        // Setup category click listeners
        setupCategoryClickListeners();
        
        // Setup search functionality
        setupSearchView();
    }

    private void setupCategoryClickListeners() {
        categoryTop.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryClothesActivity.class);
            intent.putExtra("category", "Top");
            startActivity(intent);
        });
        
        categoryBottom.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryClothesActivity.class);
            intent.putExtra("category", "Bottom");
            startActivity(intent);
        });
        
        categoryShoes.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryClothesActivity.class);
            intent.putExtra("category", "Shoes");
            startActivity(intent);
        });
        
        categoryAccessories.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryClothesActivity.class);
            intent.putExtra("category", "Accessories");
            startActivity(intent);
        });
    }

    private void checkOnboardingStatus() {
        userDataManager.getCurrentUserProfile(new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    // Show onboarding if user hasn't captured body measurements yet
                    if (user.getBody_measurements_captured() == null || !user.getBody_measurements_captured()) {
                        showOnboarding();
                    } else {
                        showRecommendations();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // If we can't get user data, assume new user and show onboarding
                    showOnboarding();
                });
            }
        });
    }

    private void showOnboarding() {
        onboardingContainer.setVisibility(View.VISIBLE);
        recommendationContainer.setVisibility(View.GONE);
    }

    private void showRecommendations() {
        onboardingContainer.setVisibility(View.GONE);
        recommendationContainer.setVisibility(View.VISIBLE);
        // Refresh wardrobe items when showing recommendations
        loadWardrobeItems();
    }

    private void setupSearchView() {
        if (searchView != null) {
            // Keep search view iconified (collapsed) by default to prevent auto-opening keyboard
            searchView.setIconified(true);
            searchView.setQueryHint("Search wardrobe items...");
            
            // Note: setCloseIcon method not available in this SearchView version
            // The close icon will remain visible but functionality is handled by search logic
            
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterWardrobeItems(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterWardrobeItems(newText);
                    return true;
                }
            });
            
            // Add click listener to expand search view when user clicks on it
            searchView.setOnClickListener(v -> {
                searchView.setIconified(false);
            });
        } else {
            Toast.makeText(this, "SearchView not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterWardrobeItems(String query) {
        if (allWardrobeItems == null || allWardrobeItems.isEmpty()) {
            return;
        }
        
        if (query == null || query.trim().isEmpty()) {
            filteredWardrobeItems = new ArrayList<>(allWardrobeItems);
        } else {
            filteredWardrobeItems = new ArrayList<>();
            String searchQuery = query.toLowerCase().trim();
            
            for (WardrobeItem item : allWardrobeItems) {
                boolean matches = false;
                
                // Search in name
                if (item.getName() != null && item.getName().toLowerCase().contains(searchQuery)) {
                    matches = true;
                }
                
                // Search in description
                if (!matches && item.getDescription() != null && 
                    item.getDescription().toLowerCase().contains(searchQuery)) {
                    matches = true;
                }
                
                // Search in categories
                if (!matches && item.getCategories() != null) {
                    for (String category : item.getCategories()) {
                        if (category.toLowerCase().contains(searchQuery)) {
                            matches = true;
                            break;
                        }
                    }
                }
                
                // Search in gender
                if (!matches && item.getGender() != null && 
                    item.getGender().toLowerCase().contains(searchQuery)) {
                    matches = true;
                }
                
                // Search in weather
                if (!matches && item.getWeather() != null && 
                    item.getWeather().toLowerCase().contains(searchQuery)) {
                    matches = true;
                }
                
                if (matches) {
                    filteredWardrobeItems.add(item);
                }
            }
        }
        
        if (wardrobeItemAdapter != null) {
            wardrobeItemAdapter.updateItems(filteredWardrobeItems);
        }
    }

    private void loadWardrobeItems() {
        firestoreManager.getWardrobeItems(new FirestoreManager.WardrobeItemsCallback() {
            @Override
            public void onSuccess(List<WardrobeItem> items) {
                runOnUiThread(() -> {
                    allWardrobeItems = new ArrayList<>(items);
                    filteredWardrobeItems = new ArrayList<>(items);
                    
                    if (wardrobeItemAdapter != null) {
                        wardrobeItemAdapter.updateItems(filteredWardrobeItems);
                        Toast.makeText(HomepageActivity.this, 
                            "Loaded " + items.size() + " wardrobe items", 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(HomepageActivity.this, 
                        "Failed to load wardrobe items: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
            }
        });
    }

    private void handleCameraClick() {
        // Check if user has body measurements
        userDataManager.getCurrentUserProfile(new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    if (user.getBody_measurements_captured() != null && user.getBody_measurements_captured()) {
                        // User has measurements - go to GenerateActivity for outfit generation
                        Intent intent = new Intent(HomepageActivity.this, com.aurafit.AuraFitApp.ui.generate.GenerateActivity.class);
                        startActivity(intent);
                    } else {
                        // User doesn't have measurements - go to MeasureActivity
                        Intent intent = new Intent(HomepageActivity.this, com.aurafit.AuraFitApp.ui.measure.MeasureActivity.class);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // If we can't get user data, assume new user and go to measurements
                    Intent intent = new Intent(HomepageActivity.this, com.aurafit.AuraFitApp.ui.measure.MeasureActivity.class);
                    startActivity(intent);
                });
            }
        });
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void simulateMeasurementsSaved() {
        User updatedUser = new User();
        updatedUser.setBodyMeasurements("height:170cm,chest:90cm,waist:75cm,hips:95cm");
        updatedUser.setBody_measurements_captured(true);
        
        userDataManager.updateCurrentUserData(updatedUser, new UserDataManager.UpdateCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(HomepageActivity.this, "Body measurements saved!", Toast.LENGTH_SHORT).show();
                    showRecommendations();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(HomepageActivity.this, "Failed to save measurements: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
}
