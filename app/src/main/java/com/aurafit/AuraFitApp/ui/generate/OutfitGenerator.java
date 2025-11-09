package com.aurafit.AuraFitApp.ui.generate;

import com.aurafit.AuraFitApp.data.model.Outfit;
import com.aurafit.AuraFitApp.data.model.OutfitItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OutfitGenerator {
    
    private Random random;
    private String userGender;
    private String weatherCondition;
    
    public OutfitGenerator() {
        this.random = new Random();
        this.userGender = "Unknown";
        this.weatherCondition = "Sunny";
    }
    
    public OutfitGenerator(String userGender, String weatherCondition) {
        this.random = new Random();
        this.userGender = userGender != null ? userGender : "Unknown";
        this.weatherCondition = weatherCondition != null ? weatherCondition : "Sunny";
    }
    
    public Outfit generateOutfit() {
        Outfit outfit = new Outfit();
        outfit.setName("AI Generated " + userGender + " Outfit");
        outfit.setStyle(getStyleBasedOnWeather());
        outfit.setConfidence(85);
        
        List<OutfitItem> items = new ArrayList<>();
        
        // Always add exactly 1 item from each category
        items.add(generateTopItem());
        items.add(generateBottomItem());
        items.add(generateShoesItem());
        items.add(generateAccessoryItem());
        
        outfit.setItems(items);
        
        return outfit;
    }
    
    private String getStyleBasedOnWeather() {
        if (weatherCondition.equals("Sunny")) {
            return "Summer Casual";
        } else {
            return "Rainy Weather";
        }
    }
    
    private OutfitItem generateTopItem() {
        OutfitItem item = new OutfitItem();
        item.setItemId("top_" + random.nextInt(1000));
        
        // Get actual PNG file names based on gender and weather
        String[] topOptions = getTopOptions();
        String selectedTop = topOptions[random.nextInt(topOptions.length)];
        
        item.setName(selectedTop);
        item.setCategory("Top");
        item.setSubcategory(getSubcategoryFromName(selectedTop));
        item.setImageUrl("");
        // Clean the selected top name to match the actual file names in assets
        String cleanTopName = selectedTop.replace(" ", " "); // Keep spaces as they are in the actual files
        item.setArModelUrl("ar_models/clothing/" + userGender.toLowerCase() + "/top/" + getWeatherFolder() + "/" + cleanTopName + ".png");
        item.setConfidence(90);
        
        return item;
    }
    
    private String[] getTopOptions() {
        if (userGender.equals("Male")) {
            return new String[]{
                "Alo grey sweater", "Calvin klein red shirt", "Lacoste green shirt", 
                "Lacoste white polo", "Mint Green Polo", "Sweater green", "Sweater puma"
            };
        } else if (userGender.equals("Female")) {
            if (weatherCondition.equals("Sunny")) {
                return new String[]{
                    "Beige off shoulder", "Gold top", "Pink flower top", 
                    "Ralph lauren crop top", "Red sleeveless", "White blouse"
                };
            } else {
                return new String[]{
                    "Brown blouse", "Gold top", "Pink flower top", "White blouse"
                };
            }
        } else {
            // Mixed/Unknown - combine ALL male and female options
            String[] maleOptions = {
                "Alo grey sweater", "Calvin klein red shirt", "Lacoste green shirt", 
                "Lacoste white polo", "Mint Green Polo", "Sweater green", "Sweater puma"
            };
            String[] femaleOptions;
            if (weatherCondition.equals("Sunny")) {
                femaleOptions = new String[]{
                    "Beige off shoulder", "Gold top", "Pink flower top", 
                    "Ralph lauren crop top", "Red sleeveless", "White blouse"
                };
            } else {
                femaleOptions = new String[]{
                    "Brown blouse", "Gold top", "Pink flower top", "White blouse"
                };
            }
            
            // Combine both arrays - ALL options available
            String[] combinedOptions = new String[maleOptions.length + femaleOptions.length];
            System.arraycopy(maleOptions, 0, combinedOptions, 0, maleOptions.length);
            System.arraycopy(femaleOptions, 0, combinedOptions, maleOptions.length, femaleOptions.length);
            
            return combinedOptions;
        }
    }
    
    private OutfitItem generateBottomItem() {
        OutfitItem item = new OutfitItem();
        item.setItemId("bottom_" + random.nextInt(1000));
        
        // Get actual PNG file names based on gender and weather
        String[] bottomOptions = getBottomOptions();
        String selectedBottom = bottomOptions[random.nextInt(bottomOptions.length)];
        
        item.setName(selectedBottom);
        item.setCategory("Bottom");
        item.setSubcategory(getSubcategoryFromName(selectedBottom));
        item.setImageUrl("");
        item.setArModelUrl("ar_models/clothing/" + userGender.toLowerCase() + "/bottom/" + getWeatherFolder() + "/" + selectedBottom + ".png");
        item.setConfidence(85);
        
        return item;
    }
    
    private String[] getBottomOptions() {
        if (userGender.equals("Male")) {
            return new String[]{
                "Black denim cargo shorts", "Black pants", "Lacoste black short", 
                "Lacoste brown short", "White cargo pants", "White short", "Zara black pants"
            };
        } else if (userGender.equals("Female")) {
            if (weatherCondition.equals("Sunny")) {
                return new String[]{
                    "Black Celine denim shorts", "Black leather skirt", "Brown long skirt", 
                    "Brown pants", "Denim pants", "White pants", "White skirt"
                };
            } else {
                return new String[]{
                    "Brown long skirt", "Brown pants", "Denim pants", "White pants"
                };
            }
        } else {
            // Mixed/Unknown - combine ALL male and female options
            String[] maleOptions = {
                "Black denim cargo shorts", "Black pants", "Lacoste black short", 
                "Lacoste brown short", "White cargo pants", "White short", "Zara black pants"
            };
            String[] femaleOptions;
            if (weatherCondition.equals("Sunny")) {
                femaleOptions = new String[]{
                    "Black Celine denim shorts", "Black leather skirt", "Brown long skirt", 
                    "Brown pants", "Denim pants", "White pants", "White skirt"
                };
            } else {
                femaleOptions = new String[]{
                    "Brown long skirt", "Brown pants", "Denim pants", "White pants"
                };
            }
            
            // Combine both arrays - ALL options available
            String[] combinedOptions = new String[maleOptions.length + femaleOptions.length];
            System.arraycopy(maleOptions, 0, combinedOptions, 0, maleOptions.length);
            System.arraycopy(femaleOptions, 0, combinedOptions, maleOptions.length, femaleOptions.length);
            
            return combinedOptions;
        }
    }
    
    private OutfitItem generateShoesItem() {
        OutfitItem item = new OutfitItem();
        item.setItemId("shoes_" + random.nextInt(1000));
        
        // Get actual PNG file names based on gender and weather
        String[] shoesOptions = getShoesOptions();
        String selectedShoes = shoesOptions[random.nextInt(shoesOptions.length)];
        
        item.setName(selectedShoes);
        item.setCategory("Shoes");
        item.setSubcategory(getSubcategoryFromName(selectedShoes));
        item.setImageUrl("");
        item.setArModelUrl("ar_models/clothing/" + userGender.toLowerCase() + "/shoes/" + getWeatherFolder() + "/" + selectedShoes + ".png");
        item.setConfidence(80);
        
        return item;
    }
    
    private String[] getShoesOptions() {
        if (userGender.equals("Male")) {
            if (weatherCondition.equals("Sunny")) {
                return new String[]{
                    "Adidas Rubber Shoes", "Black Sandal", "Brown leather shoes", 
                    "Red Shoes", "Slippers Jordan", "Vans Rubber Shoes"
                };
            } else {
                return new String[]{
                    "Adidas Rubber Shoes", "Black Sandal", "Red Shoes", 
                    "Slippers Jordan", "Vans Rubber Shoes"
                };
            }
        } else if (userGender.equals("Female")) {
            if (weatherCondition.equals("Sunny")) {
                return new String[]{
                    "Black heels", "Platform sandals", "Red leather shoes", 
                    "White bow heels", "White sandals"
                };
            } else {
                return new String[]{
                    "Platform sandals", "Red leather shoes", "White sandals"
                };
            }
        } else {
            // Mixed/Unknown - combine ALL male and female options
            String[] maleOptions;
            if (weatherCondition.equals("Sunny")) {
                maleOptions = new String[]{
                    "Adidas Rubber Shoes", "Black Sandal", "Brown leather shoes", 
                    "Red Shoes", "Slippers Jordan", "Vans Rubber Shoes"
                };
            } else {
                maleOptions = new String[]{
                    "Adidas Rubber Shoes", "Black Sandal", "Red Shoes", 
                    "Slippers Jordan", "Vans Rubber Shoes"
                };
            }
            
            String[] femaleOptions;
            if (weatherCondition.equals("Sunny")) {
                femaleOptions = new String[]{
                    "Black heels", "Platform sandals", "Red leather shoes", 
                    "White bow heels", "White sandals"
                };
            } else {
                femaleOptions = new String[]{
                    "Platform sandals", "Red leather shoes", "White sandals"
                };
            }
            
            // Combine both arrays - ALL options available
            String[] combinedOptions = new String[maleOptions.length + femaleOptions.length];
            System.arraycopy(maleOptions, 0, combinedOptions, 0, maleOptions.length);
            System.arraycopy(femaleOptions, 0, combinedOptions, maleOptions.length, femaleOptions.length);
            
            return combinedOptions;
        }
    }
    
    private OutfitItem generateAccessoryItem() {
        OutfitItem item = new OutfitItem();
        item.setItemId("accessory_" + random.nextInt(1000));
        
        // Get actual PNG file names for accessories
        String[] accessoryOptions = getAccessoryOptions();
        String selectedAccessory = accessoryOptions[random.nextInt(accessoryOptions.length)];
        
        item.setName(selectedAccessory);
        item.setCategory("Accessories");
        item.setSubcategory(getSubcategoryFromName(selectedAccessory));
        item.setImageUrl("");
        
        // Determine if it's a bag (hand positioning) or other accessory
        String arModelPath;
        if (isBag(selectedAccessory)) {
            // Bags are positioned on hand
            arModelPath = "ar_models/clothing/" + userGender.toLowerCase() + "/accessories/" + getWeatherFolder() + "/bag/" + selectedAccessory + ".png";
        } else if (isHat(selectedAccessory)) {
            // Hats are positioned on head
            arModelPath = "ar_models/clothing/" + userGender.toLowerCase() + "/accessories/" + getWeatherFolder() + "/hat/" + selectedAccessory + ".png";
        } else {
            // Glasses are positioned on face
            arModelPath = "ar_models/clothing/" + userGender.toLowerCase() + "/accessories/" + getWeatherFolder() + "/glasses/" + selectedAccessory + ".png";
        }
        
        item.setArModelUrl(arModelPath);
        item.setConfidence(75);
        
        return item;
    }
    
    private OutfitItem generateHatItem() {
        OutfitItem item = new OutfitItem();
        item.setItemId("hat_" + random.nextInt(1000));
        
        String[] hatOptions = getHatOptions();
        String selectedHat = hatOptions[random.nextInt(hatOptions.length)];
        
        item.setName(selectedHat);
        item.setCategory("Hats");
        item.setSubcategory(getSubcategoryFromName(selectedHat));
        item.setImageUrl("");
        item.setArModelUrl("ar_models/clothing/" + userGender.toLowerCase() + "/accessories/" + getModelFileName(selectedHat));
        item.setConfidence(70);
        
        return item;
    }
    
    private String[] getAccessoryOptions() {
        if (userGender.equals("Male")) {
            // Male accessories: bags and hats
            return new String[]{
                "Sling Bag", "Sling Bag(1)", "Adidas", "Brown Cap", "Gucci", "Hermes"
            };
        } else if (userGender.equals("Female")) {
            if (weatherCondition.equals("Sunny")) {
                // Female accessories: bags, hats, and glasses for sunny weather
                return new String[]{
                    "Chanel white handbag", "Charles and Keith Black Bag", "Mini Lady Dior",
                    "Adidas", "Brown Cap", "Gucci", "Hermes", "Glasses"
                };
            } else {
                // Female accessories: bags and hats for rainy weather
                return new String[]{
                    "Chanel white handbag", "Charles and Keith Black Bag", "Mini Lady Dior",
                    "Adidas", "Brown Cap", "Gucci", "Hermes"
                };
            }
        } else {
            // Mixed/Unknown - combine ALL male and female options
            String[] maleOptions = {
                "Sling Bag", "Sling Bag(1)", "Adidas", "Brown Cap", "Gucci", "Hermes"
            };
            String[] femaleOptions;
            if (weatherCondition.equals("Sunny")) {
                femaleOptions = new String[]{
                    "Chanel white handbag", "Charles and Keith Black Bag", "Mini Lady Dior",
                    "Adidas", "Brown Cap", "Gucci", "Hermes", "Glasses"
                };
            } else {
                femaleOptions = new String[]{
                    "Chanel white handbag", "Charles and Keith Black Bag", "Mini Lady Dior",
                    "Adidas", "Brown Cap", "Gucci", "Hermes"
                };
            }
            
            // Combine both arrays - ALL options available
            String[] combinedOptions = new String[maleOptions.length + femaleOptions.length];
            System.arraycopy(maleOptions, 0, combinedOptions, 0, maleOptions.length);
            System.arraycopy(femaleOptions, 0, combinedOptions, maleOptions.length, femaleOptions.length);
            
            return combinedOptions;
        }
    }
    
    private boolean isBag(String accessoryName) {
        return accessoryName.toLowerCase().contains("bag") || 
               accessoryName.toLowerCase().contains("handbag") ||
               accessoryName.toLowerCase().contains("dior");
    }
    
    private boolean isHat(String accessoryName) {
        return accessoryName.toLowerCase().contains("cap") || 
               accessoryName.toLowerCase().contains("adidas") ||
               accessoryName.toLowerCase().contains("gucci") ||
               accessoryName.toLowerCase().contains("hermes");
    }
    
    private String[] getHatOptions() {
        if (userGender.equals("Male")) {
            if (weatherCondition.equals("Sunny")) {
                return new String[]{"Baseball Cap", "Sun Hat", "Bucket Hat"};
            } else {
                return new String[]{"Beanie", "Cap", "Fedora"};
            }
        } else if (userGender.equals("Female")) {
            if (weatherCondition.equals("Sunny")) {
                return new String[]{"Sun Hat", "Baseball Cap", "Wide Brim Hat"};
            } else {
                return new String[]{"Beanie", "Beret", "Fedora"};
            }
        } else {
            if (weatherCondition.equals("Sunny")) {
                return new String[]{"Baseball Cap", "Sun Hat"};
            } else {
                return new String[]{"Beanie", "Cap"};
            }
        }
    }
    
    private String getSubcategoryFromName(String itemName) {
        if (itemName.toLowerCase().contains("shirt") || itemName.toLowerCase().contains("blouse")) {
            return "Shirt";
        } else if (itemName.toLowerCase().contains("shorts")) {
            return "Shorts";
        } else if (itemName.toLowerCase().contains("jeans")) {
            return "Jeans";
        } else if (itemName.toLowerCase().contains("sneakers")) {
            return "Sneakers";
        } else if (itemName.toLowerCase().contains("boots")) {
            return "Boots";
        } else if (itemName.toLowerCase().contains("cap")) {
            return "Cap";
        } else {
            return "General";
        }
    }
    
    private String getWeatherFolder() {
        if (weatherCondition.equals("Sunny")) {
            return "Sunny";
        } else if (weatherCondition.equals("Rainy")) {
            return "Rainy";
        } else {
            return "SunnyRainy"; // Default fallback
        }
    }
    
    private String getModelFileName(String itemName) {
        return itemName.toLowerCase().replace(" ", "_").replace("-", "_") + ".obj";
    }
}
