package com.aurafit.AuraFitApp.ui.wardrobe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.model.Outfit;
import com.aurafit.AuraFitApp.data.model.OutfitItem;
import com.aurafit.AuraFitApp.ui.generate.GenerateActivity;
import com.aurafit.AuraFitApp.ui.homepage.HomepageActivity;
import com.aurafit.AuraFitApp.ui.community.CommunityTabbedActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class WardrobeActivity extends AppCompatActivity {
    
    private TextView titleText;
    private RecyclerView outfitsRecyclerView;
    private WardrobeAdapter wardrobeAdapter;
    private List<Outfit> savedOutfits;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    // Navigation bar elements
    private ImageView galleryIcon;
    private ImageView communityIcon;
    private ImageView drawerIcon;
    private ImageView cameraIcon;
    
    // Broadcast receiver for outfit updates
    private BroadcastReceiver outfitUpdateReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);
        
        initializeViews();
        setupClickListeners();
        initializeFirebase();
        setupBroadcastReceiver();
        loadSavedOutfits();
    }
    
    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        outfitsRecyclerView = findViewById(R.id.outfitsRecyclerView);
        
        // Navigation bar elements
        galleryIcon = findViewById(R.id.galleryIcon);
        communityIcon = findViewById(R.id.communityIcon);
        drawerIcon = findViewById(R.id.drawerIcon);
        cameraIcon = findViewById(R.id.cameraIcon);
        
        titleText.setText("Saved Outfit");
    }
    
    private void setupClickListeners() {
        // Navigation bar click listeners
        galleryIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomepageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        
        communityIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommunityTabbedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        
        drawerIcon.setOnClickListener(v -> {
            // Already in wardrobe, do nothing or show feedback
            // Could show a toast or highlight the current tab
        });
        
        cameraIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, GenerateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }
    
    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    private void loadSavedOutfits() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view saved outfits", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        db.collection("savedOutfits")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    savedOutfits = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        // Get the outfits map from the document
                        Object outfitsData = documentSnapshot.get("outfits");
                        if (outfitsData instanceof Map) {
                            Map<String, Object> outfitsMap = (Map<String, Object>) outfitsData;
                            for (Map.Entry<String, Object> entry : outfitsMap.entrySet()) {
                                String outfitId = entry.getKey();
                                Object outfitData = entry.getValue();
                                if (outfitData instanceof Map) {
                                    // Convert map to Outfit object
                                    Outfit outfit = convertMapToOutfit((Map<String, Object>) outfitData);
                                    outfit.setOutfitId(outfitId);
                                    savedOutfits.add(outfit);
                                }
                            }
                        }
                    }
                    
                    setupRecyclerView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load saved outfits", Toast.LENGTH_SHORT).show();
                    setupRecyclerView();
                });
    }
    
    private void setupRecyclerView() {
        if (savedOutfits == null) {
            savedOutfits = new ArrayList<>();
        }
        
        wardrobeAdapter = new WardrobeAdapter(savedOutfits, this::onOutfitClick, this::onOutfitDelete);
        wardrobeAdapter.setDeleteListener(this::onOutfitDelete);
        outfitsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        outfitsRecyclerView.setAdapter(wardrobeAdapter);
    }
    
    private void onOutfitClick(Outfit outfit) {
        // Show outfit detail fragment
        showOutfitDetailFragment(outfit);
    }
    
    private void onOutfitDelete(Outfit outfit, int position) {
        // Remove from local list
        savedOutfits.remove(outfit);
        
        // Update adapter
        if (wardrobeAdapter != null) {
            wardrobeAdapter.removeOutfit(position);
        }
        
        // Show success message
        Toast.makeText(this, "Outfit deleted successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void showOutfitDetailFragment(Outfit outfit) {
        OutfitDetailFragment fragment = OutfitDetailFragment.newInstance(outfit);
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void refreshOutfits() {
        loadSavedOutfits();
    }

    public void addNewOutfit(Outfit outfit) {
        if (savedOutfits != null && wardrobeAdapter != null) {
            savedOutfits.add(outfit);
            wardrobeAdapter.addOutfit(outfit);
            Toast.makeText(this, "New outfit added to wardrobe!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBroadcastReceiver() {
        outfitUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("OUTFIT_SAVED".equals(intent.getAction())) {
                    String action = intent.getStringExtra("action");
                    if ("refresh_outfits".equals(action)) {
                        // Refresh the outfit list when a new outfit is saved
                        refreshOutfits();
                    }
                }
            }
        };
        
        // Register the broadcast receiver with proper flags for Android 13+
        IntentFilter filter = new IntentFilter("OUTFIT_SAVED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(outfitUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(outfitUpdateReceiver, filter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh outfits when returning to this activity
        refreshOutfits();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver
        if (outfitUpdateReceiver != null) {
            unregisterReceiver(outfitUpdateReceiver);
        }
    }
    
    public static void saveOutfitToFirebase(Outfit outfit, OnSaveCompleteListener listener) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (auth.getCurrentUser() == null) {
            listener.onSaveFailed("User not authenticated");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String outfitId = "outfit_" + System.currentTimeMillis();
        outfit.setOutfitId(outfitId);
        
        // Get current user's outfits document
        db.collection("savedOutfits")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> outfitsMap = new HashMap<>();
                    
                    if (documentSnapshot.exists()) {
                        // Get existing outfits
                        Object existingOutfits = documentSnapshot.get("outfits");
                        if (existingOutfits instanceof Map) {
                            outfitsMap = (Map<String, Object>) existingOutfits;
                        }
                    }
                    
                    // Add new outfit to the map
                    outfitsMap.put(outfitId, outfit);
                    
                    // Update the document with the new outfits map
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("outfits", outfitsMap);
                    updateData.put("lastUpdated", System.currentTimeMillis());
                    
                    db.collection("savedOutfits")
                            .document(userId)
                            .set(updateData)
                            .addOnSuccessListener(aVoid -> {
                                listener.onSaveSuccess(outfit);
                            })
                            .addOnFailureListener(e -> {
                                listener.onSaveFailed(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    listener.onSaveFailed(e.getMessage());
                });
    }
    
    private Outfit convertMapToOutfit(Map<String, Object> outfitData) {
        Outfit outfit = new Outfit();
        
        // Set basic properties
        if (outfitData.containsKey("name")) {
            outfit.setName((String) outfitData.get("name"));
        }
        if (outfitData.containsKey("weatherCondition")) {
            outfit.setWeatherCondition((String) outfitData.get("weatherCondition"));
        }
        if (outfitData.containsKey("createdAt")) {
            outfit.setCreatedAt((Long) outfitData.get("createdAt"));
        }
        
        // Convert items list
        if (outfitData.containsKey("items")) {
            Object itemsData = outfitData.get("items");
            if (itemsData instanceof List) {
                List<OutfitItem> items = new ArrayList<>();
                for (Object itemData : (List<?>) itemsData) {
                    if (itemData instanceof Map) {
                        OutfitItem item = convertMapToOutfitItem((Map<String, Object>) itemData);
                        items.add(item);
                    }
                }
                outfit.setItems(items);
            }
        }
        
        return outfit;
    }
    
    private OutfitItem convertMapToOutfitItem(Map<String, Object> itemData) {
        OutfitItem item = new OutfitItem();
        
        if (itemData.containsKey("name")) {
            item.setName((String) itemData.get("name"));
        }
        if (itemData.containsKey("category")) {
            item.setCategory((String) itemData.get("category"));
        }
        if (itemData.containsKey("arModelUrl")) {
            item.setArModelUrl((String) itemData.get("arModelUrl"));
        }
        if (itemData.containsKey("imageUrl")) {
            item.setImageUrl((String) itemData.get("imageUrl"));
        }
        
        return item;
    }
    
    public interface OnSaveCompleteListener {
        void onSaveSuccess(Outfit outfit);
        void onSaveFailed(String error);
    }
}
