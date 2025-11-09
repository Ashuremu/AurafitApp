package com.aurafit.AuraFitApp.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.FirestoreManager;
import com.aurafit.AuraFitApp.ui.homepage.adapter.WardrobeItemAdapter;
import com.aurafit.AuraFitApp.ui.homepage.model.WardrobeItem;

import java.util.ArrayList;
import java.util.List;

public class CategoryClothesActivity extends AppCompatActivity {

    private static final String TAG = "CategoryClothesActivity";
    
    private TextView tvCategoryTitle;
    private Button btnMen, btnWomen;
    private ImageButton btnBack;
    private RecyclerView recyclerViewClothes;
    private LinearLayout layoutLoading, layoutError, layoutEmpty;
    private Button btnRetry;

    private WardrobeItemAdapter adapter;
    private List<WardrobeItem> allClothes = new ArrayList<>();
    private List<WardrobeItem> filteredClothes = new ArrayList<>();
    private String currentCategory;
    private String currentGender = "Male";
    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_clothes);

        Log.d(TAG, "=== CategoryClothesActivity onCreate ===");

        // Initialize FirestoreManager
        firestoreManager = FirestoreManager.getInstance();
        Log.d(TAG, "FirestoreManager initialized");

        // Get category from intent
        currentCategory = getIntent().getStringExtra("category");
        if (currentCategory == null) {
            currentCategory = "Top";
        }
        Log.d(TAG, "Current category: " + currentCategory);

        initializeViews();
        setupClickListeners();
        updateGenderButtons();
        loadClothes();
    }

    private void initializeViews() {
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        btnMen = findViewById(R.id.btnMen);
        btnWomen = findViewById(R.id.btnWomen);
        btnBack = findViewById(R.id.btnBack);
        recyclerViewClothes = findViewById(R.id.recyclerViewClothes);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutError = findViewById(R.id.layoutError);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnRetry = findViewById(R.id.btnRetry);

        // Set category title
        tvCategoryTitle.setText(currentCategory);

        // Setup RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewClothes.setLayoutManager(layoutManager);
        adapter = new WardrobeItemAdapter(filteredClothes);
        adapter.setOnItemClickListener(this::onClothesItemClick);
        recyclerViewClothes.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnMen.setOnClickListener(v -> {
            Log.d(TAG, "Men button clicked");
            currentGender = "Male";
            updateGenderButtons();
            filterClothes();
        });

        btnWomen.setOnClickListener(v -> {
            Log.d(TAG, "Women button clicked");
            currentGender = "Female";
            updateGenderButtons();
            filterClothes();
        });


        btnRetry.setOnClickListener(v -> loadClothes());
    }

    private void updateGenderButtons() {
        if ("Male".equals(currentGender)) {
            btnMen.setBackgroundResource(R.drawable.category_button_selected);
            btnWomen.setBackgroundResource(R.drawable.category_button_unselected);
        } else {
            btnMen.setBackgroundResource(R.drawable.category_button_unselected);
            btnWomen.setBackgroundResource(R.drawable.category_button_selected);
        }
    }

    private void loadClothes() {
        Log.d(TAG, "=== loadClothes() called ===");
        showLoadingState();

        Log.d(TAG, "Fetching wardrobe items from Firestore...");
        firestoreManager.getWardrobeItems(new FirestoreManager.WardrobeItemsCallback() {
            @Override
            public void onSuccess(List<WardrobeItem> items) {
                Log.d(TAG, "=== Firestore Success ===");
                Log.d(TAG, "Total items fetched: " + items.size());
                
                allClothes.clear();
                allClothes.addAll(items);
                
                // Log all items with their categories and genders
                for (int i = 0; i < items.size(); i++) {
                    WardrobeItem item = items.get(i);
                    Log.d(TAG, "Item " + i + ": " + item.getName() + 
                          " | Category: " + item.getMainCategory() + 
                          " | Gender: " + item.getGender());
                }
                
                filterClothes();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "=== Firestore Failure ===");
                Log.e(TAG, "Error fetching wardrobe items: " + e.getMessage(), e);
                showErrorState();
                Toast.makeText(CategoryClothesActivity.this, "Failed to load clothes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterClothes() {
        Log.d(TAG, "=== filterClothes() called ===");
        Log.d(TAG, "Filtering by category: " + currentCategory + " and gender: " + currentGender);
        
        filteredClothes.clear();
        
        for (WardrobeItem item : allClothes) {
            boolean categoryMatch = item.getMainCategory() != null && item.getMainCategory().equals(currentCategory);
            boolean genderMatch = item.getGender() != null && item.getGender().equals(currentGender);
            
            Log.d(TAG, "Item: " + item.getName() + 
                  " | Category match: " + categoryMatch + " (" + item.getMainCategory() + " vs " + currentCategory + ")" +
                  " | Gender match: " + genderMatch + " (" + item.getGender() + " vs " + currentGender + ")");
            
            if (categoryMatch && genderMatch) {
                filteredClothes.add(item);
                Log.d(TAG, "✓ Added to filtered list: " + item.getName());
            }
        }

        Log.d(TAG, "Filtered results: " + filteredClothes.size() + " items");
        adapter.notifyDataSetChanged();
        
        if (filteredClothes.isEmpty()) {
            Log.d(TAG, "No items found - showing empty state");
            showEmptyState();
        } else {
            Log.d(TAG, "Items found - showing content state");
            showContentState();
        }
    }

    private void onClothesItemClick(WardrobeItem item) {
        // TODO: Navigate to item details
        Toast.makeText(this, "Opening " + item.getName(), Toast.LENGTH_SHORT).show();
    }

    private void showLoadingState() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        recyclerViewClothes.setVisibility(View.GONE);
    }

    private void showErrorState() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        recyclerViewClothes.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerViewClothes.setVisibility(View.GONE);
    }

    private void showContentState() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        recyclerViewClothes.setVisibility(View.VISIBLE);
    }
}
