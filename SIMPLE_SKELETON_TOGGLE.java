// Add this code directly to your GenerateActivity.java

// 1. Add these fields to your GenerateActivity class:
private LinearLayout skeletonContainer;
private LinearLayout directionalControls;
private Button toggleSkeletonButton;

// 2. Add this to your onCreate() method after setContentView():
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_generate);
    
    // ... your existing code ...
    
    // Setup skeleton toggle
    setupSkeletonToggle();
}

// 3. Add this method to your GenerateActivity class:
private void setupSkeletonToggle() {
    // Get UI references
    toggleSkeletonButton = findViewById(R.id.toggleSkeletonAdjustmentButton);
    skeletonContainer = findViewById(R.id.skeletonAdjustmentContainer);
    directionalControls = findViewById(R.id.skeletonDirectionalControls);
    
    // Setup toggle button click listener
    if (toggleSkeletonButton != null) {
        toggleSkeletonButton.setOnClickListener(v -> {
            if (directionalControls != null) {
                if (directionalControls.getVisibility() == View.GONE) {
                    // Show controls
                    directionalControls.setVisibility(View.VISIBLE);
                    toggleSkeletonButton.setText("Hide\nControls");
                    Toast.makeText(this, "Skeleton adjustment controls enabled", Toast.LENGTH_SHORT).show();
                } else {
                    // Hide controls
                    directionalControls.setVisibility(View.GONE);
                    toggleSkeletonButton.setText("Adjust\nSkeleton");
                    Toast.makeText(this, "Skeleton adjustment controls disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    // Setup directional button listeners
    Button upButton = findViewById(R.id.skeletonUpButton);
    Button downButton = findViewById(R.id.skeletonDownButton);
    Button leftButton = findViewById(R.id.skeletonLeftButton);
    Button rightButton = findViewById(R.id.skeletonRightButton);
    Button resetButton = findViewById(R.id.skeletonResetButton);
    
    if (upButton != null) upButton.setOnClickListener(v -> {
        SkeletonPositionAdjuster.moveUp(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(arOverlayView);
        Toast.makeText(this, "Moved skeleton up", Toast.LENGTH_SHORT).show();
    });
    
    if (downButton != null) downButton.setOnClickListener(v -> {
        SkeletonPositionAdjuster.moveDown(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(arOverlayView);
        Toast.makeText(this, "Moved skeleton down", Toast.LENGTH_SHORT).show();
    });
    
    if (leftButton != null) leftButton.setOnClickListener(v -> {
        SkeletonPositionAdjuster.moveLeft(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(arOverlayView);
        Toast.makeText(this, "Moved skeleton left", Toast.LENGTH_SHORT).show();
    });
    
    if (rightButton != null) rightButton.setOnClickListener(v -> {
        SkeletonPositionAdjuster.moveRight(10f);
        SkeletonPositionAdjuster.applySkeletonAdjustment(arOverlayView);
        Toast.makeText(this, "Moved skeleton right", Toast.LENGTH_SHORT).show();
    });
    
    if (resetButton != null) resetButton.setOnClickListener(v -> {
        SkeletonPositionAdjuster.resetToDefault();
        SkeletonPositionAdjuster.applySkeletonAdjustment(arOverlayView);
        Toast.makeText(this, "Reset skeleton to default", Toast.LENGTH_SHORT).show();
    });
}

// 4. Make sure you have these imports at the top:
import com.aurafit.AuraFitApp.ui.ar.SkeletonPositionAdjuster;
import android.widget.Toast;
import android.view.View;
