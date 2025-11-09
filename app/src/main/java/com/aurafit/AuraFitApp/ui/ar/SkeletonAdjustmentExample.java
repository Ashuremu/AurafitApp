package com.aurafit.AuraFitApp.ui.ar;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SkeletonAdjustmentExample {
    
    private static final String TAG = "SkeletonAdjustmentExample";
    
    private Context context;
    private ArOverlayView overlayView;
    private SkeletonAdjustmentControls controls;
    
    public SkeletonAdjustmentExample(Context context, ArOverlayView overlayView) {
        this.context = context;
        this.overlayView = overlayView;
        this.controls = new SkeletonAdjustmentControls(context, overlayView);
    }

    public void initializeSkeletonAdjustment() {
        // Enable skeleton adjustment in the overlay view
        overlayView.enableSkeletonAdjustment();
        
        // Apply the current adjustment
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        
        Log.d(TAG, "Skeleton adjustment system initialized");
        Log.d(TAG, "Current adjustment: " + SkeletonPositionAdjuster.getAdjustmentSummary());
    }
    
    public void addControlsToLayout(ViewGroup parentLayout) {
        if (controls != null && parentLayout != null) {
            LinearLayout controlsLayout = controls.getControlsLayout();
            parentLayout.addView(controlsLayout);
            Log.d(TAG, "Added skeleton adjustment controls to layout");
        }
    }
    
    public void toggleControls() {
        if (controls != null) {
            controls.toggleVisibility();
            Log.d(TAG, "Toggled skeleton adjustment controls visibility");
        }
    }
    
    public void moveUp() {
        SkeletonPositionAdjuster.moveUp(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        Log.d(TAG, "Moved skeleton up");
    }
    
    public void moveDown() {
        SkeletonPositionAdjuster.moveDown(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        Log.d(TAG, "Moved skeleton down");
    }
    
    public void moveLeft() {
        SkeletonPositionAdjuster.moveLeft(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        Log.d(TAG, "Moved skeleton left");
    }
    
    public void moveRight() {
        SkeletonPositionAdjuster.moveRight(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        Log.d(TAG, "Moved skeleton right");
    }
    
    public void resetToDefault() {
        SkeletonPositionAdjuster.resetToDefault();
        SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
        Log.d(TAG, "Reset to default adjustment");
    }
    
    public String getCurrentAdjustmentDetails() {
        SkeletonPositionAdjuster.SkeletonAdjustment adjustment = SkeletonPositionAdjuster.getCurrentAdjustment();
        if (adjustment != null) {
            return String.format("Offset: (%.1f, %.1f) | Scale: (%.2f, %.2f)",
                adjustment.offsetX, adjustment.offsetY, adjustment.scaleX, adjustment.scaleY);
        }
        return "No adjustment available";
    }
}
