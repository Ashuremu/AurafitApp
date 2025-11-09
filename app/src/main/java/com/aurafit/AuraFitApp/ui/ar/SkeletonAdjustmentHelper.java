package com.aurafit.AuraFitApp.ui.ar;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SkeletonAdjustmentHelper {
    
    private static final String TAG = "SkeletonAdjustmentHelper";
    
    private Context context;
    private ArOverlayView overlayView;
    private SkeletonAdjustmentControls controls;
    
    // UI References
    private Button toggleButton;
    private LinearLayout skeletonContainer;
    private LinearLayout directionalControls;
    private TextView statusText;
    
    // State
    private boolean isControlsVisible = false;
    
    public SkeletonAdjustmentHelper(Context context, ArOverlayView overlayView) {
        this.context = context;
        this.overlayView = overlayView;
        this.controls = new SkeletonAdjustmentControls(context, overlayView);
    }

    public void initialize() {
        // Enable skeleton adjustment in the overlay view
        overlayView.enableSkeletonAdjustment();
        
        // Apply the current adjustment
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        
        Log.d(TAG, "Skeleton adjustment system initialized");
    }

    public void setupControls(Button toggleButton, LinearLayout skeletonContainer,
                            LinearLayout directionalControls, TextView statusText) {
        this.toggleButton = toggleButton;
        this.skeletonContainer = skeletonContainer;
        this.directionalControls = directionalControls;
        this.statusText = statusText;
        
        // Initially show container but hide directional controls
        if (skeletonContainer != null) {
            skeletonContainer.setVisibility(View.VISIBLE);
        }
        if (directionalControls != null) {
            directionalControls.setVisibility(View.GONE);
        }
        
        // Setup toggle button
        if (toggleButton != null) {
            toggleButton.setOnClickListener(v -> toggleControls());
        }
        
        // Setup directional buttons
        setupDirectionalButtons();
        
        // Update status text
        updateStatusText();
    }
    
    public void toggleControls() {
        if (skeletonContainer == null) {
            Log.w(TAG, "Skeleton container not set");
            return;
        }
        
        isControlsVisible = !isControlsVisible;
        
        if (isControlsVisible) {
            showControls();
        } else {
            hideControls();
        }
    }
    
    public void showControls() {
        if (directionalControls != null) {
            directionalControls.setVisibility(View.VISIBLE);
        }
        if (toggleButton != null) {
            toggleButton.setText("Hide\nControls");
        }
        isControlsVisible = true;
        
        // Show toast message
        Toast.makeText(context, "Skeleton adjustment controls enabled", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "Skeleton controls shown");
    }
    
    public void hideControls() {
        if (directionalControls != null) {
            directionalControls.setVisibility(View.GONE);
        }
        if (toggleButton != null) {
            toggleButton.setText("Adjust\nSkeleton");
        }
        isControlsVisible = false;
        
        // Show toast message
        Toast.makeText(context, "Skeleton adjustment controls disabled", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "Skeleton controls hidden");
    }
    
    private void setupDirectionalButtons() {
        // These will be set up when the controls are created
        // The actual button references will come from the SkeletonAdjustmentControls
    }
    
    public void moveUp() {
        SkeletonPositionAdjuster.moveUp(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        updateStatusText();
        Log.d(TAG, "Moved skeleton up");
    }
    
    public void moveDown() {
        SkeletonPositionAdjuster.moveDown(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        updateStatusText();
        Log.d(TAG, "Moved skeleton down");
    }
    
    public void moveLeft() {
        SkeletonPositionAdjuster.moveLeft(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        updateStatusText();
        Log.d(TAG, "Moved skeleton left");
    }
    
    public void moveRight() {
        SkeletonPositionAdjuster.moveRight(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        updateStatusText();
        Log.d(TAG, "Moved skeleton right");
    }
    
    public void resetToDefault() {
        SkeletonPositionAdjuster.resetToDefault();
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        updateStatusText();
        Log.d(TAG, "Reset skeleton to default");
    }
    
    private void updateStatusText() {
        if (statusText != null) {
            SkeletonPositionAdjuster.SkeletonAdjustment adjustment = SkeletonPositionAdjuster.getCurrentAdjustment();
            String status = String.format("Offset: (%.1f, %.1f) | Scale: (%.2f, %.2f)",
                adjustment.offsetX, adjustment.offsetY, adjustment.scaleX, adjustment.scaleY);
            statusText.setText(status);
        }
    }
    
    public String getCurrentAdjustmentDetails() {
        SkeletonPositionAdjuster.SkeletonAdjustment adjustment = SkeletonPositionAdjuster.getCurrentAdjustment();
        if (adjustment != null) {
            return String.format("Offset: (%.1f, %.1f) | Scale: (%.2f, %.2f)",
                adjustment.offsetX, adjustment.offsetY, adjustment.scaleX, adjustment.scaleY);
        }
        return "No adjustment available";
    }
    
    public boolean isControlsVisible() {
        return isControlsVisible;
    }
    
    public SkeletonAdjustmentControls getControls() {
        return controls;
    }
}
