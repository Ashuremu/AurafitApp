package com.aurafit.AuraFitApp.ui.homepage.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WardrobeItem implements Serializable {
    private String id;
    private String name;
    private String description;
    private String weather;
    private String gender;
    private double price;
    private String imageUrl;
    private Object sizes; // Flexible to handle both List and Map
    private List<String> categories;
    private String mainCategory;
    private String uid;
    private transient Date createdAt;
    private transient Date updatedAt;

    // Default constructor required for Firestore
    public WardrobeItem() {}

    public WardrobeItem(String id, String name, String description, String weather, 
                       String gender, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.weather = weather;
        this.gender = gender;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Object getSizes() {
        return sizes;
    }

    public void setSizes(Object sizes) {
        this.sizes = sizes;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<SizeInfo> getSizesAsList() {
        if (sizes == null) {
            return null;
        }
        
        if (sizes instanceof List) {
            List<?> sizeList = (List<?>) sizes;
            List<SizeInfo> result = new ArrayList<>();
            
            for (Object item : sizeList) {
                if (item instanceof Map) {
                    Map<String, Object> sizeMap = (Map<String, Object>) item;
                    SizeInfo sizeInfo = new SizeInfo();
                    sizeInfo.setSize((String) sizeMap.get("size"));
                    sizeInfo.setHeight((String) sizeMap.get("height"));
                    sizeInfo.setWidth((String) sizeMap.get("width"));
                    
                    // Handle stock field - it might be Integer or Long from Firestore
                    Object stockObj = sizeMap.get("stock");
                    if (stockObj instanceof Integer) {
                        sizeInfo.setStock((Integer) stockObj);
                    } else if (stockObj instanceof Long) {
                        sizeInfo.setStock(((Long) stockObj).intValue());
                    } else if (stockObj instanceof Double) {
                        sizeInfo.setStock(((Double) stockObj).intValue());
                    } else {
                        sizeInfo.setStock(0); // Default to 0 if stock is not available
                    }
                    
                    result.add(sizeInfo);
                } else if (item instanceof SizeInfo) {
                    result.add((SizeInfo) item);
                }
            }
            return result;
        }
        
        // If it's a single Map, convert to List (for backward compatibility)
        if (sizes instanceof Map) {
            Map<String, Object> sizeMap = (Map<String, Object>) sizes;
            List<SizeInfo> sizeList = new ArrayList<>();
            SizeInfo sizeInfo = new SizeInfo();
            sizeInfo.setSize((String) sizeMap.get("size"));
            sizeInfo.setHeight((String) sizeMap.get("height"));
            sizeInfo.setWidth((String) sizeMap.get("width"));
            
            // Handle stock field
            Object stockObj = sizeMap.get("stock");
            if (stockObj instanceof Integer) {
                sizeInfo.setStock((Integer) stockObj);
            } else if (stockObj instanceof Long) {
                sizeInfo.setStock(((Long) stockObj).intValue());
            } else if (stockObj instanceof Double) {
                sizeInfo.setStock(((Double) stockObj).intValue());
            } else {
                sizeInfo.setStock(0);
            }
            
            sizeList.add(sizeInfo);
            return sizeList;
        }
        
        return null;
    }

    public int getStockForSize(String size) {
        try {
            List<SizeInfo> sizeList = getSizesAsList();
            if (sizeList == null || size == null) {
                return 0;
            }
            
            for (SizeInfo sizeInfo : sizeList) {
                if (sizeInfo != null && size.equals(sizeInfo.getSize())) {
                    return sizeInfo.getStock();
                }
            }
        } catch (Exception e) {
            // Log error and return 0 for safety
            System.err.println("Error getting stock for size " + size + ": " + e.getMessage());
        }
        
        return 0;
    }

    public boolean isSizeAvailable(String size) {
        try {
            return getStockForSize(size) > 0;
        } catch (Exception e) {
            System.err.println("Error checking size availability for " + size + ": " + e.getMessage());
            return false;
        }
    }

    public List<String> getAvailableSizes() {
        List<String> availableSizes = new ArrayList<>();
        
        try {
            List<SizeInfo> sizeList = getSizesAsList();
            
            if (sizeList != null) {
                for (SizeInfo sizeInfo : sizeList) {
                    if (sizeInfo != null && sizeInfo.getStock() > 0) {
                        availableSizes.add(sizeInfo.getSize());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting available sizes: " + e.getMessage());
        }
        
        return availableSizes;
    }

    public static class SizeInfo {
        private String size;
        private String height;
        private String width;
        private int stock;

        // Default constructor required for Firestore
        public SizeInfo() {}

        public SizeInfo(String size, String height, String width) {
            this.size = size;
            this.height = height;
            this.width = width;
            this.stock = 0;
        }

        public SizeInfo(String size, String height, String width, int stock) {
            this.size = size;
            this.height = height;
            this.width = width;
            this.stock = stock;
        }

        // Getters and Setters
        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }
    }
}




