package com.aurafit.AuraFitApp.ui.wardrobe;

import android.util.Log;
import com.aurafit.AuraFitApp.data.model.Outfit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class OutfitFirebaseManager {
    
    private static final String TAG = "OutfitFirebaseManager";
    private static final String COLLECTION_NAME = "savedOutfits";
    
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    
    public OutfitFirebaseManager() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public void deleteOutfitFromFirebase(Outfit outfit, OnDeleteCompleteListener listener) {
        if (auth.getCurrentUser() == null) {
            listener.onDeleteFailed("User not authenticated");
            return;
        }
        
        if (outfit.getOutfitId() == null || outfit.getOutfitId().isEmpty()) {
            listener.onDeleteFailed("Outfit ID is missing");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        // Get the user's outfits document (same as saveOutfitToFirebase)
        db.collection(COLLECTION_NAME)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onDeleteFailed("No outfits found for user");
                        return;
                    }
                    
                    // Get existing outfits map (same structure as saveOutfitToFirebase)
                    Map<String, Object> outfitsMap = new HashMap<>();
                    Object existingOutfits = documentSnapshot.get("outfits");
                    if (existingOutfits instanceof Map) {
                        outfitsMap = (Map<String, Object>) existingOutfits;
                    }
                    
                    // Remove the specific outfit by outfitId
                    if (outfitsMap.containsKey(outfit.getOutfitId())) {
                        outfitsMap.remove(outfit.getOutfitId());
                        
                        // Update the document (same structure as saveOutfitToFirebase)
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("outfits", outfitsMap);
                        updateData.put("lastUpdated", System.currentTimeMillis());
                        
                        // Use set() instead of update() to match saveOutfitToFirebase behavior
                        db.collection(COLLECTION_NAME)
                                .document(userId)
                                .set(updateData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Outfit deleted from Firebase: " + outfit.getOutfitId());
                                    listener.onDeleteSuccess(outfit);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating Firebase after deletion", e);
                                    listener.onDeleteFailed("Failed to update Firebase: " + e.getMessage());
                                });
                    } else {
                        listener.onDeleteFailed("Outfit not found in Firebase");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error reading outfits from Firebase", e);
                    listener.onDeleteFailed("Failed to read outfits: " + e.getMessage());
                });
    }
    
    public void deleteMultipleOutfitsFromFirebase(java.util.List<Outfit> outfits, OnBatchDeleteCompleteListener listener) {
        if (auth.getCurrentUser() == null) {
            listener.onBatchDeleteFailed("User not authenticated");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        // Get the user's outfits document (same as saveOutfitToFirebase)
        db.collection(COLLECTION_NAME)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onBatchDeleteFailed("No outfits found for user");
                        return;
                    }
                    
                    // Get existing outfits map (same structure as saveOutfitToFirebase)
                    Map<String, Object> outfitsMap = new HashMap<>();
                    Object existingOutfits = documentSnapshot.get("outfits");
                    if (existingOutfits instanceof Map) {
                        outfitsMap = (Map<String, Object>) existingOutfits;
                    }
                    
                    // Remove all specified outfits by outfitId
                    int deletedCount = 0;
                    final java.util.List<Outfit> outfitsToDelete = new java.util.ArrayList<>(outfits);
                    
                    for (Outfit outfit : outfitsToDelete) {
                        if (outfit.getOutfitId() != null && outfitsMap.containsKey(outfit.getOutfitId())) {
                            outfitsMap.remove(outfit.getOutfitId());
                            deletedCount++;
                        }
                    }
                    
                    final int finalDeletedCount = deletedCount;
                    
                    if (finalDeletedCount == 0) {
                        listener.onBatchDeleteFailed("No outfits found to delete");
                        return;
                    }
                    
                    // Update the document (same structure as saveOutfitToFirebase)
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("outfits", outfitsMap);
                    updateData.put("lastUpdated", System.currentTimeMillis());
                    
                    // Use set() instead of update() to match saveOutfitToFirebase behavior
                    db.collection(COLLECTION_NAME)
                            .document(userId)
                            .set(updateData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Deleted " + finalDeletedCount + " outfits from Firebase");
                                listener.onBatchDeleteSuccess(outfitsToDelete, finalDeletedCount);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating Firebase after batch deletion", e);
                                listener.onBatchDeleteFailed("Failed to update Firebase: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error reading outfits from Firebase", e);
                    listener.onBatchDeleteFailed("Failed to read outfits: " + e.getMessage());
                });
    }

    public boolean isUserAuthenticated() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    public interface OnDeleteCompleteListener {
        void onDeleteSuccess(Outfit deletedOutfit);
        void onDeleteFailed(String errorMessage);
    }

    public interface OnBatchDeleteCompleteListener {
        void onBatchDeleteSuccess(java.util.List<Outfit> deletedOutfits, int deletedCount);
        void onBatchDeleteFailed(String errorMessage);
    }
}
