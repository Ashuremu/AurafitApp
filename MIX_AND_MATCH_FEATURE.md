# Mix & Match Feature - Try On Custom Outfits

## Overview
This feature allows users to select individual clothing items from generated outfit recommendations and create custom "mix and match" combinations to try on.

## How It Works

### 1. Generate Outfits
- User positions themselves in front of the camera
- Taps "Generate Outfit" button
- System generates 3 complete outfit recommendations based on weather and user preferences
- Each outfit contains: Top, Bottom, Shoes, and Accessories

### 2. Mix & Match Button
- After outfits are generated, a purple "Mix & Match" button appears on the right side
- Button is positioned above the outfit option buttons (1, 2, 3)

### 3. Item Selection Panel
When user taps "Mix & Match":
- A bottom panel slides up showing the item selector
- Panel contains:
  - **Category Tabs**: Top, Bottom, Shoes, Accessories
  - **Item Grid**: Horizontal scrollable list of items in selected category
  - **Try On Button**: Apply selected combination

### 4. Selecting Items
- User switches between categories using tabs
- Each category shows all unique items from the 3 generated outfits
- Tapping an item:
  - Highlights it with a teal color
  - Shows a green dot indicator
  - Displays a toast message confirming selection
- Selected items are remembered when switching categories

### 5. Try On Custom Outfit
- User selects one item from each category (Top, Bottom, Shoes, Accessories)
- Taps "Try On Selected Items" button
- System creates a custom outfit with selected items
- AR overlay updates to show the custom combination
- Panel closes and outfit is displayed on camera view

### 6. Save Custom Outfit
- Users can save their custom mix & match outfit to wardrobe
- Tap "Save Outfit" button
- Outfit is saved with name "Custom Mix & Match"

## UI Components

### New Layout Files
1. **item_clothing_selector.xml**
   - Individual item card in the selector
   - Shows item icon, name, and selection indicator
   - 80dp width cards in horizontal scroll

2. **outfit_item_selector_panel.xml**
   - Main selector panel container
   - Category tabs (Top, Bottom, Shoes, Accessories)
   - Items container (horizontal scroll)
   - Try On button
   - Close button (X)

### New Java Classes
1. **ClothingItemAdapter.java**
   - RecyclerView adapter for clothing items
   - Handles item selection and display
   - Dynamic icon assignment based on category

### Modified Files
1. **GenerateActivity.java**
   - Added item selector panel management
   - Implemented category switching
   - Added custom outfit creation logic
   - New methods:
     - `showItemSelectorPanel()` - Display the selector
     - `hideItemSelectorPanel()` - Hide the selector
     - `extractAllItemsFromOutfits()` - Get all unique items
     - `switchCategory()` - Change category tab
     - `displayItemsForCategory()` - Show items for selected category
     - `selectItem()` - Mark item as selected
     - `applyCustomOutfit()` - Create and display custom outfit

2. **activity_generate.xml**
   - Added Mix & Match button
   - Added outfit item selector panel container

### New Drawable Resources
- **ic_shirt.xml** - Top items icon
- **ic_pants.xml** - Bottom items icon
- **ic_shoes.xml** - Shoes items icon
- **ic_accessories.xml** - Accessories items icon
- **ic_close.xml** - Close button icon

## User Flow

```
1. User opens Generate screen
2. Camera detects user
3. User taps "Generate Outfit"
4. System shows 3 outfit recommendations
5. User taps "Mix & Match" button
6. Item selector panel opens
7. User browses "Tops" category (default)
8. User selects preferred top
9. User switches to "Bottoms" tab
10. User selects preferred bottom
11. User switches to "Shoes" tab
12. User selects preferred shoes
13. User switches to "Accessories" tab
14. User selects preferred accessory
15. User taps "Try On Selected Items"
16. System creates custom outfit
17. AR overlay shows custom combination
18. User can save custom outfit to wardrobe
```

## Features

### Smart Item Deduplication
- System automatically removes duplicate items
- Only shows unique clothing pieces from all 3 outfits
- Based on item name comparison

### Default Selection
- First item from each category is pre-selected
- User can immediately try on without selecting if they want

### Visual Feedback
- Selected items have teal background (#4ECDC4)
- Unselected items have dark transparent background
- Active category tab has orange color (#FF6B35)
- Inactive tabs have dark transparent background
- Green dot indicator on selected items

### Responsive UI
- Horizontal scrolling for items (no vertical space waste)
- Panel slides up from bottom
- Doesn't block camera view completely
- Easy to close with X button

## Technical Details

### Item Storage
```java
private List<OutfitItem> allTops, allBottoms, allShoes, allAccessories;
private OutfitItem selectedTop, selectedBottom, selectedShoes, selectedAccessories;
```

### Category Navigation
- Tab buttons update background colors dynamically
- Items refresh when switching categories
- Selection state persists across category switches

### Custom Outfit Creation
```java
Outfit customOutfit = new Outfit();
customOutfit.setName("Custom Mix & Match");
customOutfit.setStyle("Custom");
customOutfit.setItems([selectedTop, selectedBottom, selectedShoes, selectedAccessories]);
```

## Benefits

1. **Increased Engagement**: Users can experiment with different combinations
2. **Personalization**: Users create outfits matching their exact preferences
3. **Discovery**: Users explore all available clothing options
4. **Flexibility**: Not limited to AI's complete outfit recommendations
5. **Fun Factor**: Interactive mix-and-match experience

## Future Enhancements

Potential improvements:
- Add preview thumbnails instead of just icons
- Allow "no selection" for certain categories (e.g., skip accessories)
- Add color filter for items
- Show item compatibility scores
- Save multiple custom combinations
- Share custom outfits with friends
- Add more items from user's wardrobe to mix-and-match pool

