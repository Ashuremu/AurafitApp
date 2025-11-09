package com.aurafit.AuraFitApp.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aurafit.AuraFitApp.R;

public class CommunityTabbedActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ViewPagerAdapter adapter;
    
    // Tab views
    private TextView profileTab, discoverTab, likesTab;
    
    // Other UI
    private ImageView backArrow, addPostButton;
    private ImageView drawerIcon, communityIcon, galleryIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_tabs);
        
        initializeViews();
        setupClickListeners();
        setupViewPager();
    }
    
    private void initializeViews() {
        // ViewPager
        viewPager = findViewById(R.id.viewPager);
        
        // Tab views
        profileTab = findViewById(R.id.profileTab);
        discoverTab = findViewById(R.id.discoverTab);
        likesTab = findViewById(R.id.likesTab);
        
        // Other UI
        backArrow = findViewById(R.id.backArrow);
        addPostButton = findViewById(R.id.addPostButton);
        
        // Bottom navigation
        galleryIcon = findViewById(R.id.galleryIcon);
        communityIcon = findViewById(R.id.communityIcon);
        drawerIcon = findViewById(R.id.drawerIcon);
    }
    
    private void setupViewPager() {
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Set up tab selection with ViewPager
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabSelection(position);
                
                // Refresh data when tab changes
                refreshCurrentFragment(position);
            }
        });
    }
    
    private void updateTabSelection(int position) {
        // Reset all tabs
        profileTab.setTextColor(getResources().getColor(R.color.gray));
        discoverTab.setTextColor(getResources().getColor(R.color.gray));
        likesTab.setTextColor(getResources().getColor(R.color.gray));
        
        // Remove background from all tabs
        profileTab.setBackground(null);
        discoverTab.setBackground(null);
        likesTab.setBackground(null);
        
        // Highlight selected tab
        switch (position) {
            case 0: // Profile
                profileTab.setTextColor(getResources().getColor(R.color.black));
                profileTab.setBackground(getResources().getDrawable(R.drawable.tab_underline));
                break;
            case 1: // Discover
                discoverTab.setTextColor(getResources().getColor(R.color.black));
                discoverTab.setBackground(getResources().getDrawable(R.drawable.tab_underline));
                break;
            case 2: // Likes
                likesTab.setTextColor(getResources().getColor(R.color.black));
                likesTab.setBackground(getResources().getDrawable(R.drawable.tab_underline));
                break;
        }
    }
    
    private void setupClickListeners() {
        backArrow.setOnClickListener(v -> finish());
        
        addPostButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreatePostActivity.class);
            startActivityForResult(intent, 1001);
        });
        
        // Bottom navigation click listeners
        galleryIcon.setOnClickListener(v -> {
            // Navigate back to homepage
            Intent intent = new Intent(this, com.aurafit.AuraFitApp.ui.homepage.HomepageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        
        communityIcon.setOnClickListener(v -> {
            // Already in community, do nothing or show feedback
            Toast.makeText(this, "Already in community", Toast.LENGTH_SHORT).show();
        });
        
        drawerIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.aurafit.AuraFitApp.ui.wardrobe.WardrobeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        
        // Tab click listeners
        profileTab.setOnClickListener(v -> {
            viewPager.setCurrentItem(0, true);
        });
        
        discoverTab.setOnClickListener(v -> {
            viewPager.setCurrentItem(1, true);
        });
        
        likesTab.setOnClickListener(v -> {
            viewPager.setCurrentItem(2, true);
        });
    }

    private void refreshCurrentFragment(int position) {
        Fragment currentFragment = adapter.getFragment(position);
        if (currentFragment != null) {
            if (currentFragment instanceof ProfileFragment) {
                ((ProfileFragment) currentFragment).refreshData();
            } else if (currentFragment instanceof DiscoverFragment) {
                ((DiscoverFragment) currentFragment).refreshData();
            } else if (currentFragment instanceof LikesFragment) {
                ((LikesFragment) currentFragment).refreshData();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Refresh the current fragment when returning from CreatePostActivity
            // The fragments will handle their own refresh logic
            Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}
