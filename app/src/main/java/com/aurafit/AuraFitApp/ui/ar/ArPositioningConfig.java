package com.aurafit.AuraFitApp.ui.ar;

import java.util.HashMap;
import java.util.Map;

public class ArPositioningConfig {
    
    private static Map<String, Map<String, PositioningData>> positioningMap = new HashMap<>();
    
    static {
        initializePositioningData();
    }
    
    private static void initializePositioningData() {
        Map<String, PositioningData> topPositioning = new HashMap<>();
        
        topPositioning.put("Alo grey sweater", new PositioningData(0.0f, 9.0f, 0.0f, 2.5f, 0.0f, 0.0f, 0.0f, 2.5f, 4.0f, 2.2f));
        topPositioning.put("Calvin klein red shirt", new PositioningData(0.0f, 0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.9f));
        topPositioning.put("Lacoste green shirt", new PositioningData(0.0f, -0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.9f));
        topPositioning.put("Lacoste white polo", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.9f));
        topPositioning.put("Mint Green Polo", new PositioningData(0.0f, 0.2f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.9f));
        topPositioning.put("Sweater green", new PositioningData(0.0f, 9.0f, 0.0f, 2.5f, 0.0f, 0.0f, 0.0f, 2.5f, 4.0f, 2.2f));
        topPositioning.put("Sweater puma", new PositioningData(0.0f, 9.0f, 0.0f, 2.5f, 0.0f, 0.0f, 0.0f, 2.5f, 4.0f, 2.2f));
        
