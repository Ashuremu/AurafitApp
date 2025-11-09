package com.aurafit.AuraFitApp.ui.ar;

import android.content.Context;
import android.util.Log;

public class SkeletonPositionAdjuster {
    
    private static final String TAG = "SkeletonPositionAdjuster";
    
    // Simple skeleton adjustment parameters
    public static class SkeletonAdjustment {
        public float offsetX;
        public float offsetY;
        public float scaleX;
        public float scaleY;
        
        public SkeletonAdjustment(float offsetX, float offsetY, float scaleX, float scaleY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
        }
        
        @Override
        public String toString() {
            return String.format("SkeletonAdjustment(offsetX=%.2f, offsetY=%.2f, scaleX=%.2f, scaleY=%.2f)", 
                               offsetX, offsetY, scaleX, scaleY);
        }
    }
    
    private static SkeletonAdjustment currentAdjustment;
    private static Context context;
    
    public static void initialize(Context appContext) {
        context = appContext;
        // Initialize with default values
        currentAdjustment = new SkeletonAdjustment(-220f, 15f, 3.5f, 3.2f);
        Log.d(TAG, "Skeleton adjuster initialized with default values");
    }

    public static SkeletonAdjustment getCurrentAdjustment() {
        if (currentAdjustment == null) {
            currentAdjustment = new SkeletonAdjustment(-220f, 15f, 3.5f, 3.2f);
        }
        return currentAdjustment;
    }
    
    public static void applySkeletonAdjustment(ArOverlayView overlayView) {
        if (overlayView == null || currentAdjustment == null) {
            Log.w(TAG, "Cannot apply adjustment - overlayView or currentAdjustment is null");
            return;
        }
        
        // Apply basic adjustment parameters
        overlayView.adjustAlignment(
            currentAdjustment.offsetX,
            currentAdjustment.offsetY,
            currentAdjustment.scaleX,
            currentAdjustment.scaleY
        );
        
        Log.d(TAG, "Applied skeleton adjustment: " + currentAdjustment.toString());
    }
    
    public static void setCustomAdjustment(float offsetX, float offsetY, float scaleX, float scaleY) {
        currentAdjustment = new SkeletonAdjustment(offsetX, offsetY, scaleX, scaleY);
        Log.d(TAG, "Set custom skeleton adjustment: " + currentAdjustment.toString());
    }
    
    public static void moveUp(float amount) {
        if (currentAdjustment == null) {
            currentAdjustment = new SkeletonAdjustment(-220f, 15f, 3.5f, 3.2f);
        }
        currentAdjustment.offsetY -= amount;
        Log.d(TAG, "Moved skeleton up by " + amount);
    }
    
    public static void moveDown(float amount) {
        if (currentAdjustment == null) {
            currentAdjustment = new SkeletonAdjustment(-220f, 15f, 3.5f, 3.2f);
        }
        currentAdjustment.offsetY += amount;
        Log.d(TAG, "Moved skeleton down by " + amount);
    }
    
    public static void moveLeft(float amount) {
        if (currentAdjustment == null) {
            currentAdjustment = new SkeletonAdjustment(-220f, 15f, 3.5f, 3.2f);
        }
        currentAdjustment.offsetX -= amount;
        Log.d(TAG, "Moved skeleton left by " + amount);
    }
    
    public static void moveRight(float amount) {
        if (currentAdjustment == null) {
            currentAdjustment = new SkeletonAdjustment(-220f, 15f, 3.5f, 3.2f);
        }
        currentAdjustment.offsetX += amount;
        Log.d(TAG, "Moved skeleton right by " + amount);
    }
    
    public static void resetToDefault() {
        currentAdjustment = new SkeletonAdjustment(-220f, 15f, 3.5f, 3.2f);
        Log.d(TAG, "Reset to default adjustment");
    }
    
    public static String getAdjustmentSummary() {
        if (currentAdjustment == null) {
            return "No adjustment set";
        }
        
        return String.format("Current Adjustment:\n" +
                "Offset: X=%.2f, Y=%.2f\n" +
                "Scale: X=%.2f, Y=%.2f",
                currentAdjustment.offsetX, currentAdjustment.offsetY,
                currentAdjustment.scaleX, currentAdjustment.scaleY);
    }
}
