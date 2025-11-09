package com.aurafit.AuraFitApp.ui.ar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ArModelLoader {
    
    private Context context;
    private java.util.Map<String, ImageModel> imageCache = new java.util.HashMap<>();
    
    
    public static class ImageModel {
        public Bitmap image;
        public String path;
        
        public ImageModel(Bitmap image, String path) {
            this.image = image;
            this.path = path;
        }
    }
    
    public ArModelLoader(Context context) {
        this.context = context;
    }
    
  
    public void listAvailableAssets(String basePath) {
        try {
            String[] assets = context.getAssets().list(basePath);
            System.out.println("ArModelLoader: Available assets in " + basePath + ":");
            for (String asset : assets) {
                System.out.println("  - " + asset);
            }
        } catch (Exception e) {
            System.out.println("ArModelLoader: Failed to list assets in " + basePath + ": " + e.getMessage());
        }
    }
    

    public ImageModel loadImageModel(String assetPath) {
        
        if (imageCache.containsKey(assetPath)) {
            System.out.println("ArModelLoader: Loading from cache: " + assetPath);
            return imageCache.get(assetPath);
        }
        
        try {
            System.out.println("ArModelLoader: Attempting to load image from: " + assetPath);
            InputStream is = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            
            if (bitmap != null) {
                System.out.println("ArModelLoader: Successfully loaded image: " + assetPath + " (size: " + bitmap.getWidth() + "x" + bitmap.getHeight() + ")");
                ImageModel imageModel = new ImageModel(bitmap, assetPath);
                
                imageCache.put(assetPath, imageModel);
                return imageModel;
            } else {
                System.out.println("ArModelLoader: Failed to decode image: " + assetPath);
                return null;
            }
        } catch (Exception e) {
            System.out.println("ArModelLoader: Failed to load image from " + assetPath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    

    public void preloadImages(String[] imagePaths) {
        System.out.println("ArModelLoader: Preloading " + imagePaths.length + " images...");
        for (String path : imagePaths) {
            if (!imageCache.containsKey(path)) {
                loadImageModel(path);
            }
        }
        System.out.println("ArModelLoader: Preloading complete");
    }

    public void clearCache() {
        imageCache.clear();
        System.out.println("ArModelLoader: Cache cleared");
    }
    
 
    public void renderImageOverlay(Canvas canvas, String imagePath, PointF topLeft, 
                                   PointF topRight, PointF bottomLeft, PointF bottomRight, Paint paint) {
        
        System.out.println("ArModelLoader: Attempting to load image from path: " + imagePath);
        System.out.println("ArModelLoader: Canvas size: " + canvas.getWidth() + "x" + canvas.getHeight());
        System.out.println("ArModelLoader: Drawing area - topLeft: (" + topLeft.x + ", " + topLeft.y + "), bottomRight: (" + bottomRight.x + ", " + bottomRight.y + ")");
        
        ImageModel imageModel = loadImageModel(imagePath);
        
        if (imageModel != null && imageModel.image != null) {
            System.out.println("ArModelLoader: Successfully loaded image from path, drawing it");
            
            drawClothingImage(canvas, imageModel.image, topLeft, topRight, bottomLeft, bottomRight);
        } else {
            System.out.println("ArModelLoader: Image failed to load from path, using colored fallback");
            
            drawColoredOverlay(canvas, "clothing", topLeft, topRight, bottomLeft, bottomRight, paint);
        }
    }
    
    
    
  
    private void drawClothingImage(Canvas canvas, Bitmap image, PointF topLeft, 
                                  PointF topRight, PointF bottomLeft, PointF bottomRight) {
        
        float left = Math.min(topLeft.x, bottomLeft.x);
        float top = Math.min(topLeft.y, topRight.y);
        float right = Math.max(topRight.x, bottomRight.x);
        float bottom = Math.max(bottomLeft.y, bottomRight.y);
        
        
        android.graphics.RectF destRect = new android.graphics.RectF(left, top, right, bottom);
        
        System.out.println("ArModelLoader: Drawing image with destRect: " + destRect.toString());
        System.out.println("ArModelLoader: Image size: " + image.getWidth() + "x" + image.getHeight());
        
        
        Paint imagePaint = new Paint();
        imagePaint.setAlpha(255); 
        canvas.drawBitmap(image, null, destRect, imagePaint);
        
        System.out.println("ArModelLoader: Image drawn successfully");
    }
    

    private void drawColoredOverlay(Canvas canvas, String itemType, PointF topLeft, 
                                   PointF topRight, PointF bottomLeft, PointF bottomRight, Paint paint) {
        
        int color = getColorForItemType(itemType);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255); 
        
        
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(topLeft.x, topLeft.y);
        path.lineTo(topRight.x, topRight.y);
        path.lineTo(bottomRight.x, bottomRight.y);
        path.lineTo(bottomLeft.x, bottomLeft.y);
        path.close();
        
        canvas.drawPath(path, paint);
        
        
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setAlpha(255);
        canvas.drawPath(path, paint);
    }

    private int getColorForItemType(String itemType) {
        switch (itemType.toLowerCase()) {
            case "top":
            case "shirt":
                return 0xFF4A90E2; 
            case "bottom":
            case "pants":
                return 0xFF2C3E50; 
            case "shoes":
                return 0xFF8B4513; 
            case "hat":
            case "cap":
                return 0xFF34495E; 
            case "glasses":
            case "eyewear":
                return 0xFF2C3E50; 
            case "accessories":
                return 0xFFE74C3C; 
            default:
                return 0xFF95A5A6; 
        }
    }
}


