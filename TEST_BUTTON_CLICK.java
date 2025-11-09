// Add this simple test to your GenerateActivity to verify the button is working

private void testButtonClick() {
    Button toggleButton = findViewById(R.id.toggleSkeletonAdjustmentButton);
    if (toggleButton != null) {
        toggleButton.setOnClickListener(v -> {
            Toast.makeText(this, "Button clicked!", Toast.LENGTH_SHORT).show();
            Log.d("SkeletonTest", "Toggle button clicked");
        });
    } else {
        Log.e("SkeletonTest", "Toggle button not found!");
    }
}

// Call this in onCreate() to test:
// testButtonClick();

