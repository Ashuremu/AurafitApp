# Skeleton Adjustment Integration Guide

## Quick Integration Steps

### 1. Add to Your GenerateActivity

```java
public class GenerateActivity extends AppCompatActivity {
    private ArOverlayView arOverlayView;
    private SkeletonAdjustmentHelper skeletonHelper;
    
    // UI References
    private Button toggleSkeletonButton;
    private LinearLayout skeletonContainer;
    private LinearLayout directionalControls;
    private TextView skeletonStatusText;
    private Button upButton, downButton, leftButton, rightButton, resetButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        
        // Initialize AR overlay view
        arOverlayView = findViewById(R.id.arOverlayView);
        
        // Initialize skeleton adjustment helper
        skeletonHelper = new SkeletonAdjustmentHelper(this, arOverlayView);
        skeletonHelper.initialize();
        
        // Setup skeleton controls
        setupSkeletonControls();
    }
    
    private void setupSkeletonControls() {
        // Get UI references
        toggleSkeletonButton = findViewById(R.id.toggleSkeletonAdjustmentButton);
        skeletonContainer = findViewById(R.id.skeletonAdjustmentContainer);
        directionalControls = findViewById(R.id.skeletonDirectionalControls);
        skeletonStatusText = findViewById(R.id.skeletonStatusText);
        
        upButton = findViewById(R.id.skeletonUpButton);
        downButton = findViewById(R.id.skeletonDownButton);
        leftButton = findViewById(R.id.skeletonLeftButton);
        rightButton = findViewById(R.id.skeletonRightButton);
        resetButton = findViewById(R.id.skeletonResetButton);
        
        // Setup the helper
        skeletonHelper.setupControls(
            toggleSkeletonButton,
            skeletonContainer,
            directionalControls,
            skeletonStatusText
        );
        
        // Setup directional button listeners
        upButton.setOnClickListener(v -> skeletonHelper.moveUp());
        downButton.setOnClickListener(v -> skeletonHelper.moveDown());
        leftButton.setOnClickListener(v -> skeletonHelper.moveLeft());
        rightButton.setOnClickListener(v -> skeletonHelper.moveRight());
        resetButton.setOnClickListener(v -> skeletonHelper.resetToDefault());
    }
}
```

### 2. Layout Structure

The layout now includes:

```xml
<!-- Toggle button in camera controls (3rd column) -->
<Button
    android:id="@+id/toggleSkeletonAdjustmentButton"
    android:text="Adjust\nSkeleton" />

<!-- Skeleton adjustment container (left side) -->
<LinearLayout
    android:id="@+id/skeletonAdjustmentContainer"
    android:visibility="gone">
    
    <!-- Directional controls -->
    <LinearLayout
        android:id="@+id/skeletonDirectionalControls"
        android:visibility="gone">
        
        <Button android:id="@+id/skeletonUpButton" android:text="↑ UP" />
        <Button android:id="@+id/skeletonLeftButton" android:text="← LEFT" />
        <Button android:id="@+id/skeletonRightButton" android:text="RIGHT →" />
        <Button android:id="@+id/skeletonDownButton" android:text="↓ DOWN" />
        <Button android:id="@+id/skeletonResetButton" android:text="RESET" />
        
        <TextView android:id="@+id/skeletonStatusText" />
    </LinearLayout>
</LinearLayout>
```

### 3. How It Works

1. **User taps "Adjust Skeleton"** in bottom camera controls
2. **Skeleton container appears** on left side of screen
3. **Directional controls are shown** with up/down/left/right buttons
4. **User can adjust skeleton** by tapping directional buttons
5. **Status text updates** showing current offset and scale values
6. **User taps "Hide Controls"** to hide the controls

### 4. Features

- ✅ **Toggle visibility** - Show/hide controls with button
- ✅ **Directional movement** - Up, down, left, right buttons
- ✅ **Reset functionality** - Return to default position
- ✅ **Real-time feedback** - Status text shows current values
- ✅ **Smooth integration** - Works with existing AR overlay

### 5. Customization

You can customize the movement amount by modifying the helper:

```java
// In SkeletonAdjustmentHelper.java, change the movement amount
public void moveUp() {
    SkeletonPositionAdjuster.moveUp(15f); // Changed from 10f to 15f
    // ... rest of method
}
```

### 6. Debug Information

Enable debug logging to see skeleton adjustment activity:

```java
// Add to your activity
Log.d("SkeletonAdjustment", skeletonHelper.getCurrentAdjustmentDetails());
```

## Troubleshooting

### Controls not appearing?
- Check that all UI references are found (no null values)
- Verify the layout IDs match exactly
- Ensure the skeleton container is initially hidden

### Buttons not working?
- Check that click listeners are properly set
- Verify the SkeletonAdjustmentHelper is initialized
- Check that the ArOverlayView is properly set up

### Skeleton not moving?
- Ensure skeleton adjustment is enabled in ArOverlayView
- Check that the overlay view is properly initialized
- Verify the body landmarks are being detected
