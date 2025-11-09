package com.aurafit.AuraFitApp.ui.ar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.aurafit.AuraFitApp.data.model.Outfit;
import com.aurafit.AuraFitApp.data.model.OutfitItem;

import java.util.ArrayList;
import java.util.List;


public class ArOverlayView extends View {
    
    private Outfit outfit;
    private List<PointF> bodyLandmarks;
    private boolean isOutfitVisible = false;
    private Paint clothingPaint;
    private Paint landmarkPaint;
    private Paint skeletonPaint;
    private ArModelLoader modelLoader;
    private boolean showSkeleton = true;
    private boolean isSkeletonAdjustmentEnabled = false;
    
    
 
    public void toggleSkeleton() {
        showSkeleton = !showSkeleton;
        invalidate();
    }

    public void setShowSkeleton(boolean show) {
        showSkeleton = show;
        invalidate();
    }
    
    private float alignmentOffsetX = -220;  
    private float alignmentOffsetY = 15;   
    private float alignmentScaleX = 3.5f;  
    private float alignmentScaleY = 3.2f;  
    
    private float clothingScaleX = 2.5f;
    private float clothingScaleY = 2.5f;
    
    private float pantsScaleX = 3.5f;
    private float pantsScaleY = 3.5f;
    
    private float legScaleY = 1.2f;
    
    public void adjustAlignment(float offsetX, float offsetY, float scaleX, float scaleY) {
        alignmentOffsetX = offsetX;
        alignmentOffsetY = offsetY;
        alignmentScaleX = scaleX;
        alignmentScaleY = scaleY;
        
        
        
        
        
        invalidate();
    }

    public void fineTuneAlignment(float deltaX, float deltaY, float deltaScaleX, float deltaScaleY) {
        alignmentOffsetX += deltaX;
        alignmentOffsetY += deltaY;
        alignmentScaleX += deltaScaleX;
        alignmentScaleY += deltaScaleY;
        
        System.out.println("ArOverlayView: Fine-tuned alignment - offsetX=" + alignmentOffsetX + 
                          ", offsetY=" + alignmentOffsetY + ", scaleX=" + alignmentScaleX + 
                          ", scaleY=" + alignmentScaleY);
        invalidate();
    }

    public String getCurrentAlignment() {
        return "offsetX=" + alignmentOffsetX + ", offsetY=" + alignmentOffsetY + 
               ", scaleX=" + alignmentScaleX + ", scaleY=" + alignmentScaleY;
    }

    public void makeTorsoWider(float widthMultiplier) {
        alignmentScaleX = alignmentScaleX * widthMultiplier;
        System.out.println("ArOverlayView: Torso made wider, new scaleX: " + alignmentScaleX);
        invalidate();
    }

    public void adjustClothingSize(float scaleX, float scaleY) {
        clothingScaleX = scaleX;
        clothingScaleY = scaleY;
        System.out.println("ArOverlayView: Clothing size adjusted - scaleX: " + clothingScaleX + ", scaleY: " + clothingScaleY);
        invalidate();
    }

    public void adjustPantsSize(float scaleX, float scaleY) {
        pantsScaleX = scaleX;
        pantsScaleY = scaleY;
        System.out.println("ArOverlayView: Pants size adjusted - scaleX: " + pantsScaleX + ", scaleY: " + pantsScaleY);
        invalidate();
    }

    public void makeLegsLonger(float lengthMultiplier) {
        legScaleY = legScaleY * lengthMultiplier;
        System.out.println("ArOverlayView: Legs made longer, new legScaleY: " + legScaleY);
        invalidate();
    }

    public void calibrateItemPositioning(OutfitItem item, float deltaX, float deltaY, float deltaZ,
                                        float deltaScale, float deltaRotX, float deltaRotY, float deltaRotZ) {
        ArPositioningCalibrator.calibrateItem(item, deltaX, deltaY, deltaZ, deltaScale, deltaRotX, deltaRotY, deltaRotZ);
        invalidate();
    }

    public void calibrateItemSizing(OutfitItem item, float deltaWidth, float deltaHeight, float deltaDepth) {
        ArPositioningCalibrator.calibrateSizing(item, deltaWidth, deltaHeight, deltaDepth);
        invalidate();
    }

    public ArPositioningConfig.PositioningData getItemPositioning(OutfitItem item) {
        return ArPositioningCalibrator.getCurrentPositioning(item);
    }

    public void resetItemPositioning(OutfitItem item) {
        ArPositioningCalibrator.resetItemPositioning(item);
        invalidate();
    }

    public void logItemPositioning(OutfitItem item) {
        ArPositioningCalibrator.logCurrentPositioning(item);
    }

    public void enableSkeletonAdjustment() {
        isSkeletonAdjustmentEnabled = true;
        System.out.println("ArOverlayView: Skeleton adjustment enabled");
        invalidate();
    }
    
    public void disableSkeletonAdjustment() {
        isSkeletonAdjustmentEnabled = false;
        System.out.println("ArOverlayView: Skeleton adjustment disabled");
        invalidate();
    }
    
