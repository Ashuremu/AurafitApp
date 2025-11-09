package com.aurafit.AuraFitApp.ui.ar;

import android.content.Context;
import android.util.Log;
import com.aurafit.AuraFitApp.data.model.OutfitItem;

public class ArPositioningCalibrator {
    
    private static final String TAG = "ArPositioningCalibrator";
    
    public static void calibrateItem(OutfitItem item, float deltaX, float deltaY, float deltaZ, 
                                   float deltaScale, float deltaRotX, float deltaRotY, float deltaRotZ) {
        
        String category = item.getCategory().toLowerCase();
        String itemName = item.getName();
        
        // Get current positioning
        ArPositioningConfig.PositioningData currentData = ArPositioningConfig.getPositioningData(category, itemName);
        
        // Apply adjustments
        float newX = currentData.x + deltaX;
        float newY = currentData.y + deltaY;
        float newZ = currentData.z + deltaZ;
        float newScale = Math.max(0.1f, currentData.scale + deltaScale); // Prevent negative scale
        float newRotX = currentData.rotationX + deltaRotX;
        float newRotY = currentData.rotationY + deltaRotY;
        float newRotZ = currentData.rotationZ + deltaRotZ;
        
        // Create new positioning data with current sizing
        ArPositioningConfig.PositioningData newData = new ArPositioningConfig.PositioningData(
            newX, newY, newZ, newScale, newRotX, newRotY, newRotZ,
            currentData.widthScale, currentData.heightScale, currentData.depthScale
        );
        
        // Update the positioning
        ArPositioningConfig.updatePositioningData(category, itemName, newData);
        
        Log.d(TAG, "Calibrated " + itemName + ": " + newData.toString());
    }
    
    public static void calibrateSizing(OutfitItem item, float deltaWidth, float deltaHeight, float deltaDepth) {
        String category = item.getCategory().toLowerCase();
        String itemName = item.getName();
        
        // Get current positioning
        ArPositioningConfig.PositioningData currentData = ArPositioningConfig.getPositioningData(category, itemName);
        
        // Apply sizing adjustments
        float newWidth = Math.max(0.1f, currentData.widthScale + deltaWidth);
        float newHeight = Math.max(0.1f, currentData.heightScale + deltaHeight);
        float newDepth = Math.max(0.1f, currentData.depthScale + deltaDepth);
        
        // Create new positioning data with updated sizing
        ArPositioningConfig.PositioningData newData = new ArPositioningConfig.PositioningData(
            currentData.x, currentData.y, currentData.z, currentData.scale,
            currentData.rotationX, currentData.rotationY, currentData.rotationZ,
            newWidth, newHeight, newDepth
        );
        
        // Update the positioning
        ArPositioningConfig.updatePositioningData(category, itemName, newData);
        
        Log.d(TAG, "Calibrated sizing for " + itemName + ": " + newData.toString());
    }
    
    public static void calibrateBagPosition(OutfitItem bagItem, float handOffsetX, float handOffsetY, float handOffsetZ) {
        calibrateItem(bagItem, handOffsetX, handOffsetY, handOffsetZ, 0.0f, 0.0f, 0.0f, 0.0f);
    }
    
    public static void calibrateHatPosition(OutfitItem hatItem, float headOffsetX, float headOffsetY, float headOffsetZ) {
        calibrateItem(hatItem, headOffsetX, headOffsetY, headOffsetZ, 0.0f, 0.0f, 0.0f, 0.0f);
    }
    
    public static void calibrateGlassesPosition(OutfitItem glassesItem, float faceOffsetX, float faceOffsetY, float faceOffsetZ) {
        calibrateItem(glassesItem, faceOffsetX, faceOffsetY, faceOffsetZ, 0.0f, 0.0f, 0.0f, 0.0f);
    }
    
    public static void resetItemPositioning(OutfitItem item) {
        String category = item.getCategory().toLowerCase();
        String itemName = item.getName();
        
        // Get default positioning
        ArPositioningConfig.PositioningData defaultData = ArPositioningConfig.getDefaultPositioning(category);
        
        // Update with default values
        ArPositioningConfig.updatePositioningData(category, itemName, defaultData);
        
        Log.d(TAG, "Reset positioning for " + itemName + " to default");
    }
    
    public static ArPositioningConfig.PositioningData getCurrentPositioning(OutfitItem item) {
        String category = item.getCategory().toLowerCase();
        String itemName = item.getName();
        return ArPositioningConfig.getPositioningData(category, itemName);
    }
    
    public static void logCurrentPositioning(OutfitItem item) {
        ArPositioningConfig.PositioningData data = getCurrentPositioning(item);
        Log.d(TAG, "Current positioning for " + item.getName() + ": " + data.toString());
    }
    
    public static void calibrateMultipleItems(OutfitItem[] items, float[] deltaX, float[] deltaY, float[] deltaZ) {
        if (items.length != deltaX.length || items.length != deltaY.length || items.length != deltaZ.length) {
            Log.e(TAG, "Array lengths must match for batch calibration");
            return;
        }
        
        for (int i = 0; i < items.length; i++) {
            calibrateItem(items[i], deltaX[i], deltaY[i], deltaZ[i], 0.0f, 0.0f, 0.0f, 0.0f);
        }
    }
}
