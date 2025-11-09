# Simple Skeleton Position Adjustment System

## Overview

The Simple Skeleton Position Adjustment System provides basic directional controls for adjusting AR skeleton positioning. This system offers simple up, down, left, right controls for fine-tuning skeleton positioning.

## Features

- **Simple Directional Controls**: Basic up, down, left, right movement controls
- **Real-time Adjustment**: Interactive UI controls for live skeleton position adjustment
- **Reset Functionality**: Easy reset to default positioning
- **Status Display**: Real-time display of current position values

## Components

### 1. SkeletonPositionAdjuster
Simple class that handles basic skeleton position adjustments.

**Key Methods:**
- `initialize(Context)`: Initialize the adjuster with app context
- `moveUp(float)`: Move skeleton up by specified amount
- `moveDown(float)`: Move skeleton down by specified amount
- `moveLeft(float)`: Move skeleton left by specified amount
- `moveRight(float)`: Move skeleton right by specified amount
- `resetToDefault()`: Reset to default positioning
- `applySkeletonAdjustment(ArOverlayView)`: Apply current adjustment to overlay view

### 2. SkeletonAdjustmentControls
Simple UI controls for skeleton position adjustment.

**Key Methods:**
- `SkeletonAdjustmentControls(Context, ArOverlayView)`: Create controls instance
- `getControlsLayout()`: Get the UI layout for controls
- `toggleVisibility()`: Show/hide controls

### 3. ArOverlayView Integration
Enhanced overlay view with skeleton adjustment support.

**New Methods:**
- `enableSkeletonAdjustment()`: Enable skeleton position adjustment
- `disableSkeletonAdjustment()`: Disable skeleton position adjustment
- `applySkeletonAdjustment()`: Apply current skeleton adjustment

## Usage Examples

### Basic Setup

```java
// Initialize the system
SkeletonAdjustmentExample example = new SkeletonAdjustmentExample(context, overlayView);
example.initializeSkeletonAdjustment();

// Add controls to your layout
example.addControlsToLayout(parentLayout);
```

### Directional Controls

```java
// Move skeleton in different directions
example.moveUp();     // Move skeleton up
example.moveDown();   // Move skeleton down
example.moveLeft();   // Move skeleton left
example.moveRight();  // Move skeleton right

// Reset to default
example.resetToDefault();
```

### Get Current Status

```java
// Get current adjustment details
String details = example.getCurrentAdjustmentDetails();
Log.d("SkeletonAdjustment", details);
```

## Adjustment Parameters

### Core Parameters
- **offsetX/offsetY**: Horizontal and vertical position offsets
- **scaleX/scaleY**: Horizontal and vertical scaling factors

## UI Controls

The system provides simple controls for:

1. **Directional Buttons**: Up, Down, Left, Right buttons
2. **Reset Button**: Reset to default positioning
3. **Status Display**: Real-time display of current position values

## Integration with Existing Code

### ArOverlayView Integration
The skeleton adjustment system integrates seamlessly with the existing `ArOverlayView`:

```java
// Enable skeleton adjustment
overlayView.enableSkeletonAdjustment();

// Apply adjustments automatically
SkeletonPositionAdjuster.applySkeletonAdjustment(overlayView);
```

### Automatic Application
The system automatically applies adjustments in the `onDraw` method when enabled:

```java
@Override
protected void onDraw(Canvas canvas) {
    // Apply skeleton adjustment if enabled
    if (isSkeletonAdjustmentEnabled) {
        applySkeletonAdjustment();
    }
    // ... rest of drawing code
}
```

## Troubleshooting

### Common Issues

1. **Skeleton not aligning properly**
   - Check if skeleton adjustment is enabled
   - Use directional controls for adjustments

2. **Controls not appearing**
   - Ensure controls are added to parent layout
   - Check if controls are set to visible
   - Verify context is properly initialized

### Debug Information

Enable debug logging to see detailed information:

```java
// Get current adjustment summary
String summary = SkeletonPositionAdjuster.getAdjustmentSummary();
Log.d("SkeletonAdjustment", summary);
```

## Best Practices

1. **Initialize Early**: Initialize the system as early as possible in your app lifecycle
2. **Test on Multiple Devices**: Test skeleton adjustments on various device types
3. **Use Small Movements**: Use small incremental movements for fine adjustments
4. **Reset When Needed**: Use the reset function to return to default positioning

## Support

For issues or questions about the skeleton position adjustment system, refer to the debug logs for troubleshooting information.
