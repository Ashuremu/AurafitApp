package com.aurafit.AuraFitApp.ui.ar;

import android.content.Context;
import android.content.res.AssetManager;

import com.aurafit.AuraFitApp.data.model.Outfit;
import com.aurafit.AuraFitApp.data.model.OutfitItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArResourceManager {
    
    private Context context;
    private AssetManager assetManager;
    private Map<String, JSONObject> arDataCache;
    
    public ArResourceManager(Context context) {
        this.context = context;
        this.assetManager = context.getAssets();
        this.arDataCache = new HashMap<>();
    }

    public Map<String, Float> getClothingPosition(String category, String itemType) {
        try {
            JSONObject clothingData = loadArData("clothing_positions.json");
            JSONObject categoryData = clothingData.getJSONObject("clothing_positions");
            
            if (categoryData.has(category)) {
                JSONObject categoryItems = categoryData.getJSONObject(category);
                if (categoryItems.has(itemType)) {
                    JSONObject itemData = categoryItems.getJSONObject(itemType);
                    JSONObject position = itemData.getJSONObject("position");
                    
                    Map<String, Float> positioning = new HashMap<>();
                    positioning.put("x", (float) position.getDouble("x"));
                    positioning.put("y", (float) position.getDouble("y"));
                    positioning.put("z", (float) position.getDouble("z"));
                    positioning.put("scale", (float) itemData.getDouble("scale"));
                    
                    return positioning;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Return default positioning if not found
        return getDefaultPositioning(category);
    }

    public Map<String, Map<String, Map<String, Float>>> getBodyLandmarks() {
        try {
            JSONObject landmarksData = loadArData("body_landmarks.json");
            JSONObject landmarks = landmarksData.getJSONObject("body_landmarks");
            
            Map<String, Map<String, Map<String, Float>>> result = new HashMap<>();
            
            // Parse each body part
            for (String bodyPart : new String[]{"shoulders", "waist", "hips", "feet", "wrists"}) {
                if (landmarks.has(bodyPart)) {
                    JSONObject partData = landmarks.getJSONObject(bodyPart);
                    Map<String, Map<String, Float>> partMap = new HashMap<>();
                    
                    for (String side : new String[]{"left", "right"}) {
                        if (partData.has(side)) {
                            JSONObject sideData = partData.getJSONObject(side);
                            Map<String, Float> sideMap = new HashMap<>();
                            sideMap.put("x", (float) sideData.getDouble("x"));
                            sideMap.put("y", (float) sideData.getDouble("y"));
                            sideMap.put("z", (float) sideData.getDouble("z"));
                            partMap.put(side, sideMap);
                        }
                    }
                    result.put(bodyPart, partMap);
                }
            }
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultBodyLandmarks();
        }
    }

    public List<OutfitTemplate> getOutfitTemplates() {
        List<OutfitTemplate> templates = new ArrayList<>();
        
        try {
            JSONObject templatesData = loadArData("outfit_templates.json");
            JSONObject templatesObj = templatesData.getJSONObject("outfit_templates");
            
            for (String templateName : new String[]{"casual", "formal", "business_casual", "sporty", "elegant"}) {
                if (templatesObj.has(templateName)) {
                    JSONObject template = templatesObj.getJSONObject(templateName);
                    
                    OutfitTemplate outfitTemplate = new OutfitTemplate();
                    outfitTemplate.name = template.getString("name");
                    outfitTemplate.style = template.getString("style");
                    outfitTemplate.confidence = template.getInt("confidence");
                    
                    JSONArray itemsArray = template.getJSONArray("items");
                    outfitTemplate.items = new ArrayList<>();
                    for (int i = 0; i < itemsArray.length(); i++) {
                        outfitTemplate.items.add(itemsArray.getString(i));
                    }
                    
                    templates.add(outfitTemplate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return templates;
    }

    public String getModelPath(String category, String itemType) {
        return "ar_models/clothing/" + category + "/" + itemType + ".obj";
    }

    public String getTexturePath(String category, String itemType) {
        return "ar_models/clothing/" + category + "/textures/" + itemType + "_texture.jpg";
    }

    public boolean modelExists(String category, String itemType) {
        try {
            String modelPath = getModelPath(category, itemType);
            InputStream inputStream = assetManager.open(modelPath);
            inputStream.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private JSONObject loadArData(String filename) throws JSONException, IOException {
        if (arDataCache.containsKey(filename)) {
            return arDataCache.get(filename);
        }
        
        InputStream inputStream = assetManager.open("ar_data/" + filename);
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();
        
        String jsonString = new String(buffer, "UTF-8");
        JSONObject jsonObject = new JSONObject(jsonString);
        arDataCache.put(filename, jsonObject);
        
        return jsonObject;
    }

    private Map<String, Float> getDefaultPositioning(String category) {
        Map<String, Float> positioning = new HashMap<>();
        
        switch (category.toLowerCase()) {
            case "tops":
            case "shirts":
                positioning.put("x", 0.0f);
                positioning.put("y", -0.2f);
                positioning.put("z", 0.0f);
                positioning.put("scale", 1.0f);
                break;
            case "bottoms":
            case "pants":
                positioning.put("x", 0.0f);
                positioning.put("y", 0.1f);
                positioning.put("z", 0.0f);
                positioning.put("scale", 1.0f);
                break;
            case "shoes":
                positioning.put("x", 0.0f);
                positioning.put("y", 0.4f);
                positioning.put("z", 0.0f);
                positioning.put("scale", 0.8f);
                break;
            case "accessories":
                positioning.put("x", 0.1f);
                positioning.put("y", -0.1f);
                positioning.put("z", 0.0f);
                positioning.put("scale", 0.5f);
                break;
        }
        
        return positioning;
    }

    private Map<String, Map<String, Map<String, Float>>> getDefaultBodyLandmarks() {
        Map<String, Map<String, Map<String, Float>>> landmarks = new HashMap<>();
        
        // Default shoulder positions
        Map<String, Map<String, Float>> shoulders = new HashMap<>();
        Map<String, Float> leftShoulder = new HashMap<>();
        leftShoulder.put("x", 0.3f);
        leftShoulder.put("y", 0.2f);
        leftShoulder.put("z", 0.0f);
        shoulders.put("left", leftShoulder);
        
        Map<String, Float> rightShoulder = new HashMap<>();
        rightShoulder.put("x", 0.7f);
        rightShoulder.put("y", 0.2f);
        rightShoulder.put("z", 0.0f);
        shoulders.put("right", rightShoulder);
        landmarks.put("shoulders", shoulders);
        
        return landmarks;
    }

    public static class OutfitTemplate {
        public String name;
        public String style;
        public int confidence;
        public List<String> items;
    }
}

