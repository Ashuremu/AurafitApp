package com.aurafit.AuraFitApp.ui.wardrobe;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;
import com.aurafit.AuraFitApp.data.model.Outfit;

public class OutfitDeleteHelper {

    public static void showDeleteConfirmation(Context context, Outfit outfit,
                                           WardrobeAdapter.OnOutfitDeleteListener deleteListener, 
                                           int position) {
        new AlertDialog.Builder(context)
            .setTitle("Delete Outfit")
            .setMessage("Are you sure you want to delete \"" + outfit.getName() + "\"? This will remove it from your saved outfits.")
            .setPositiveButton("Delete", (dialog, which) -> {
                
                Toast.makeText(context, "Deleting outfit...", Toast.LENGTH_SHORT).show();
                
                
                OutfitFirebaseManager firebaseManager = new OutfitFirebaseManager();
                firebaseManager.deleteOutfitFromFirebase(outfit, new OutfitFirebaseManager.OnDeleteCompleteListener() {
                    @Override
                    public void onDeleteSuccess(Outfit deletedOutfit) {
                        
                        if (deleteListener != null) {
                            deleteListener.onOutfitDelete(deletedOutfit, position);
                        }
                        Toast.makeText(context, "Outfit deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                    
                    @Override
                    public void onDeleteFailed(String errorMessage) {
                        
                        Toast.makeText(context, "Failed to delete from cloud: " + errorMessage, Toast.LENGTH_LONG).show();
                        
                        
                        if (deleteListener != null) {
                            deleteListener.onOutfitDelete(outfit, position);
                        }
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public static void showDeleteConfirmation(Context context, String title, String message,
                                           WardrobeAdapter.OnOutfitDeleteListener deleteListener, 
                                           int position, Outfit outfit) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Delete", (dialog, which) -> {
                if (deleteListener != null) {
                    deleteListener.onOutfitDelete(outfit, position);
                }
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public static void showBatchDeleteConfirmation(Context context, java.util.List<Outfit> outfits,
                                                 OnBatchDeleteListener batchDeleteListener) {
        new AlertDialog.Builder(context)
            .setTitle("Delete Multiple Outfits")
            .setMessage("Are you sure you want to delete " + outfits.size() + " outfit(s)? This will remove them from your saved outfits.")
            .setPositiveButton("Delete All", (dialog, which) -> {
                
                Toast.makeText(context, "Deleting " + outfits.size() + " outfits...", Toast.LENGTH_SHORT).show();
                
                
                OutfitFirebaseManager firebaseManager = new OutfitFirebaseManager();
                firebaseManager.deleteMultipleOutfitsFromFirebase(outfits, new OutfitFirebaseManager.OnBatchDeleteCompleteListener() {
                    @Override
                    public void onBatchDeleteSuccess(java.util.List<Outfit> deletedOutfits, int deletedCount) {
                        
                        if (batchDeleteListener != null) {
                            batchDeleteListener.onBatchDeleteSuccess(deletedOutfits);
                        }
                        Toast.makeText(context, "Successfully deleted " + deletedCount + " outfits", Toast.LENGTH_SHORT).show();
                    }
                    
                    @Override
                    public void onBatchDeleteFailed(String errorMessage) {
                        
                        Toast.makeText(context, "Failed to delete from cloud: " + errorMessage, Toast.LENGTH_LONG).show();
                        
                        
                        if (batchDeleteListener != null) {
                            batchDeleteListener.onBatchDeleteSuccess(outfits);
                        }
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public interface OnBatchDeleteListener {
        void onBatchDeleteSuccess(java.util.List<Outfit> deletedOutfits);
    }
}
