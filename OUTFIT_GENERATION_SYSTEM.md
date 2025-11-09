# Outfit Generation System

## Overview
The AuraFit app now uses a sophisticated outfit generation system that creates personalized outfits based on user gender, weather conditions, and actual AR clothing models.

## File Structure Integration
The system uses a simplified gender-based AR clothing models structure:
```
ar_models/clothing/
├── male/
│   ├── top/
│   ├── bottom/
│   ├── shoes/
│   └── accessories/
└── female/
    ├── top/
    ├── bottom/
    ├── shoes/
    └── accessories/
```

**Note**: The old structure folders (`accessories/`, `glasses/`, `hats/`, `pants/`, `shirts/`, `shoes/`) have been removed. All clothing items are now organized by gender within their respective categories.

## Key Features

### 1. Gender-Based Generation
- **Male Users**: Generates outfits from `male/` folder with masculine clothing options
- **Female Users**: Generates outfits from `female/` folder with feminine clothing options
- **Mixed/Unknown**: Uses unisex options that work for all users

### 2. Weather-Responsive Clothing
- **Sunny Weather**: Light, breathable clothing (shorts, t-shirts, sandals)
- **Rainy Weather**: Protective, layered clothing (jackets, boots, long sleeves)

### 3. Complete Outfit Generation
Each outfit includes:
- **Top**: Shirts, blouses, t-shirts, sweaters
- **Bottom**: Pants, shorts, skirts, jeans
- **Shoes**: Sneakers, boots, sandals, loafers
- **Accessories** (30% chance): Gender-specific accessories (watches, bracelets, necklaces, rings)
- **Hats** (40% chance in sunny, 10% in rainy): Gender and weather-appropriate headwear

## Gender-Specific Options

### Male Clothing Options
**Sunny Weather:**
- Tops: Casual T-Shirt, Polo Shirt, Tank Top, Short Sleeve Shirt
- Bottoms: Summer Shorts, Cargo Shorts, Swim Trunks, Athletic Shorts
- Shoes: Sneakers, Sandals, Loafers, Canvas Shoes

**Rainy Weather:**
- Tops: Long Sleeve Shirt, Hoodie, Sweater, Jacket
- Bottoms: Jeans, Chinos, Cargo Pants, Joggers
- Shoes: Boots, Waterproof Sneakers, Leather Shoes, Rain Boots
- Accessories: Watch, Bracelet, Ring, Cufflinks
- Hats: Beanie, Cap, Fedora

### Female Clothing Options
**Sunny Weather:**
- Tops: Summer Blouse, Tank Top, Crop Top, Light Dress
- Bottoms: Summer Shorts, Skirt, Capri Pants, Denim Shorts
- Shoes: Sneakers, Sandals, Flats, Canvas Shoes
- Accessories: Watch, Bracelet, Necklace, Ring, Earrings
- Hats: Sun Hat, Baseball Cap, Wide Brim Hat

**Rainy Weather:**
- Tops: Long Sleeve Blouse, Cardigan, Sweater, Light Jacket
- Bottoms: Jeans, Leggings, Trousers, Long Skirt
- Shoes: Boots, Ankle Boots, Waterproof Sneakers, Rain Boots
- Accessories: Watch, Bracelet, Necklace, Ring, Earrings
- Hats: Beanie, Beret, Fedora

## AR Model Integration
- **Model Paths**: `ar_models/clothing/{gender}/{category}/{item_name}.obj`
- **File Naming**: Converts item names to snake_case (e.g., "Baseball Cap" → "baseball_cap.obj")
- **Category Mapping**: Automatically maps items to correct subcategories

## Usage Example
```java
// Create generator with user context
OutfitGenerator generator = new OutfitGenerator("Male", "Sunny");

// Generate personalized outfit
Outfit outfit = generator.generateOutfit();

// Outfit will contain:
// - Male-specific clothing items
// - Weather-appropriate selections
// - Complete AR model paths
// - Optional accessories and hats
```

## Benefits
1. **Personalized**: Each user gets gender-appropriate clothing
2. **Weather-Aware**: Clothing matches current weather conditions
3. **Realistic**: Uses actual AR models from the clothing structure
4. **Complete**: Generates full outfits with accessories
5. **Flexible**: Supports mixed/unknown gender preferences

The system ensures that every generated outfit is both stylish and practical for the current weather conditions while respecting the user's gender preferences!
