// Add this method to your GenerateActivity to test skeleton adjustment

private void testSkeletonAdjustment() {
    // Test if skeleton adjustment is working
    if (arOverlayView != null) {
        // Enable skeleton adjustment
        arOverlayView.enableSkeletonAdjustment();
        
        // Test movement
        SkeletonPositionAdjuster.moveUp(20f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(arOverlayView);
        
        // Log current adjustment
        String currentAdjustment = SkeletonPositionAdjuster.getAdjustmentSummary();
        Log.d("SkeletonTest", "Current adjustment: " + currentAdjustment);
        
        // Force redraw
        arOverlayView.invalidate();
    }
}

// Call this method in onCreate() after setupSkeletonControls() to test:
// testSkeletonAdjustment();
