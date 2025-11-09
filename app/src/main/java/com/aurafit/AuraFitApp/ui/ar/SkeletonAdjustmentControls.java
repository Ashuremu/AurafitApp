package com.aurafit.AuraFitApp.ui.ar;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SkeletonAdjustmentControls {
    
    private static final String TAG = "SkeletonAdjustmentControls";
    
    private Context context;
    private ArOverlayView overlayView;
    private LinearLayout controlsLayout;
    private SkeletonPositionAdjuster.SkeletonAdjustment currentAdjustment;
    
    // UI Components
    private Button upButton, downButton, leftButton, rightButton;
    private Button resetButton;
    private TextView statusText;
    
    // Movement amount
    private static final float MOVE_AMOUNT = 10f;
    
    public SkeletonAdjustmentControls(Context context, ArOverlayView overlayView) {
        this.context = context;
        this.overlayView = overlayView;
        this.currentAdjustment = SkeletonPositionAdjuster.getCurrentAdjustment();
        createControlsLayout();
    }

    private void createControlsLayout() {
        controlsLayout = new LinearLayout(context);
        controlsLayout.setOrientation(LinearLayout.VERTICAL);
        controlsLayout.setPadding(20, 20, 20, 20);
        controlsLayout.setBackgroundColor(Color.parseColor("#80000000")); // Semi-transparent black
        
        // Title
        TextView titleText = new TextView(context);
        titleText.setText("Skeleton Position Adjustment");
        titleText.setTextColor(Color.WHITE);
        titleText.setTextSize(18);
        titleText.setGravity(Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 20);
        controlsLayout.addView(titleText);
        
        // Directional controls
        createDirectionalControls();
        
        // Reset button
        createResetButton();
        
        // Status text
        createStatusText();
        
        // Initialize with current values
        updateStatusText();
    }

    private void createDirectionalControls() {
        // Up button
        upButton = new Button(context);
        upButton.setText("↑ UP");
        upButton.setTextColor(Color.WHITE);
        upButton.setBackgroundColor(Color.parseColor("#4ECDC4"));
        upButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        upButton.setPadding(0, 10, 0, 10);
        upButton.setOnClickListener(v -> moveUp());
        controlsLayout.addView(upButton);
        
        // Left and Right buttons in horizontal layout
        LinearLayout horizontalLayout = new LinearLayout(context);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setPadding(0, 5, 0, 5);
        
        leftButton = new Button(context);
        leftButton.setText("← LEFT");
        leftButton.setTextColor(Color.WHITE);
        leftButton.setBackgroundColor(Color.parseColor("#45B7D1"));
        leftButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        leftButton.setPadding(10, 10, 10, 10);
        leftButton.setOnClickListener(v -> moveLeft());
        horizontalLayout.addView(leftButton);
        
        rightButton = new Button(context);
        rightButton.setText("RIGHT →");
        rightButton.setTextColor(Color.WHITE);
        rightButton.setBackgroundColor(Color.parseColor("#45B7D1"));
        rightButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        rightButton.setPadding(10, 10, 10, 10);
        rightButton.setOnClickListener(v -> moveRight());
        horizontalLayout.addView(rightButton);
        
        controlsLayout.addView(horizontalLayout);
        
        // Down button
        downButton = new Button(context);
        downButton.setText("↓ DOWN");
        downButton.setTextColor(Color.WHITE);
        downButton.setBackgroundColor(Color.parseColor("#4ECDC4"));
        downButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        downButton.setPadding(0, 10, 0, 10);
        downButton.setOnClickListener(v -> moveDown());
        controlsLayout.addView(downButton);
    }

    private void createResetButton() {
        resetButton = new Button(context);
        resetButton.setText("RESET");
        resetButton.setTextColor(Color.WHITE);
        resetButton.setBackgroundColor(Color.parseColor("#FF6B6B"));
        resetButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        resetButton.setPadding(0, 15, 0, 15);
        resetButton.setOnClickListener(v -> resetToDefault());
        controlsLayout.addView(resetButton);
    }

    private void moveUp() {
        SkeletonPositionAdjuster.moveUp(MOVE_AMOUNT);
        applyAdjustment();
        updateStatusText();
        Toast.makeText(context, "Moved skeleton up", Toast.LENGTH_SHORT).show();
    }

    private void moveDown() {
        SkeletonPositionAdjuster.moveDown(MOVE_AMOUNT);
        applyAdjustment();
        updateStatusText();
        Toast.makeText(context, "Moved skeleton down", Toast.LENGTH_SHORT).show();
    }

    private void moveLeft() {
        SkeletonPositionAdjuster.moveLeft(MOVE_AMOUNT);
        applyAdjustment();
        updateStatusText();
        Toast.makeText(context, "Moved skeleton left", Toast.LENGTH_SHORT).show();
    }

    private void moveRight() {
        SkeletonPositionAdjuster.moveRight(MOVE_AMOUNT);
        applyAdjustment();
        updateStatusText();
        Toast.makeText(context, "Moved skeleton right", Toast.LENGTH_SHORT).show();
    }

    private void createStatusText() {
        statusText = new TextView(context);
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(12);
        statusText.setPadding(0, 10, 0, 0);
        updateStatusText();
        controlsLayout.addView(statusText);
    }

    private void applyAdjustment() {
        if (overlayView != null) {
            SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
            updateStatusText();
        }
    }

    private void resetToDefault() {
        SkeletonPositionAdjuster.resetToDefault();
        applyAdjustment();
        updateStatusText();
        Toast.makeText(context, "Reset to default adjustment", Toast.LENGTH_SHORT).show();
    }

    private void updateStatusText() {
        if (statusText != null) {
            currentAdjustment = SkeletonPositionAdjuster.getCurrentAdjustment();
            String status = String.format("Offset: (%.1f, %.1f) | Scale: (%.2f, %.2f)",
                currentAdjustment.offsetX, currentAdjustment.offsetY,
                currentAdjustment.scaleX, currentAdjustment.scaleY);
            statusText.setText(status);
        }
    }

    public LinearLayout getControlsLayout() {
        return controlsLayout;
    }

    public void setVisible(boolean visible) {
        if (controlsLayout != null) {
            controlsLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void toggleVisibility() {
        if (controlsLayout != null) {
            boolean isVisible = controlsLayout.getVisibility() == View.VISIBLE;
            setVisible(!isVisible);
        }
    }
}
