# AR Outfit Generation System Flow

## 🎯 **Complete System Flow**

```
1. USER STARTS APP
   ↓
2. CAMERA PERMISSION REQUEST
   ↓
3. CAMERA PREVIEW STARTS
   ↓
4. BODY DETECTION (MediaPipe)
   ↓
5. USER CLICKS "GENERATE OUTFIT"
   ↓
6. FETCH USER DATA (Gender from Firestore)
   ↓
7. GET WEATHER DATA (Sunny/Rainy)
   ↓
8. GENERATE OUTFIT (4 items: top, bottom, shoes, accessories)
   ↓
9. APPLY POSITIONING DATA (ArPositioningConfig)
   ↓
10. RENDER AR OVERLAY (ArOverlayView)
    ↓
11. DISPLAY OUTFIT ON USER
```

## 📱 **Detailed Flow Breakdown**

### **Phase 1: App Initialization**
```
GenerateActivity.onCreate()
├── Request camera permission
├── Initialize CameraX
├── Initialize MediaPipe Pose Landmarker
├── Initialize ArOverlayView
└── Start camera preview
```

### **Phase 2: Body Detection**
```
Camera Preview → MediaPipe → Body Landmarks
├── Detect 15+ body landmarks
├── Track pose in real-time
├── Update ArOverlayView with landmarks
└── Show skeleton overlay (debug mode)
```

### **Phase 3: Outfit Generation**
```
User clicks "Generate Outfit" button
├── Fetch user gender from Firestore
├── Get weather condition (Sunny/Rainy)
├── Create OutfitGenerator with gender + weather
├── Generate 4 items:
│   ├── Top (shirt, blouse, sweater)
│   ├── Bottom (pants, shorts, skirt)
│   ├── Shoes (sneakers, boots, sandals)
│   └── Accessories (bag, hat, glasses)
└── Return complete Outfit object
```

### **Phase 4: AR Positioning & Rendering**
```
Outfit → ArOverlayView → AR Rendering
├── For each clothing item:
│   ├── Get positioning data from ArPositioningConfig
│   ├── Apply precise positioning (X, Y, Z)
│   ├── Apply precise sizing (width, height, depth)
│   ├── Determine rendering method:
│   │   ├── Top/Bottom: Body landmark positioning
│   │   ├── Shoes: Ankle positioning
│   │   ├── Bags: Hand positioning
│   │   ├── Hats: Head positioning
│   │   └── Glasses: Face positioning
│   └── Render with 2D overlay
└── Display complete outfit on user
```

## 🔄 **Real-time Calibration Flow**

```
User wants to adjust positioning
├── Call ArOverlayView.calibrateItemPositioning()
├── Update ArPositioningConfig data
├── Re-render item with new positioning
└── Save changes for future use
```

## 📊 **Data Flow Architecture**

### **Configuration Layer**
```
ArPositioningConfig.java
├── Static positioning data for all items
├── Gender-specific positioning
├── Weather-responsive positioning
└── Sizing data (width, height, depth)
```

### **Calibration Layer**
```
ArPositioningCalibrator.java
├── Runtime positioning adjustments
├── Sizing adjustments
├── Batch calibration
└── Reset to defaults
```

### **Rendering Layer**
```
ArOverlayView.java
├── Body landmark processing
├── Item-specific rendering
├── Positioning application
├── Real-time calibration
└── AR overlay display
```

## 🎮 **User Interaction Flow**

### **1. Initial Setup**
- User opens app
- Camera permission granted
- Body detection starts
- Skeleton overlay visible (debug)

### **2. Outfit Generation**
- User clicks "Generate Outfit"
- System fetches user data
- Weather data retrieved
- Outfit generated with 4 items
- AR overlay appears

### **3. Real-time Adjustment**
- User can calibrate positioning
- Adjust sizing in real-time
- Reset to defaults
- Save preferred settings

## 🔧 **Technical Components**

### **Core Classes**
- **GenerateActivity**: Main activity, orchestrates everything
- **OutfitGenerator**: Creates outfits based on gender/weather
- **WeatherService**: Provides weather data
- **ArOverlayView**: Renders AR overlays
- **ArPositioningConfig**: Stores positioning data
- **ArPositioningCalibrator**: Handles calibration

### **Data Models**
- **Outfit**: Complete outfit with items
- **OutfitItem**: Individual clothing item
- **PositioningData**: Position, scale, rotation, sizing

### **External Services**
- **MediaPipe**: Body landmark detection
- **CameraX**: Camera preview
- **Firestore**: User data storage
- **OpenWeatherMap**: Weather API (optional)

## 🎯 **Key Features**

### **Smart Positioning**
- Each item has precise positioning data
- Gender-specific positioning
- Weather-responsive adjustments
- Real-time calibration

### **Intelligent Accessories**
- Bags positioned on hands
- Hats positioned on head
- Glasses positioned on face
- Automatic type detection

### **Flexible Configuration**
- Static configuration in code
- Runtime calibration
- Easy debugging
- Persistent settings

## 🚀 **Performance Optimizations**

### **Rendering**
- 2D overlay instead of 3D models
- Efficient landmark processing
- Minimal redraws
- Smart caching

### **Detection**
- Real-time body tracking
- Optimized MediaPipe usage
- Efficient landmark updates
- Background processing

This system provides a complete AR outfit generation experience with precise positioning, real-time calibration, and intelligent accessory placement!
