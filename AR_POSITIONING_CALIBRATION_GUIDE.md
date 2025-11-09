# AR Positioning Calibration Guide

## Overview
This guide shows you exactly where and how to calibrate the position for each clothing item in the AR system.

## 🎯 **Where to Calibrate Positions**

### **1. Main Configuration File**
**File**: `aurafit-app/app/src/main/java/com/aurafit/AuraFitApp/ui/ar/ArPositioningConfig.java`

This is the **primary location** where you can calibrate positions for each clothing item.

### **2. Calibration Methods**
**File**: `aurafit-app/app/src/main/java/com/aurafit/AuraFitApp/ui/ar/ArPositioningCalibrator.java`

This provides runtime calibration methods.

### **3. AR Overlay View**
**File**: `aurafit-app/app/src/main/java/com/aurafit/AuraFitApp/ui/ar/ArOverlayView.java`

Contains the main rendering logic and alignment variables.

## 🔧 **How to Calibrate Positions**

### **Method 1: Static Configuration (Recommended)**

Edit the `ArPositioningConfig.java` file and modify the positioning data:

```java
// Example: Adjust "Lacoste white polo" positioning
topPositioning.put("Lacoste white polo", new PositioningData(
    0.0f,    // X position (left/right)
    -0.2f,   // Y position (up/down) 
    0.0f,    // Z position (forward/back)
    1.0f,    // Scale (size)
    0.0f,    // Rotation X
    0.0f,    // Rotation Y
    0.0f     // Rotation Z
));
```

### **Method 2: Runtime Calibration**

Use the `ArPositioningCalibrator` class for dynamic adjustments:

```java
// Example: Move a shirt up by 0.1 units
ArPositioningCalibrator.calibrateItem(shirtItem, 0.0f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

// Example: Adjust bag position on hand
ArPositioningCalibrator.calibrateBagPosition(bagItem, 0.1f, -0.05f, 0.1f);

// Example: Adjust hat position on head
ArPositioningCalibrator.calibrateHatPosition(hatItem, 0.0f, -0.05f, 0.0f);
```

## 📍 **Positioning Categories**

### **1. Top Clothing (Shirts, Blouses, Sweaters)**
- **Default Position**: `(0.0, -0.2, 0.0)`
- **Scale**: `1.0`
- **Calibration**: Adjust Y for height, X for left/right alignment

### **2. Bottom Clothing (Pants, Shorts, Skirts)**
- **Default Position**: `(0.0, 0.1, 0.0)`
- **Scale**: `1.0`
- **Calibration**: Adjust Y for waist height, X for hip alignment

### **3. Shoes**
- **Default Position**: `(0.0, 0.4, 0.0)`
- **Scale**: `0.8`
- **Calibration**: Adjust Y for foot height, X for left/right foot alignment

### **4. Accessories**

#### **Bags (Hand Positioning)**
- **Default Position**: `(0.2, -0.1, 0.1)`
- **Scale**: `0.5`
- **Calibration**: Adjust X/Y/Z for hand placement

#### **Hats (Head Positioning)**
- **Default Position**: `(0.0, -0.3, 0.0)`
- **Scale**: `0.6`
- **Calibration**: Adjust Y for head height, X for centering

#### **Glasses (Face Positioning)**
- **Default Position**: `(0.0, -0.2, 0.0)`
- **Scale**: `0.4`
- **Calibration**: Adjust Y for eye level, X for centering

## 🎮 **Calibration Values Guide**

### **Position Values**
- **X (Left/Right)**: 
  - `-0.5` = Far left
  - `0.0` = Center
  - `0.5` = Far right

- **Y (Up/Down)**:
  - `-0.5` = High up
  - `0.0` = Center
  - `0.5` = Low down

- **Z (Forward/Back)**:
  - `-0.5` = Behind body
  - `0.0` = On body surface
  - `0.5` = In front of body

### **Scale Values**
- `0.1` = Very small
- `0.5` = Half size
- `1.0` = Normal size
- `2.0` = Double size

### **Rotation Values (in degrees)**
- `0.0` = No rotation
- `90.0` = 90 degrees
- `180.0` = 180 degrees
- `-90.0` = -90 degrees

## 🔍 **Debugging and Testing**

### **1. Enable Debug Logging**
```java
// In your activity
ArPositioningCalibrator.logCurrentPositioning(item);
```

### **2. Test Different Values**
```java
// Test small adjustments
ArPositioningCalibrator.calibrateItem(item, 0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
```

### **3. Reset to Default**
```java
// Reset item to default positioning
ArPositioningCalibrator.resetItemPositioning(item);
```

## 📝 **Step-by-Step Calibration Process**

1. **Identify the item** you want to calibrate
2. **Open** `ArPositioningConfig.java`
3. **Find the item** in the appropriate category (top, bottom, shoes, accessories)
4. **Modify the PositioningData** values:
   - Adjust X, Y, Z for position
   - Adjust scale for size
   - Adjust rotation values if needed
5. **Test the changes** in the app
6. **Fine-tune** using `ArPositioningCalibrator` if needed

## 🎯 **Common Calibration Scenarios**

### **Shirt Too High/Low**
```java
// Move shirt down by 0.1 units
topPositioning.put("Lacoste white polo", new PositioningData(0.0f, -0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f));
```

### **Bag Not on Hand**
```java
// Adjust bag position for better hand placement
accessoriesPositioning.put("Sling Bag", new PositioningData(0.3f, -0.05f, 0.15f, 0.5f, 0.0f, 0.0f, 0.0f));
```

### **Hat Too Small**
```java
// Increase hat scale
accessoriesPositioning.put("Brown Cap", new PositioningData(0.0f, -0.3f, 0.0f, 0.8f, 0.0f, 0.0f, 0.0f));
```

### **Shoes Not Aligned**
```java
// Adjust shoe position
shoesPositioning.put("Adidas Rubber Shoes", new PositioningData(0.1f, 0.4f, 0.0f, 0.8f, 0.0f, 0.0f, 0.0f));
```

## 💡 **Pro Tips**

1. **Start with small adjustments** (0.05-0.1 units)
2. **Test one item at a time** to isolate issues
3. **Use the debug logging** to see current values
4. **Save your working configurations** as comments
5. **Test with different body types** if possible

The positioning system is now fully configurable and easy to calibrate for each individual clothing item!