    public void applySkeletonAdjustment() {
        if (isSkeletonAdjustmentEnabled) {
            SkeletonPositionAdjuster.SkeletonAdjustment currentAdjustment = SkeletonPositionAdjuster.getCurrentAdjustment();
            
            
            alignmentOffsetX = currentAdjustment.offsetX;
            alignmentOffsetY = currentAdjustment.offsetY;
            alignmentScaleX = currentAdjustment.scaleX;
            alignmentScaleY = currentAdjustment.scaleY;
            
            System.out.println("ArOverlayView: Applied skeleton adjustment - " + currentAdjustment.toString());
            invalidate();
        }
    }
    
    public boolean isSkeletonAdjustmentEnabled() {
        return isSkeletonAdjustmentEnabled;
    }
    
    public void testPositioningChanges() {
        if (outfit != null && outfit.getItems() != null) {
            for (OutfitItem item : outfit.getItems()) {
                if ("top".equals(item.getCategory().toLowerCase())) {
                    ArPositioningCalibrator.calibrateItem(item, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
                } else if ("bottom".equals(item.getCategory().toLowerCase())) {
                    ArPositioningCalibrator.calibrateItem(item, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
                }
            }
            invalidate();
        }
    }
    
    public void forcePositioningTest() {
        if (outfit != null && outfit.getItems() != null) {
            for (OutfitItem item : outfit.getItems()) {
                if ("top".equals(item.getCategory().toLowerCase())) {
                    ArPositioningCalibrator.calibrateItem(item, 0.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
                }
            }
            invalidate();
        }
    }
    
    
    public ArOverlayView(Context context) {
        super(context);
        init();
    }
    
    public ArOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        clothingPaint = new Paint();
        clothingPaint.setAntiAlias(true);
        clothingPaint.setStyle(Paint.Style.FILL);
        
        landmarkPaint = new Paint();
        landmarkPaint.setColor(Color.GREEN);
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setAntiAlias(true);
        
        skeletonPaint = new Paint();
        skeletonPaint.setColor(Color.parseColor("#00FF00")); 
        skeletonPaint.setStyle(Paint.Style.STROKE);
        skeletonPaint.setStrokeWidth(4f);
        skeletonPaint.setAntiAlias(true);
        skeletonPaint.setStrokeCap(Paint.Cap.ROUND);
        
        modelLoader = new ArModelLoader(getContext());
        
        
        SkeletonPositionAdjuster.initialize(getContext());
        
        
        modelLoader.listAvailableAssets("ar_models/clothing");
    }
    
    public void setOutfit(Outfit outfit) {
        this.outfit = outfit;
        System.out.println("ArOverlayView: Outfit set with " + (outfit != null && outfit.getItems() != null ? outfit.getItems().size() : 0) + " items");
        if (outfit != null && outfit.getItems() != null) {
            for (OutfitItem item : outfit.getItems()) {
                System.out.println("ArOverlayView: Outfit item: " + item.getName() + " (" + item.getCategory() + ")");
            }
        }
        invalidate();
    }
    
    
    public void updateBodyLandmarks(List<PointF> landmarks) {
        this.bodyLandmarks = landmarks;
        invalidate();
    }
    
    
    public void setOutfitVisible(boolean visible) {
        this.isOutfitVisible = visible;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        try {
            
            if (isSkeletonAdjustmentEnabled) {
                applySkeletonAdjustment();
            }
            
            
            if (showSkeleton && bodyLandmarks != null && bodyLandmarks.size() >= 6) {
                drawSkeleton(canvas);
            }
            
            
            
            
            
            if (isOutfitVisible && outfit != null && bodyLandmarks != null && bodyLandmarks.size() >= 6) {
                drawOutfit(canvas);
                
                showSkeleton = false;
            } else {
                
                showSkeleton = true;
            }
            
        } catch (Exception e) {
            System.out.println("ArOverlayView: Error in onDraw: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void drawOutfit(Canvas canvas) {
        if (canvas == null || outfit == null || outfit.getItems() == null) {
            System.out.println("ArOverlayView: Cannot draw outfit - canvas, outfit, or items null");
            return;
        }
        
        System.out.println("ArOverlayView: Drawing outfit with " + outfit.getItems().size() + " items");
        
        
        
        
        for (OutfitItem item : outfit.getItems()) {
            if (item == null) continue;
            if (item.getCategory() != null) {
                String category = item.getCategory().toLowerCase();
                if (category.equals("bottom") || category.equals("bottoms")) {
                    System.out.println("ArOverlayView: Drawing bottom layer: " + item.getName());
                        drawBottomClothing(canvas, item);
                }
            }
        }
        
        
        for (OutfitItem item : outfit.getItems()) {
            if (item == null) continue;
            if (item.getCategory() != null) {
                String category = item.getCategory().toLowerCase();
                if (category.equals("shoes")) {
                    System.out.println("ArOverlayView: Drawing shoes layer: " + item.getName());
                        drawShoes(canvas, item);
                }
            }
        }
        
        
        for (OutfitItem item : outfit.getItems()) {
            if (item == null) continue;
            if (item.getCategory() != null) {
                String category = item.getCategory().toLowerCase();
                if (category.equals("top") || category.equals("tops")) {
                    System.out.println("ArOverlayView: Drawing top layer: " + item.getName());
                    drawTopClothing(canvas, item);
                }
            }
        }
        
        
        for (OutfitItem item : outfit.getItems()) {
            if (item == null) continue;
            if (item.getCategory() != null) {
                String category = item.getCategory().toLowerCase();
                if (category.equals("hat") || category.equals("cap") || 
                    category.equals("glasses") || category.equals("eyewear") || 
                    category.equals("accessories")) {
                    System.out.println("ArOverlayView: Drawing accessory layer: " + item.getName());
                        drawAccessory(canvas, item);
                }
            }
        }
    }
    
    private void drawTopClothing(Canvas canvas, OutfitItem item) {
        
        ArPositioningConfig.PositioningData positioning = ArPositioningConfig.getPositioningData("top", item.getName());
        
        
        PointF leftShoulder = bodyLandmarks.get(3);   
        PointF rightShoulder = bodyLandmarks.get(4);  
        PointF leftHip = bodyLandmarks.get(9);        
        PointF rightHip = bodyLandmarks.get(10);     
        
        
        float offsetX = alignmentOffsetX;
        float offsetY = alignmentOffsetY;
        float scaleX = alignmentScaleX;
        float scaleY = alignmentScaleY;
        
        
        float leftShoulderX = (leftShoulder.x * scaleX) + offsetX;
        float leftShoulderY = (leftShoulder.y * scaleY) + offsetY;
        float rightShoulderX = (rightShoulder.x * scaleX) + offsetX;
        float rightShoulderY = (rightShoulder.y * scaleY) + offsetY;
        float leftHipX = (leftHip.x * scaleX) + offsetX;
        float leftHipY = (leftHip.y * scaleY) + offsetY;
        float rightHipX = (rightHip.x * scaleX) + offsetX;
        float rightHipY = (rightHip.y * scaleY) + offsetY;
        
        
        float positionX = positioning.x * 50; 
        float positionY = positioning.y * 50;
        float positionZ = positioning.z * 50;
        
        
        leftShoulderX += positionX;
        leftShoulderY += positionY;
        rightShoulderX += positionX;
        rightShoulderY += positionY;
        leftHipX += positionX;
        leftHipY += positionY;
        rightHipX += positionX;
        rightHipY += positionY;
        
        
        float widthScale = positioning.widthScale;
        float heightScale = positioning.heightScale;
        float depthScale = positioning.depthScale;
        
        
        float shoulderWidth = Math.abs(rightShoulderX - leftShoulderX) * clothingScaleX * widthScale;
        float torsoHeight = Math.abs(leftHipY - leftShoulderY) * clothingScaleY * heightScale;
        
        
        float expandX = shoulderWidth * 0.2f + positionX;
        float expandY = torsoHeight * 0.1f; 
        
        
        PointF topLeft = new PointF(leftShoulderX - expandX, leftShoulderY - expandY);
        PointF topRight = new PointF(rightShoulderX + expandX, rightShoulderY - expandY);
        PointF bottomLeft = new PointF(leftHipX - expandX * 0.5f, leftHipY);
        PointF bottomRight = new PointF(rightHipX + expandX * 0.5f, rightHipY);
        
        
        clothingPaint.setColor(getColorForItem(item));
        clothingPaint.setAlpha(200); 
        
        
        System.out.println("ArOverlayView: Rendering 2D clothing overlay for top with positioning: " + positioning.toString());
        System.out.println("ArOverlayView: Applied position offsets - X: " + positionX + ", Y: " + positionY + ", Z: " + positionZ);
        System.out.println("ArOverlayView: Applied sizing - Width: " + widthScale + ", Height: " + heightScale + ", Depth: " + depthScale);
        System.out.println("ArOverlayView: Final coordinates - topLeft: (" + topLeft.x + ", " + topLeft.y + "), topRight: (" + topRight.x + ", " + topRight.y + ")");
        System.out.println("ArOverlayView: Final coordinates - bottomLeft: (" + bottomLeft.x + ", " + bottomLeft.y + "), bottomRight: (" + bottomRight.x + ", " + bottomRight.y + ")");
        modelLoader.renderImageOverlay(canvas, item.getArModelUrl(), topLeft, topRight, bottomLeft, bottomRight, clothingPaint);
    }
    
    private void drawBottomClothing(Canvas canvas, OutfitItem item) {
        
        ArPositioningConfig.PositioningData positioning = ArPositioningConfig.getPositioningData("bottom", item.getName());
        
        
        PointF leftHip = bodyLandmarks.get(9);        
        PointF rightHip = bodyLandmarks.get(10);       
        PointF leftAnkle = bodyLandmarks.get(13);      
        PointF rightAnkle = bodyLandmarks.get(14);     
        
        
        float offsetX = alignmentOffsetX;
        float offsetY = alignmentOffsetY;
        float scaleX = alignmentScaleX;
        float scaleY = alignmentScaleY;
        
        
        float leftHipX = (leftHip.x * scaleX) + offsetX;
        float leftHipY = (leftHip.y * scaleY) + offsetY;
        float rightHipX = (rightHip.x * scaleX) + offsetX;
        float rightHipY = (rightHip.y * scaleY) + offsetY;
        
        
        float legScaleY = alignmentScaleY * this.legScaleY;
        float leftAnkleX = (leftAnkle.x * scaleX) + offsetX;
        float leftAnkleY = (leftAnkle.y * legScaleY) + offsetY;
        float rightAnkleX = (rightAnkle.x * scaleX) + offsetX;
        float rightAnkleY = (rightAnkle.y * legScaleY) + offsetY;
        
        
        float positionX = positioning.x * 50; 
        float positionY = positioning.y * 50;
        float positionZ = positioning.z * 50;
        
        
        leftHipX += positionX;
        leftHipY += positionY;
        rightHipX += positionX;
        rightHipY += positionY;
        leftAnkleX += positionX;
        leftAnkleY += positionY;
        rightAnkleX += positionX;
        rightAnkleY += positionY;
        
        
        float widthScale = positioning.widthScale;
        float heightScale = positioning.heightScale;
        float depthScale = positioning.depthScale;
        
        
        float hipWidth = Math.abs(rightHipX - leftHipX) * clothingScaleX * pantsScaleX * widthScale;
        float legHeight = Math.abs(leftAnkleY - leftHipY) * clothingScaleY * pantsScaleY * heightScale;
        
        
        float expandX = hipWidth * 0.15f + positionX;
        
        
        PointF topLeft = new PointF(leftHipX - expandX, leftHipY);
        PointF topRight = new PointF(rightHipX + expandX, rightHipY);
        PointF bottomLeft = new PointF(leftAnkleX - expandX * 0.8f, leftAnkleY);
        PointF bottomRight = new PointF(rightAnkleX + expandX * 0.8f, rightAnkleY);
        
        
        clothingPaint.setColor(getColorForItem(item));
        clothingPaint.setAlpha(200);
        
        
        System.out.println("ArOverlayView: Rendering 2D clothing overlay for bottom with positioning: " + positioning.toString());
        modelLoader.renderImageOverlay(canvas, item.getArModelUrl(), topLeft, topRight, bottomLeft, bottomRight, clothingPaint);
    }
    
    private void drawShoes(Canvas canvas, OutfitItem item) {
        if (bodyLandmarks.size() < 6) return;
        
        
        ArPositioningConfig.PositioningData positioning = ArPositioningConfig.getPositioningData("shoes", item.getName());
        
        PointF leftAnkle = bodyLandmarks.get(13);      
        PointF rightAnkle = bodyLandmarks.get(14);     
        
        
        float offsetX = alignmentOffsetX;
        float offsetY = alignmentOffsetY;
        float scaleX = alignmentScaleX;
        float scaleY = alignmentScaleY;
        
        
        float legScaleY = alignmentScaleY * this.legScaleY;
        float leftAnkleX = (leftAnkle.x * scaleX) + offsetX;
        float leftAnkleY = (leftAnkle.y * legScaleY) + offsetY;
        float rightAnkleX = (rightAnkle.x * scaleX) + offsetX;
        float rightAnkleY = (rightAnkle.y * legScaleY) + offsetY;
        
        
        float positionX = positioning.x * 50; 
        float positionY = positioning.y * 50;
        float positionZ = positioning.z * 50;
        
        
        float widthScale = positioning.widthScale;
        float heightScale = positioning.heightScale;
        float depthScale = positioning.depthScale;
        
        
        float ankleDistance = Math.abs(rightAnkleX - leftAnkleX);
        float shoeSize = Math.max(ankleDistance * 0.8f, 40f) * positioning.scale * widthScale; 
        float shoeHeight = shoeSize * heightScale * 0.6f; 
        
        
        leftAnkleX += positionX;
        leftAnkleY += positionY - 150; 
        rightAnkleX += positionX;
        rightAnkleY += positionY - 150; 
        
        
        float centerX = (leftAnkleX + rightAnkleX) / 2;
        float centerY = (leftAnkleY + rightAnkleY) / 2;
        
        
        PointF topLeft = new PointF(centerX - shoeSize/2, centerY - shoeHeight/2);
        PointF topRight = new PointF(centerX + shoeSize/2, centerY - shoeHeight/2);
        PointF bottomLeft = new PointF(centerX - shoeSize/2, centerY + shoeHeight/2);
        PointF bottomRight = new PointF(centerX + shoeSize/2, centerY + shoeHeight/2);
        
        
        modelLoader.renderImageOverlay(canvas, item.getArModelUrl(), topLeft, topRight, bottomLeft, bottomRight, clothingPaint);
        
        System.out.println("ArOverlayView: Rendering shoes with positioning: " + positioning.toString());
    }
    
  
    private void drawAccessory(Canvas canvas, OutfitItem item) {
        if (bodyLandmarks.size() < 2) return;
        
        
        ArPositioningConfig.PositioningData positioning = ArPositioningConfig.getPositioningData("accessories", item.getName());
        
        
        String itemName = item.getName().toLowerCase();
        
        if (itemName.contains("bag") || itemName.contains("handbag") || itemName.contains("dior")) {
            
            drawBagOnHand(canvas, item, positioning);
        } else if (itemName.contains("cap") || itemName.contains("adidas") || 
                   itemName.contains("gucci") || itemName.contains("hermes")) {
            
            drawHatOnHead(canvas, item, positioning);
        } else if (itemName.contains("glasses")) {
            
            drawGlassesOnFace(canvas, item, positioning);
        } else {
            
            drawDefaultAccessory(canvas, item, positioning);
        }
    }
    
  
    private void drawBagOnHand(Canvas canvas, OutfitItem item, ArPositioningConfig.PositioningData positioning) {
        if (bodyLandmarks.size() < 8) return;
        
        PointF leftWrist = bodyLandmarks.get(7);     
        
        
        float offsetX = alignmentOffsetX;
        float offsetY = alignmentOffsetY;
        float scaleX = alignmentScaleX;
        float scaleY = alignmentScaleY;
        
        
        float leftWristX = (leftWrist.x * scaleX) + offsetX;
        float leftWristY = (leftWrist.y * scaleY) + offsetY;
        
        
        float positionX = positioning.x * 100;
        float positionY = positioning.y * 100;
        float positionZ = positioning.z * 100;
        
        
        float widthScale = positioning.widthScale;
        float heightScale = positioning.heightScale;
        float depthScale = positioning.depthScale;
        
        
        float bagSize = 35f * positioning.scale * widthScale; 
        float bagHeight = bagSize * heightScale * 1.2f; 
        
        
        leftWristX += positionX;
        leftWristY += positionY;
        
        
        PointF topLeft = new PointF(leftWristX - bagSize/2, leftWristY - bagHeight/2);
        PointF topRight = new PointF(leftWristX + bagSize/2, leftWristY - bagHeight/2);
        PointF bottomLeft = new PointF(leftWristX - bagSize/2, leftWristY + bagHeight/2);
        PointF bottomRight = new PointF(leftWristX + bagSize/2, leftWristY + bagHeight/2);
        
        
        modelLoader.renderImageOverlay(canvas, item.getArModelUrl(), topLeft, topRight, bottomLeft, bottomRight, clothingPaint);
        
        System.out.println("ArOverlayView: Rendering bag on hand with positioning: " + positioning.toString());
    }
    
  
    private void drawHatOnHead(Canvas canvas, OutfitItem item, ArPositioningConfig.PositioningData positioning) {
        if (bodyLandmarks.size() < 3) return;
        
        PointF nose = bodyLandmarks.get(0);
        PointF leftEar = bodyLandmarks.get(1);
        PointF rightEar = bodyLandmarks.get(2);
        
        
        float offsetX = alignmentOffsetX;
        float offsetY = alignmentOffsetY;
        float scaleX = alignmentScaleX;
        float scaleY = alignmentScaleY;
        
        
        float noseX = (nose.x * scaleX) + offsetX;
        float noseY = (nose.y * scaleY) + offsetY;
        float leftEarX = (leftEar.x * scaleX) + offsetX;
        float leftEarY = (leftEar.y * scaleY) + offsetY;
        float rightEarX = (rightEar.x * scaleX) + offsetX;
        float rightEarY = (rightEar.y * scaleY) + offsetY;
        
        
        float positionX = positioning.x * 100;
        float positionY = positioning.y * 100;
        float positionZ = positioning.z * 100;
        
        
        float widthScale = positioning.widthScale;
        float heightScale = positioning.heightScale;
        float depthScale = positioning.depthScale;
        
        
        float hatWidth = Math.abs(rightEarX - leftEarX) * positioning.scale * widthScale;
        float hatHeight = hatWidth; 
        
        
        float hatY = Math.min(leftEarY, rightEarY) - 70 + positionY; 
        float hatX = noseX + positionX - 20; 
        
        
        PointF topLeft = new PointF(hatX - hatWidth/2, hatY - hatHeight/2);
        PointF topRight = new PointF(hatX + hatWidth/2, hatY - hatHeight/2);
        PointF bottomLeft = new PointF(hatX - hatWidth/2, hatY + hatHeight/2);
        PointF bottomRight = new PointF(hatX + hatWidth/2, hatY + hatHeight/2);
        
        
        modelLoader.renderImageOverlay(canvas, item.getArModelUrl(), topLeft, topRight, bottomLeft, bottomRight, clothingPaint);
        
        System.out.println("ArOverlayView: Rendering hat on head with positioning: " + positioning.toString());
    }
    

    private void drawGlassesOnFace(Canvas canvas, OutfitItem item, ArPositioningConfig.PositioningData positioning) {
        if (bodyLandmarks.size() < 3) return;
        
        PointF nose = bodyLandmarks.get(0);
        PointF leftEar = bodyLandmarks.get(1);
        PointF rightEar = bodyLandmarks.get(2);
        
        
        float offsetX = alignmentOffsetX;
        float offsetY = alignmentOffsetY;
        float scaleX = alignmentScaleX;
        float scaleY = alignmentScaleY;
        
        
        float noseX = (nose.x * scaleX) + offsetX;
        float noseY = (nose.y * scaleY) + offsetY;
        float leftEarX = (leftEar.x * scaleX) + offsetX;
        float leftEarY = (leftEar.y * scaleY) + offsetY;
        float rightEarX = (rightEar.x * scaleX) + offsetX;
        float rightEarY = (rightEar.y * scaleY) + offsetY;
        
        
        float positionX = positioning.x * 100;
        float positionY = positioning.y * 100;
        float positionZ = positioning.z * 100;
        
        
        float widthScale = positioning.widthScale;
        float heightScale = positioning.heightScale;
        float depthScale = positioning.depthScale;
        
        
        float glassesWidth = Math.abs(rightEarX - leftEarX) * positioning.scale * widthScale;
        float glassesHeight = 20f * positioning.scale * heightScale;
        
        
        float glassesY = noseY - 10 + positionY;
        float glassesX = noseX + positionX;
        
        
        PointF topLeft = new PointF(glassesX - glassesWidth/2, glassesY - glassesHeight/2);
        PointF topRight = new PointF(glassesX + glassesWidth/2, glassesY - glassesHeight/2);
        PointF bottomLeft = new PointF(glassesX - glassesWidth/2, glassesY + glassesHeight/2);
        PointF bottomRight = new PointF(glassesX + glassesWidth/2, glassesY + glassesHeight/2);
        
        
        modelLoader.renderImageOverlay(canvas, item.getArModelUrl(), topLeft, topRight, bottomLeft, bottomRight, clothingPaint);
        
        System.out.println("ArOverlayView: Rendering glasses on face with positioning: " + positioning.toString());
    }

    private void drawDefaultAccessory(Canvas canvas, OutfitItem item, ArPositioningConfig.PositioningData positioning) {
        if (bodyLandmarks.size() < 3) return;
        
        PointF leftShoulder = bodyLandmarks.get(3);
        
        
        float offsetX = alignmentOffsetX;
        float offsetY = alignmentOffsetY;
        float scaleX = alignmentScaleX;
        float scaleY = alignmentScaleY;
        
        
        float leftShoulderX = (leftShoulder.x * scaleX) + offsetX;
        float leftShoulderY = (leftShoulder.y * scaleY) + offsetY;
        
        
        float positionX = positioning.x * 100;
        float positionY = positioning.y * 100;
        
        
        leftShoulderX += positionX;
        leftShoulderY += positionY;
        
        clothingPaint.setColor(Color.YELLOW);
        clothingPaint.setAlpha(180);
        
        
        canvas.drawCircle(leftShoulderX - 30, leftShoulderY + 20, 15, clothingPaint);
        
        System.out.println("ArOverlayView: Rendering default accessory with positioning: " + positioning.toString());
    }
  
    private int getColorForItem(OutfitItem item) {
        
        if (item.getSubcategory() != null) {
            String sub = item.getSubcategory().toLowerCase();
            if (sub.contains("shirt") || sub.contains("tshirt")) {
                return Color.parseColor("#4A90E2"); 
            } else if (sub.contains("pants") || sub.contains("jeans")) {
                return Color.parseColor("#1E3A5F"); 
            } else if (sub.contains("shorts")) {
                return Color.parseColor("#8B7355"); 
            }
        }
        
        
        switch (item.getCategory().toLowerCase()) {
            case "top":
            case "tops":
                return Color.parseColor("#E74C3C"); 
            case "bottom":
            case "bottoms":
                return Color.parseColor("#2C3E50"); 
            case "shoes":
                return Color.parseColor("#34495E"); 
            default:
                return Color.parseColor("#95A5A6"); 
        }
    }
    
        
        
        
    private void drawSkeleton(Canvas canvas) {
        if (canvas == null || bodyLandmarks == null || bodyLandmarks.size() < 15) {
            System.out.println("ArOverlayView: Cannot draw skeleton - canvas or landmarks null, or insufficient landmarks");
            return;
        }
        
        
        int screenHeight = getHeight();
        int screenWidth = getWidth();
        
        
        
        PointF nose = bodyLandmarks.get(0);
        PointF leftEar = bodyLandmarks.get(1);
        PointF rightEar = bodyLandmarks.get(2);
        
        
        leftEar = new PointF(leftEar.x - 5, leftEar.y); 
        rightEar = new PointF(rightEar.x + 5, rightEar.y); 
        
        
        PointF leftEye = bodyLandmarks.size() > 15 ? bodyLandmarks.get(15) : new PointF(nose.x - 25, nose.y - 15);
        PointF rightEye = bodyLandmarks.size() > 16 ? bodyLandmarks.get(16) : new PointF(nose.x + 25, nose.y - 15);
        PointF leftEyebrow = bodyLandmarks.size() > 17 ? bodyLandmarks.get(17) : new PointF(nose.x - 30, nose.y - 25);
        PointF rightEyebrow = bodyLandmarks.size() > 18 ? bodyLandmarks.get(18) : new PointF(nose.x + 30, nose.y - 25);
        PointF leftShoulder = bodyLandmarks.get(3);
        PointF rightShoulder = bodyLandmarks.get(4);
        PointF leftElbow = bodyLandmarks.get(5);
        PointF rightElbow = bodyLandmarks.get(6);
        PointF leftWrist = bodyLandmarks.get(7);
        PointF rightWrist = bodyLandmarks.get(8);
        PointF leftHip = bodyLandmarks.get(9);
        PointF rightHip = bodyLandmarks.get(10);
        PointF leftKnee = bodyLandmarks.get(11);
        PointF rightKnee = bodyLandmarks.get(12);
        PointF leftAnkle = bodyLandmarks.get(13);
        PointF rightAnkle = bodyLandmarks.get(14);
        
        
        float offsetX = alignmentOffsetX;
        float offsetY = alignmentOffsetY;
        float scaleX = alignmentScaleX;
        float scaleY = alignmentScaleY;
        
        
        
        
        
        
        
        float headCenterX = (nose.x * scaleX) + offsetX;
        float headCenterY = (nose.y * scaleY) + offsetY;
        float headRadius = 60; 
        
        
        PointF topOfHead = new PointF(nose.x, nose.y - 50); 
        PointF mouth = bodyLandmarks.size() > 9 ? bodyLandmarks.get(9) : new PointF(nose.x, nose.y + 20);
        
        
        canvas.drawCircle(headCenterX, headCenterY, headRadius, skeletonPaint);
        
        
        canvas.drawLine(
            (topOfHead.x * scaleX) + offsetX, (topOfHead.y * scaleY) + offsetY,
            (nose.x * scaleX) + offsetX, (nose.y * scaleY) + offsetY, skeletonPaint);
        canvas.drawLine(
            (nose.x * scaleX) + offsetX, (nose.y * scaleY) + offsetY,
            (mouth.x * scaleX) + offsetX, (mouth.y * scaleY) + offsetY, skeletonPaint);
        
        
        canvas.drawLine(
            (leftEar.x * scaleX) + offsetX, (leftEar.y * scaleY) + offsetY,
            (rightEar.x * scaleX) + offsetX, (rightEar.y * scaleY) + offsetY, skeletonPaint);
        canvas.drawLine(
            (leftEye.x * scaleX) + offsetX, (leftEye.y * scaleY) + offsetY,
            (rightEye.x * scaleX) + offsetX, (rightEye.y * scaleY) + offsetY, skeletonPaint);
        
        
        canvas.drawLine(
            (leftEye.x * scaleX) + offsetX, (leftEye.y * scaleY) + offsetY,
            (nose.x * scaleX) + offsetX, (nose.y * scaleY) + offsetY, skeletonPaint);
        canvas.drawLine(
            (rightEye.x * scaleX) + offsetX, (rightEye.y * scaleY) + offsetY,
            (nose.x * scaleX) + offsetX, (nose.y * scaleY) + offsetY, skeletonPaint);
        
        
        
        canvas.drawLine(
            (leftShoulder.x * scaleX) + offsetX, (leftShoulder.y * scaleY) + offsetY,
            (rightShoulder.x * scaleX) + offsetX, (rightShoulder.y * scaleY) + offsetY, skeletonPaint);
        
        
        canvas.drawLine(
            (leftShoulder.x * scaleX) + offsetX, (leftShoulder.y * scaleY) + offsetY,
            (leftHip.x * scaleX) + offsetX, (leftHip.y * scaleY) + offsetY, skeletonPaint);
        
        
        canvas.drawLine(
            (rightShoulder.x * scaleX) + offsetX, (rightShoulder.y * scaleY) + offsetY,
            (rightHip.x * scaleX) + offsetX, (rightHip.y * scaleY) + offsetY, skeletonPaint);
        
        
        canvas.drawLine(
            (leftHip.x * scaleX) + offsetX, (leftHip.y * scaleY) + offsetY,
            (rightHip.x * scaleX) + offsetX, (rightHip.y * scaleY) + offsetY, skeletonPaint);
        
        
        
        canvas.drawLine(
            (leftShoulder.x * scaleX) + offsetX, (leftShoulder.y * scaleY) + offsetY,
            (leftElbow.x * scaleX) + offsetX, (leftElbow.y * scaleY) + offsetY, skeletonPaint);
        canvas.drawLine(
            (leftElbow.x * scaleX) + offsetX, (leftElbow.y * scaleY) + offsetY,
            (leftWrist.x * scaleX) + offsetX, (leftWrist.y * scaleY) + offsetY, skeletonPaint);
        
        
        canvas.drawLine(
            (rightShoulder.x * scaleX) + offsetX, (rightShoulder.y * scaleY) + offsetY,
            (rightElbow.x * scaleX) + offsetX, (rightElbow.y * scaleY) + offsetY, skeletonPaint);
        canvas.drawLine(
            (rightElbow.x * scaleX) + offsetX, (rightElbow.y * scaleY) + offsetY,
            (rightWrist.x * scaleX) + offsetX, (rightWrist.y * scaleY) + offsetY, skeletonPaint);
        
        
        float leftHipY = (leftHip.y * scaleY) + offsetY;
        float rightHipY = (rightHip.y * scaleY) + offsetY;
        
        
        if (leftHipY < screenHeight * 0.8f && rightHipY < screenHeight * 0.8f) {
            
            float legScaleY = alignmentScaleY * this.legScaleY;
            
            
            float legGapOffset = 25f; 
            
            
            canvas.drawLine(
                (leftHip.x * scaleX) + offsetX - legGapOffset, leftHipY,
                (leftKnee.x * scaleX) + offsetX - legGapOffset, (leftKnee.y * legScaleY) + offsetY, skeletonPaint);
            canvas.drawLine(
                (leftKnee.x * scaleX) + offsetX - legGapOffset, (leftKnee.y * legScaleY) + offsetY,
                (leftAnkle.x * scaleX) + offsetX - legGapOffset, (leftAnkle.y * legScaleY) + offsetY, skeletonPaint);
            
            
            canvas.drawLine(
                (rightHip.x * scaleX) + offsetX + legGapOffset, rightHipY,
                (rightKnee.x * scaleX) + offsetX + legGapOffset, (rightKnee.y * legScaleY) + offsetY, skeletonPaint);
            canvas.drawLine(
                (rightKnee.x * scaleX) + offsetX + legGapOffset, (rightKnee.y * legScaleY) + offsetY,
                (rightAnkle.x * scaleX) + offsetX + legGapOffset, (rightAnkle.y * legScaleY) + offsetY, skeletonPaint);
        }
        
        
        float dotRadius = 12; 
        
        
        landmarkPaint.setColor(Color.parseColor("#00BFFF")); 
        canvas.drawCircle((topOfHead.x * scaleX) + offsetX, (topOfHead.y * scaleY) + offsetY, dotRadius, landmarkPaint);
        
        
        landmarkPaint.setColor(Color.parseColor("#FF0000")); 
        canvas.drawCircle((leftEye.x * scaleX) + offsetX, (leftEye.y * scaleY) + offsetY, dotRadius, landmarkPaint);
        canvas.drawCircle((rightEye.x * scaleX) + offsetX, (rightEye.y * scaleY) + offsetY, dotRadius, landmarkPaint);
        
        
        landmarkPaint.setColor(Color.parseColor("#FFFF00")); 
        canvas.drawCircle((nose.x * scaleX) + offsetX, (nose.y * scaleY) + offsetY, dotRadius, landmarkPaint);
        
        
        landmarkPaint.setColor(Color.parseColor("#FFA500")); 
        canvas.drawCircle((leftEar.x * scaleX) + offsetX, (leftEar.y * scaleY) + offsetY, dotRadius, landmarkPaint);
        canvas.drawCircle((rightEar.x * scaleX) + offsetX, (rightEar.y * scaleY) + offsetY, dotRadius, landmarkPaint);
        
        
        landmarkPaint.setColor(Color.parseColor("#00BFFF")); 
        canvas.drawCircle((mouth.x * scaleX) + offsetX, (mouth.y * scaleY) + offsetY, dotRadius, landmarkPaint);
        
        
        landmarkPaint.setColor(Color.parseColor("#FF8C00")); 
        canvas.drawCircle((leftElbow.x * scaleX) + offsetX, (leftElbow.y * scaleY) + offsetY, 10, landmarkPaint);
        canvas.drawCircle((rightElbow.x * scaleX) + offsetX, (rightElbow.y * scaleY) + offsetY, 10, landmarkPaint);
        canvas.drawCircle((leftWrist.x * scaleX) + offsetX, (leftWrist.y * scaleY) + offsetY, 8, landmarkPaint);
        canvas.drawCircle((rightWrist.x * scaleX) + offsetX, (rightWrist.y * scaleY) + offsetY, 8, landmarkPaint);
        
        
        landmarkPaint.setColor(Color.parseColor("#00FF00")); 
        canvas.drawCircle((leftShoulder.x * scaleX) + offsetX, (leftShoulder.y * scaleY) + offsetY, 10, landmarkPaint);
        canvas.drawCircle((rightShoulder.x * scaleX) + offsetX, (rightShoulder.y * scaleY) + offsetY, 10, landmarkPaint);
        canvas.drawCircle((leftHip.x * scaleX) + offsetX, (leftHip.y * scaleY) + offsetY, 10, landmarkPaint);
        canvas.drawCircle((rightHip.x * scaleX) + offsetX, (rightHip.y * scaleY) + offsetY, 10, landmarkPaint);
        
        
        if (leftHipY < screenHeight * 0.8f && rightHipY < screenHeight * 0.8f) {
            float legScaleY = alignmentScaleY * this.legScaleY;
            float legGapOffset = 25f; 
            landmarkPaint.setColor(Color.parseColor("#8A2BE2")); 
            canvas.drawCircle((leftKnee.x * scaleX) + offsetX - legGapOffset, (leftKnee.y * legScaleY) + offsetY, 10, landmarkPaint);
            canvas.drawCircle((rightKnee.x * scaleX) + offsetX + legGapOffset, (rightKnee.y * legScaleY) + offsetY, 10, landmarkPaint);
            canvas.drawCircle((leftAnkle.x * scaleX) + offsetX - legGapOffset, (leftAnkle.y * legScaleY) + offsetY, 8, landmarkPaint);
            canvas.drawCircle((rightAnkle.x * scaleX) + offsetX + legGapOffset, (rightAnkle.y * legScaleY) + offsetY, 8, landmarkPaint);
        }
        
        
        landmarkPaint.setColor(Color.parseColor("#FFFF00")); 
        float centerX = ((leftShoulder.x + rightShoulder.x) / 2 * scaleX) + offsetX;
        float centerY = ((leftShoulder.y + rightShoulder.y) / 2 * scaleY) + offsetY;
        canvas.drawCircle(centerX, centerY, 15, landmarkPaint);
    }
    
    private void drawBodyLandmarks(Canvas canvas) {
        if (bodyLandmarks == null) return;
        
        for (PointF landmark : bodyLandmarks) {
            canvas.drawCircle(landmark.x, landmark.y, 8, landmarkPaint);
        }
    }
    
}