        topPositioning.put("Beige off shoulder", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.9f));
        topPositioning.put("Gold top", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.9f));
        topPositioning.put("Pink flower top", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.9f));
        topPositioning.put("Ralph lauren crop top", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.9f));
        topPositioning.put("Red sleeveless", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.9f));
        topPositioning.put("White blouse", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.9f));
        topPositioning.put("Brown blouse", new PositioningData(0.0f, 9.0f, 0.0f, 2.5f, 0.0f, 0.0f, 0.0f, 2.5f, 4.0f, 2.2f));
        
        positioningMap.put("top", topPositioning);
        
        Map<String, PositioningData> bottomPositioning = new HashMap<>();
        
        bottomPositioning.put("Black denim cargo shorts", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.1f, -0.5f, 1.0f));
        bottomPositioning.put("Black pants", new PositioningData(0.0f, 0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.3f, 1.2f, 1.0f));
        bottomPositioning.put("Lacoste black short", new PositioningData(0.0f, -0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, -0.5f, 0.9f));
        bottomPositioning.put("Lacoste brown short", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, -0.5f, 0.9f));
        bottomPositioning.put("White cargo pants", new PositioningData(0.0f, 0.2f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.4f, 1.2f, 1.0f));
        bottomPositioning.put("White short", new PositioningData(0.0f, -0.2f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, -0.5f, 0.9f));
        bottomPositioning.put("Zara black pants", new PositioningData(0.0f, 0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.3f, 1.2f, 1.0f));
        
        bottomPositioning.put("Black Celine denim shorts", new PositioningData(0.0f, -0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.9f, -0.5f, 0.8f));
        bottomPositioning.put("Black leather skirt", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.9f, 0.8f, 0.8f));
        bottomPositioning.put("Brown long skirt", new PositioningData(0.0f, 0.2f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.9f, 1.1f, 0.8f));
        bottomPositioning.put("Brown pants", new PositioningData(0.0f, 0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.2f, 1.1f, 0.8f));
        bottomPositioning.put("Denim pants", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.2f, 1.1f, 0.8f));
        bottomPositioning.put("White pants", new PositioningData(0.0f, 0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.2f, 1.1f, 0.8f));
        bottomPositioning.put("White skirt", new PositioningData(0.0f, -0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.9f, 0.8f, 0.8f));
        
        positioningMap.put("bottom", bottomPositioning);
        
        Map<String, PositioningData> shoesPositioning = new HashMap<>();
        
        shoesPositioning.put("Adidas Rubber Shoes", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.1f, 1.0f, 1.1f));
        shoesPositioning.put("Black Sandal", new PositioningData(0.0f, 0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.9f, 1.0f));
        shoesPositioning.put("Brown leather shoes", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.1f));
        shoesPositioning.put("Red Shoes", new PositioningData(0.0f, -0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.1f, 1.0f, 1.1f));
        shoesPositioning.put("Slippers Jordan", new PositioningData(0.0f, 0.2f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.9f, 1.0f));
        shoesPositioning.put("Vans Rubber Shoes", new PositioningData(0.0f, -0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.1f, 1.0f, 1.1f));
        
        shoesPositioning.put("Black heels", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.9f, 1.1f, 0.9f));
        shoesPositioning.put("Platform sandals", new PositioningData(0.0f, 0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.9f, 1.0f, 0.9f));
        shoesPositioning.put("Red leather shoes", new PositioningData(0.0f, -0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.9f, 1.0f, 0.9f));
        shoesPositioning.put("White bow heels", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.9f, 1.1f, 0.9f));
        shoesPositioning.put("White sandals", new PositioningData(0.0f, 0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.9f, 1.0f, 0.9f));
        
        positioningMap.put("shoes", shoesPositioning);
        
        Map<String, PositioningData> accessoriesPositioning = new HashMap<>();
        
        accessoriesPositioning.put("Sling Bag", new PositioningData(0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 1.5f, 1.5f, 1.0f));
        accessoriesPositioning.put("Sling Bag(1)", new PositioningData(0.0f, 0.1f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 1.5f, 1.5f, 1.0f));
        accessoriesPositioning.put("Chanel white handbag", new PositioningData(0.0f, -0.1f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 1.3f, 1.4f, 1.0f));
        accessoriesPositioning.put("Charles and Keith Black Bag", new PositioningData(0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 1.4f, 1.5f, 1.0f));
        accessoriesPositioning.put("Mini Lady Dior", new PositioningData(0.0f, -0.2f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 1.2f, 1.3f, 1.0f));
        
        accessoriesPositioning.put("Adidas", new PositioningData(0.0f, 0.0f, 0.0f, 1.5f, 0.0f, 0.0f, 0.0f, 1.3f, 1.2f, 1.0f));
        accessoriesPositioning.put("Brown Cap", new PositioningData(0.0f, 0.1f, 0.0f, 1.5f, 0.0f, 0.0f, 0.0f, 1.2f, 1.2f, 1.0f));
        accessoriesPositioning.put("Gucci", new PositioningData(0.0f, -0.1f, 0.0f, 1.5f, 0.0f, 0.0f, 0.0f, 1.2f, 1.2f, 1.0f));
        accessoriesPositioning.put("Hermes", new PositioningData(0.0f, 0.0f, 0.0f, 1.5f, 0.0f, 0.0f, 0.0f, 1.2f, 1.2f, 1.0f));
        
        accessoriesPositioning.put("Glasses", new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.9f, 0.9f, 0.8f));
        
        positioningMap.put("accessories", accessoriesPositioning);
    }
    
    public static PositioningData getPositioningData(String category, String itemName) {
        PositioningData defaultData = getDefaultPositioning(category);
        
        Map<String, PositioningData> categoryData = positioningMap.get(category);
        if (categoryData != null && categoryData.containsKey(itemName)) {
            PositioningData individualData = categoryData.get(itemName);
            
            return new PositioningData(
                defaultData.x + individualData.x,
                defaultData.y + individualData.y,
                defaultData.z + individualData.z,
                defaultData.scale * individualData.scale,
                defaultData.rotationX + individualData.rotationX,
                defaultData.rotationY + individualData.rotationY,
                defaultData.rotationZ + individualData.rotationZ,
                defaultData.widthScale * individualData.widthScale,
                defaultData.heightScale * individualData.heightScale,
                defaultData.depthScale * individualData.depthScale
            );
        }
        
        return defaultData;
    }
    
    public static void updatePositioningData(String category, String itemName, PositioningData newData) {
        Map<String, PositioningData> categoryData = positioningMap.get(category);
        if (categoryData != null) {
            categoryData.put(itemName, newData);
        }
    }
    
    public static PositioningData getDefaultPositioning(String category) {
        switch (category.toLowerCase()) {
            case "top":
                return new PositioningData(0.0f, 3.0f, 0.0f, 0.8f, 0.0f, 0.0f, 0.0f, 1.0f, 2.5f, 1.0f);
            case "bottom":
                return new PositioningData(0.0f, -2.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            case "shoes":
                return new PositioningData(0.0f, 2.0f, 0.0f, 2.2f, 0.0f, 0.0f, 0.0f, 1.5f, 1.6f, 2.0f);
            case "accessories":
                return new PositioningData(0.1f, -0.1f, 0.0f, 1.5f, 0.0f, 0.0f, 0.0f, 1.2f, 1.2f, 1.2f);
            default:
                return new PositioningData(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
    }
    
    public static class PositioningData {
        public float x, y, z;
        public float scale;
        public float rotationX, rotationY, rotationZ;
        public float widthScale;
        public float heightScale;
        public float depthScale;
        
        public PositioningData(float x, float y, float z, float scale, 
                              float rotationX, float rotationY, float rotationZ) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.scale = scale;
            this.rotationX = rotationX;
            this.rotationY = rotationY;
            this.rotationZ = rotationZ;
            this.widthScale = 1.0f;
            this.heightScale = 2.0f;
            this.depthScale = 1.0f;
        }
        
        public PositioningData(float x, float y, float z, float scale, 
                              float rotationX, float rotationY, float rotationZ,
                              float widthScale, float heightScale, float depthScale) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.scale = scale;
            this.rotationX = rotationX;
            this.rotationY = rotationY;
            this.rotationZ = rotationZ;
            this.widthScale = widthScale;
            this.heightScale = heightScale;
            this.depthScale = depthScale;
        }
        
        @Override
        public String toString() {
            return String.format("Position(x=%.2f, y=%.2f, z=%.2f, scale=%.2f, rotX=%.2f, rotY=%.2f, rotZ=%.2f, size(w=%.2f, h=%.2f, d=%.2f))", 
                               x, y, z, scale, rotationX, rotationY, rotationZ, widthScale, heightScale, depthScale);
        }
    }
}