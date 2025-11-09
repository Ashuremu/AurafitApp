package com.aurafit.AuraFitApp.ui.generate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.ui.ar.ArOverlayView;
import com.aurafit.AuraFitApp.ui.ar.SkeletonAdjustmentHelper;
import com.aurafit.AuraFitApp.ui.ar.SkeletonPositionAdjuster;
import com.aurafit.AuraFitApp.data.model.Outfit;
import com.aurafit.AuraFitApp.data.model.OutfitItem;
import com.aurafit.AuraFitApp.data.UserDataManager;
import com.aurafit.AuraFitApp.data.model.User;
import com.aurafit.AuraFitApp.ui.wardrobe.WardrobeActivity;

import java.util.ArrayList;
import java.util.List;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GenerateActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int ALL_PERMISSIONS_REQUEST = 103;
    
    
    private PreviewView cameraPreview;
    private ImageView backButton;
    private ImageView captureButton;
    private Button generateOutfitButton;
    private Button saveOutfitButton;
    private LinearLayout cameraControlsContainer;
    private TextView instructionText;
    private ArOverlayView arOverlayView;
    
    
    private LinearLayout detectionIndicator;
    private ImageView detectionStatusIcon;
    private TextView detectionStatusText;
    
    
    private LinearLayout weatherIndicator;
    private ImageView weatherIcon;
    private TextView weatherText;
    private TextView temperatureText;
    
    
    private LinearLayout outfitOptionsContainer;
    private Button outfitOption1Button;
    private Button outfitOption2Button;
    private Button outfitOption3Button;
    
    
    private ImageButton changeCameraButton;
    
    
    private SkeletonAdjustmentHelper skeletonHelper;
    private Button toggleSkeletonButton;
    private LinearLayout skeletonContainer;
    private LinearLayout directionalControls;
    private TextView skeletonStatusText;
    private Button upButton, downButton, leftButton, rightButton, resetButton;
    
    
    private List<Outfit> generatedOutfits;
    
    
    private LinearLayout outfitItemSelectorPanel;
    private Button mixMatchButton;
    private Button tabTop, tabBottom, tabShoes, tabAccessories;
    private LinearLayout itemsContainer;
    private Button tryOnCustomOutfitButton;
    private ImageButton closeSelectorButton;
    private String currentCategory = "Top";
    private OutfitItem selectedTop, selectedBottom, selectedShoes, selectedAccessories;
    private List<OutfitItem> allTops, allBottoms, allShoes, allAccessories;
    
    
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
    
    
    private PoseLandmarker poseLandmarker;
    private boolean isUserDetected = false;
    private int imageWidth = 0;
    private int imageHeight = 0;
    
    
    private OutfitGenerator outfitGenerator;
    private Outfit currentOutfit;
    private List<PointF> bodyLandmarks;
    private boolean isOutfitVisible = false;
    
    
    private WeatherService.WeatherData currentWeather;
    private UserDataManager userDataManager;
    private String userGender = "Unknown";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        
        userDataManager = UserDataManager.getInstance();
        outfitGenerator = new OutfitGenerator();
        initializeBodyLandmarks();
        initializePoseDetector();
        
        initializeViews();
        setupClickListeners();
        initializeWeather();
        initializeSkeletonAdjustment();
        
        
        requestAllPermissions();
    }
    
    private void initializeViews() {
        cameraPreview = findViewById(R.id.cameraPreview);
        backButton = findViewById(R.id.backButton);
        captureButton = findViewById(R.id.captureButton);
        generateOutfitButton = findViewById(R.id.generateOutfitButton);
        saveOutfitButton = findViewById(R.id.saveOutfitButton);
        cameraControlsContainer = findViewById(R.id.cameraControlsContainer);
        instructionText = findViewById(R.id.instructionText);
        arOverlayView = findViewById(R.id.arOverlayView);
        
        
        detectionIndicator = findViewById(R.id.detectionIndicator);
        detectionStatusIcon = findViewById(R.id.detectionStatusIcon);
        detectionStatusText = findViewById(R.id.detectionStatusText);
        
        
        weatherIndicator = findViewById(R.id.weatherIndicator);
        weatherIcon = findViewById(R.id.weatherIcon);
        weatherText = findViewById(R.id.weatherText);
        temperatureText = findViewById(R.id.temperatureText);
        
        
        outfitOptionsContainer = findViewById(R.id.outfitOptionsContainer);
        outfitOption1Button = findViewById(R.id.outfitOption1Button);
        outfitOption2Button = findViewById(R.id.outfitOption2Button);
        outfitOption3Button = findViewById(R.id.outfitOption3Button);
        
        
        changeCameraButton = findViewById(R.id.changeCameraButton);
        
        
        toggleSkeletonButton = findViewById(R.id.toggleSkeletonAdjustmentButton);
        skeletonContainer = findViewById(R.id.skeletonAdjustmentContainer);
        directionalControls = findViewById(R.id.skeletonDirectionalControls);
        skeletonStatusText = findViewById(R.id.skeletonStatusText);
        upButton = findViewById(R.id.skeletonUpButton);
        downButton = findViewById(R.id.skeletonDownButton);
        leftButton = findViewById(R.id.skeletonLeftButton);
        rightButton = findViewById(R.id.skeletonRightButton);
        resetButton = findViewById(R.id.skeletonResetButton);
        
        
        outfitItemSelectorPanel = findViewById(R.id.outfitItemSelectorPanel);
        mixMatchButton = findViewById(R.id.mixMatchButton);
        tabTop = outfitItemSelectorPanel.findViewById(R.id.tabTop);
        tabBottom = outfitItemSelectorPanel.findViewById(R.id.tabBottom);
        tabShoes = outfitItemSelectorPanel.findViewById(R.id.tabShoes);
        tabAccessories = outfitItemSelectorPanel.findViewById(R.id.tabAccessories);
        itemsContainer = outfitItemSelectorPanel.findViewById(R.id.itemsContainer);
        tryOnCustomOutfitButton = outfitItemSelectorPanel.findViewById(R.id.tryOnCustomOutfitButton);
        closeSelectorButton = outfitItemSelectorPanel.findViewById(R.id.closeSelectorButton);
        
        
        allTops = new ArrayList<>();
        allBottoms = new ArrayList<>();
        allShoes = new ArrayList<>();
        allAccessories = new ArrayList<>();
        
    }
    

    private void initializeBodyLandmarks() {
        bodyLandmarks = new ArrayList<>();
        
        
        bodyLandmarks.add(new PointF(200, 150)); 
        bodyLandmarks.add(new PointF(400, 150)); 
        bodyLandmarks.add(new PointF(200, 250)); 
        bodyLandmarks.add(new PointF(400, 250)); 
        bodyLandmarks.add(new PointF(180, 450)); 
        bodyLandmarks.add(new PointF(420, 450)); 
    }
    

    private void initializeWeather() {
        
        currentWeather = WeatherService.getDefaultWeather();
        updateWeatherUI();
        
        
        fetchUserDataAndWeather();
    }
    
  
    private void initializeSkeletonAdjustment() {
        if (arOverlayView != null) {
            
            skeletonHelper = new SkeletonAdjustmentHelper(this, arOverlayView);
            skeletonHelper.initialize();
            
            
            setupSkeletonControls();
        }
    }
    
    /**
     * Setup skeleton adjustment controls
     */
    private void setupSkeletonControls() {
        if (skeletonHelper != null) {
            
            skeletonHelper.setupControls(
                toggleSkeletonButton,
                skeletonContainer,
                directionalControls,
                skeletonStatusText
            );
            
            
            if (upButton != null) {
                upButton.setOnClickListener(v -> {
                    skeletonHelper.moveUp();
                    Toast.makeText(this, "Moved skeleton up", Toast.LENGTH_SHORT).show();
                });
            }
            
            if (downButton != null) {
                downButton.setOnClickListener(v -> {
                    skeletonHelper.moveDown();
                    Toast.makeText(this, "Moved skeleton down", Toast.LENGTH_SHORT).show();
                });
            }
            
            if (leftButton != null) {
                leftButton.setOnClickListener(v -> {
                    skeletonHelper.moveLeft();
                    Toast.makeText(this, "Moved skeleton left", Toast.LENGTH_SHORT).show();
                });
            }
            
            if (rightButton != null) {
                rightButton.setOnClickListener(v -> {
                    skeletonHelper.moveRight();
                    Toast.makeText(this, "Moved skeleton right", Toast.LENGTH_SHORT).show();
                });
            }
            
            if (resetButton != null) {
                resetButton.setOnClickListener(v -> {
                    skeletonHelper.resetToDefault();
                    Toast.makeText(this, "Reset skeleton to default", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
    
    /**
     * Fetch user data and then get weather with gender information
     */
    private void fetchUserDataAndWeather() {
        userDataManager.getCurrentUserProfile(new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getClothingPreference() != null) {
                    userGender = user.getClothingPreference();
                } else {
                    userGender = "Unknown";
                }
                
                
                new android.os.Handler().postDelayed(() -> {
                    WeatherService.getCurrentWeatherWithUserGender(GenerateActivity.this, 0, 0, userGender,
                        new WeatherService.WeatherCallback() {
                            @Override
                            public void onWeatherReceived(WeatherService.WeatherData weatherData) {
                                currentWeather = weatherData;
                                updateWeatherUI();
                            }
                            
                            @Override
                            public void onError(String error) {
                                
                                Toast.makeText(GenerateActivity.this, 
                                    "Weather unavailable: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                }, 2000); 
            }
            
            @Override
            public void onError(String error) {
                
                userGender = "Unknown";
                
                
                new android.os.Handler().postDelayed(() -> {
                    WeatherService.getCurrentWeatherWithUserGender(GenerateActivity.this, 0, 0, userGender,
                        new WeatherService.WeatherCallback() {
                            @Override
                            public void onWeatherReceived(WeatherService.WeatherData weatherData) {
                                currentWeather = weatherData;
                                updateWeatherUI();
                            }
                            
                            @Override
                            public void onError(String error) {
                                Toast.makeText(GenerateActivity.this, 
                                    "Weather unavailable: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                }, 2000);
            }
        });
    }
    
    
    /**
     * Update weather UI with current weather data
     */
    private void updateWeatherUI() {
        if (weatherIcon != null && weatherText != null && temperatureText != null) {
            weatherIcon.setImageResource(currentWeather.weatherIcon);
            weatherIcon.setColorFilter(currentWeather.weatherColor);
            weatherText.setText(currentWeather.condition);
            weatherText.setTextColor(currentWeather.weatherColor);
            temperatureText.setText(String.format("%.0f°C", currentWeather.temperature));
            
            
            if (currentWeather.outfitRecommendation != null && !currentWeather.outfitRecommendation.isEmpty()) {
                instructionText.setText(currentWeather.outfitRecommendation);
            }
        }
    }
    
    /**
     * Initialize MediaPipe Pose Landmarker
     */
    private void initializePoseDetector() {
        try {
            BaseOptions baseOptions = BaseOptions.builder()
                    .setModelAssetPath("models/pose_landmarker_lite.task")
                    .build();
            
            PoseLandmarker.PoseLandmarkerOptions options = PoseLandmarker.PoseLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.LIVE_STREAM)
                    .setResultListener(this::onPoseDetectionResult)
                    .setErrorListener((error) -> {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Pose detection error: " + error.getMessage(), 
                                         Toast.LENGTH_SHORT).show();
                        });
                    })
                    .build();
            
            poseLandmarker = PoseLandmarker.createFromOptions(this, options);
            
        } catch (Exception e) {
            Toast.makeText(this, "Failed to initialize pose detector: " + e.getMessage(), 
                         Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        
        generateOutfitButton.setOnClickListener(v -> {
            if (!isOutfitVisible) {
                generateAndShowOutfit();
            } else {
                hideOutfit();
            }
        });
        
        
        captureButton.setOnClickListener(v -> {
            if (isOutfitVisible && currentOutfit != null) {
                
                takeOutfitPhoto();
            } else {
                
                generateAndShowOutfit();
            }
        });
        
        
        saveOutfitButton.setOnClickListener(v -> {
            if (isOutfitVisible && currentOutfit != null) {
                saveOutfitToWardrobe();
            } else {
                Toast.makeText(this, "Please generate an outfit first", Toast.LENGTH_SHORT).show();
            }
        });
        
        
        outfitOption1Button.setOnClickListener(v -> onOutfitButtonClicked(outfitOption1Button));
        outfitOption2Button.setOnClickListener(v -> onOutfitButtonClicked(outfitOption2Button));
        outfitOption3Button.setOnClickListener(v -> onOutfitButtonClicked(outfitOption3Button));
        
        
        changeCameraButton.setOnClickListener(v -> changeCamera());
        
        
        mixMatchButton.setOnClickListener(v -> showItemSelectorPanel());
        closeSelectorButton.setOnClickListener(v -> hideItemSelectorPanel());
        
        
        tabTop.setOnClickListener(v -> switchCategory("Top"));
        tabBottom.setOnClickListener(v -> switchCategory("Bottom"));
        tabShoes.setOnClickListener(v -> switchCategory("Shoes"));
        tabAccessories.setOnClickListener(v -> switchCategory("Accessories"));
        
        
        tryOnCustomOutfitButton.setOnClickListener(v -> applyCustomOutfit());
        
    }
    
    /**
     * Handle outfit button clicks
     */
    private void onOutfitButtonClicked(Button button) {
        Outfit outfit = (Outfit) button.getTag();
        if (outfit != null) {
            
            currentOutfit = outfit;
            showOutfitOverlay();
            
            
            String message = "Switched to: " + outfit.getName();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            
            
            
            
            
            
        }
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
    }
    
    private boolean checkAllPermissions() {
        return checkCameraPermission() && checkStoragePermission();
    }
    
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    private void requestAllPermissions() {
        if (checkAllPermissions()) {
            
            startCamera();
        } else {
            
            List<String> permissions = new ArrayList<>();

            if (!checkCameraPermission()) {
                permissions.add(Manifest.permission.CAMERA);
            }
            
            if (!checkStoragePermission()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }

            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[0]),
                    ALL_PERMISSIONS_REQUEST);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == ALL_PERMISSIONS_REQUEST) {
            boolean cameraGranted = false;
            
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    cameraGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }
            
            if (cameraGranted) {
                startCamera();
            } else {
                Toast.makeText(this, getString(R.string.camera_permission_required),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, getString(R.string.camera_permission_required),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(this).get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), 
                             Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void bindCameraUseCases() {
        if (cameraProvider == null) return;
        
        
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());
        
        
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
            processImageForPoseDetection(imageProxy);
        });
        
        
        
        try {
            
            cameraProvider.unbindAll();
            
            
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            
            
            startUserDetection();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error binding camera: " + e.getMessage(), 
                         Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Generate and show outfit overlay
     */
    private void generateAndShowOutfit() {
        instructionText.setText("Generating your perfect outfits...");
        
        
        String weatherCondition = currentWeather != null ? currentWeather.condition : "Sunny";
        OutfitGenerator genderWeatherGenerator = new OutfitGenerator(userGender, weatherCondition);
        
        
        generatedOutfits = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Outfit outfit = genderWeatherGenerator.generateOutfit();
            if (outfit != null) {
                outfit.setName("Outfit Option " + (i + 1));
                generatedOutfits.add(outfit);
            }
        }
        
        if (!generatedOutfits.isEmpty()) {
            
            currentOutfit = generatedOutfits.get(0);
            showOutfitOverlay();
            updateOutfitButtons();
            instructionText.setText("Choose from 3 outfit options! Tap the numbers to switch.");
            
            
            mixMatchButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Failed to generate outfits", Toast.LENGTH_SHORT).show();
            instructionText.setText("Point the camera at yourself and tap to generate outfits!");
        }
    }
    
    /**
     * Show outfit overlay on AR view
     */
    private void showOutfitOverlay() {
        if (arOverlayView != null) {
            
            arOverlayView.setOutfit(currentOutfit);
            arOverlayView.updateBodyLandmarks(bodyLandmarks);
            arOverlayView.setOutfitVisible(true);
            isOutfitVisible = true;
            
            
            generateOutfitButton.setText("Try Another Outfit");
            
            
            outfitOptionsContainer.setVisibility(View.VISIBLE);
            
        }
    }
    
    /**
     * Update outfit buttons with generated outfit options
     */
    private void updateOutfitButtons() {
        if (generatedOutfits != null && !generatedOutfits.isEmpty()) {
            
            if (generatedOutfits.size() >= 1) {
                outfitOption1Button.setTag(generatedOutfits.get(0));
            }
            if (generatedOutfits.size() >= 2) {
                outfitOption2Button.setTag(generatedOutfits.get(1));
            }
            if (generatedOutfits.size() >= 3) {
                outfitOption3Button.setTag(generatedOutfits.get(2));
            }
        }
    }
    
    /**
     * Hide outfit overlay
     */
    private void hideOutfit() {
        if (arOverlayView != null) {
            arOverlayView.setOutfitVisible(false);
            isOutfitVisible = false;
            instructionText.setText("Point the camera at yourself and tap to generate outfits!");
            
            
            generateOutfitButton.setText("Generate Outfit");
            
            
            outfitOptionsContainer.setVisibility(View.GONE);
            mixMatchButton.setVisibility(View.GONE);
            outfitItemSelectorPanel.setVisibility(View.GONE);
            
        }
    }
    
    /**
     * Update body landmarks (would be called by pose detection)
     */
    private void updateBodyLandmarks(List<PointF> newLandmarks) {
        this.bodyLandmarks = newLandmarks;
        if (arOverlayView != null) {
            arOverlayView.updateBodyLandmarks(bodyLandmarks);
        }
    }
    
    
    /**
     * Take a photo of the outfit (placeholder for future implementation)
     */
    private void takeOutfitPhoto() {
        try {
            
            Bitmap cameraPhoto = captureCameraImage();
            
            
            Bitmap arOverlay = captureAROverlay();
            
            if (cameraPhoto != null && arOverlay != null) {
                
                Bitmap compositePhoto = createCompositePhoto(cameraPhoto, arOverlay);
                
                if (compositePhoto != null) {
                    
                    String fileName = savePhotoToStorage(compositePhoto, "");
                    
                    if (fileName != null) {
                        Toast.makeText(this, "Photo saved: " + fileName, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to create composite photo", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to capture camera or AR overlay", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error capturing photo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Capture the current screen with AR overlay
     */
    private Bitmap captureScreenWithAR() {
        try {
            
            View rootView = findViewById(android.R.id.content);
            
            
            Bitmap bitmap = Bitmap.createBitmap(
                rootView.getWidth(),
                rootView.getHeight(),
                Bitmap.Config.ARGB_8888
            );
            
            
            Canvas canvas = new Canvas(bitmap);
            
            
            canvas.drawColor(Color.WHITE);
            
            
            if (cameraPreview != null) {
                cameraPreview.draw(canvas);
            }
            
            
            if (arOverlayView != null) {
                arOverlayView.draw(canvas);
            }
            
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Capture the raw camera image (without AR overlay)
     */
    private Bitmap captureCameraImage() {
        try {
            
            if (cameraPreview != null) {
                
                return cameraPreview.getBitmap();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Capture only the AR overlay (skeleton and clothing)
     */
    private Bitmap captureAROverlay() {
        try {
            if (arOverlayView != null) {
                
                Bitmap bitmap = Bitmap.createBitmap(
                    arOverlayView.getWidth(),
                    arOverlayView.getHeight(),
                    Bitmap.Config.ARGB_8888
                );
                
                
                Canvas canvas = new Canvas(bitmap);
                
                
                canvas.drawColor(Color.TRANSPARENT);
                
                
                arOverlayView.draw(canvas);
                
                return bitmap;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Create composite photo with AR overlay on top of camera photo
     */
    private Bitmap createCompositePhoto(Bitmap cameraPhoto, Bitmap arOverlay) {
        try {
            
            int width = cameraPhoto.getWidth();
            int height = cameraPhoto.getHeight();
            
            
            Bitmap composite = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(composite);
            
            
            canvas.drawBitmap(cameraPhoto, 0, 0, null);
            
            
            Bitmap scaledAROverlay = Bitmap.createScaledBitmap(arOverlay, width, height, true);
            
            
            Paint paint = new Paint();
            paint.setAlpha(255); 
            canvas.drawBitmap(scaledAROverlay, 0, 0, paint);
            
            return composite;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Save photo to device storage
     */
    private String savePhotoToStorage(Bitmap bitmap, String prefix) {
        try {
            
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = prefix + "AuraFit_Outfit_" + timeStamp + ".jpg";
            
            
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File aurafitDir = new File(picturesDir, "AuraFit");
            
            
            if (!aurafitDir.exists()) {
                aurafitDir.mkdirs();
            }
            
            
            File photoFile = new File(aurafitDir, fileName);
            
            
            FileOutputStream fos = new FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            
            
            MediaStore.Images.Media.insertImage(
                getContentResolver(),
                photoFile.getAbsolutePath(),
                fileName,
                "AuraFit Outfit Photo"
            );
            
            return fileName;
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Show user detection indicator
     */
    private void showUserDetected() {
        if (detectionIndicator != null) {
            detectionIndicator.setVisibility(View.VISIBLE);
            detectionStatusIcon.setImageResource(R.drawable.ic_person);
            detectionStatusIcon.setColorFilter(getResources().getColor(android.R.color.holo_green_light));
            detectionStatusText.setText("User Detected");
            detectionStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }
    
    /**
     * Show user not detected indicator
     */
    private void showUserNotDetected() {
        if (detectionIndicator != null) {
            detectionIndicator.setVisibility(View.VISIBLE);
            detectionStatusIcon.setImageResource(R.drawable.ic_person);
            detectionStatusIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_light));
            detectionStatusText.setText("No User Detected");
            detectionStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }
    
    /**
     * Hide detection indicator
     */
    private void hideDetectionIndicator() {
        if (detectionIndicator != null) {
            detectionIndicator.setVisibility(View.GONE);
        }
    }
    
    /**
     * Process camera image for pose detection with MediaPipe
     */
    @androidx.camera.core.ExperimentalGetImage
    private void processImageForPoseDetection(ImageProxy imageProxy) {
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null && poseLandmarker != null) {
            
            android.graphics.Bitmap bitmap = imageProxyToBitmap(imageProxy);
            if (bitmap != null) {
                
                imageWidth = bitmap.getWidth();
                imageHeight = bitmap.getHeight();
                
                
                MPImage mpImage = new BitmapImageBuilder(bitmap).build();
                
                
                long frameTime = System.currentTimeMillis();
                poseLandmarker.detectAsync(mpImage, frameTime);
            }
        }
        imageProxy.close();
    }
    
    /**
     * Convert ImageProxy to Bitmap
     */
    private android.graphics.Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        android.graphics.Bitmap bitmap = imageProxy.toBitmap();
        
        int rotation = imageProxy.getImageInfo().getRotationDegrees();
        if (rotation != 0) {
            android.graphics.Matrix matrix = new android.graphics.Matrix();
            matrix.postRotate(rotation);
            bitmap = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, 
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }
    
    /**
     * Callback for MediaPipe pose detection results
     */
    private void onPoseDetectionResult(PoseLandmarkerResult result, MPImage mpImage) {
        if (result == null || result.landmarks().isEmpty()) {
            updateDetectionStatus(false, 0);
            return;
        }
        
        
        List<NormalizedLandmark> landmarks = result.landmarks().get(0);
        
        if (bodyLandmarks != null && imageWidth > 0 && imageHeight > 0) {
            bodyLandmarks.clear();
            
            
            
            
            
            
            
            
            
            
            
            
            if (landmarks.size() > 28) {
                
                
                NormalizedLandmark nose = landmarks.get(0);
                NormalizedLandmark leftEar = landmarks.get(7);
                NormalizedLandmark rightEar = landmarks.get(8);
                
                
                NormalizedLandmark leftShoulder = landmarks.get(11);
                NormalizedLandmark rightShoulder = landmarks.get(12);
                NormalizedLandmark leftElbow = landmarks.get(13);
                NormalizedLandmark rightElbow = landmarks.get(14);
                NormalizedLandmark leftWrist = landmarks.get(15);
                NormalizedLandmark rightWrist = landmarks.get(16);
                
                
                NormalizedLandmark leftHip = landmarks.get(23);
                NormalizedLandmark rightHip = landmarks.get(24);
                NormalizedLandmark leftKnee = landmarks.get(25);
                NormalizedLandmark rightKnee = landmarks.get(26);
                NormalizedLandmark leftAnkle = landmarks.get(27);
                NormalizedLandmark rightAnkle = landmarks.get(28);
                
                
                
                bodyLandmarks.add(new PointF((1.0f - nose.x()) * imageWidth, nose.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - leftEar.x()) * imageWidth, leftEar.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - rightEar.x()) * imageWidth, rightEar.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - leftShoulder.x()) * imageWidth, leftShoulder.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - rightShoulder.x()) * imageWidth, rightShoulder.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - leftElbow.x()) * imageWidth, leftElbow.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - rightElbow.x()) * imageWidth, rightElbow.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - leftWrist.x()) * imageWidth, leftWrist.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - rightWrist.x()) * imageWidth, rightWrist.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - leftHip.x()) * imageWidth, leftHip.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - rightHip.x()) * imageWidth, rightHip.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - leftKnee.x()) * imageWidth, leftKnee.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - rightKnee.x()) * imageWidth, rightKnee.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - leftAnkle.x()) * imageWidth, leftAnkle.y() * imageHeight)); 
                bodyLandmarks.add(new PointF((1.0f - rightAnkle.x()) * imageWidth, rightAnkle.y() * imageHeight)); 
                
                
                if (arOverlayView != null) {
                    runOnUiThread(() -> {
                        arOverlayView.updateBodyLandmarks(bodyLandmarks);
                        
                    });
                }
            }
        }
        
        
        updateDetectionStatus(true, landmarks.size());
    }
    
    /**
     * Update detection status
     */
    private void updateDetectionStatus(boolean detected, int landmarkCount) {
        
        boolean newDetectionStatus = detected && landmarkCount >= 25;
        
        if (newDetectionStatus != isUserDetected) {
            isUserDetected = newDetectionStatus;
            runOnUiThread(() -> {
                if (isUserDetected) {
                    showUserDetected();
                    instructionText.setText("User detected! Tap 'Generate Outfit' to create your look.");
                } else {
                    showUserNotDetected();
                    instructionText.setText("Position yourself in front of the camera");
                }
            });
        }
    }
    
    /**
     * Start user detection with ML Kit
     */
    private void startUserDetection() {
        
        
        showUserNotDetected();
        instructionText.setText("Positioning camera...");
    }
    
    /**
     * Change camera between front and back
     */
    private void changeCamera() {
        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        } else {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        }
        
        
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            startCamera();
        }
    }
    
    /**
     * Save current outfit to wardrobe
     */
    private void saveOutfitToWardrobe() {
        if (currentOutfit == null) {
            Toast.makeText(this, "No outfit to save", Toast.LENGTH_SHORT).show();
            return;
        }
        
        
        if (currentOutfit.getName() == null || currentOutfit.getName().isEmpty()) {
            currentOutfit.setName("Outfit " + System.currentTimeMillis());
        }
        
        WardrobeActivity.saveOutfitToFirebase(currentOutfit, new WardrobeActivity.OnSaveCompleteListener() {
            @Override
            public void onSaveSuccess(Outfit outfit) {
                runOnUiThread(() -> {
                    Toast.makeText(GenerateActivity.this, "Outfit saved to wardrobe!", Toast.LENGTH_SHORT).show();
                    
                    
                    notifyWardrobeActivityRefresh();
                });
            }
            
            @Override
            public void onSaveFailed(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(GenerateActivity.this, "Failed to save outfit: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * Notify WardrobeActivity to refresh its outfit list
     */
    private void notifyWardrobeActivityRefresh() {
        
        Intent intent = new Intent("OUTFIT_SAVED");
        intent.putExtra("action", "refresh_outfits");
        sendBroadcast(intent);
    }
    
    /**
     * Show the item selector panel for mix and match
     */
    private void showItemSelectorPanel() {
        if (generatedOutfits == null || generatedOutfits.isEmpty()) {
            Toast.makeText(this, "Please generate outfits first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        extractAllItemsFromOutfits();
        currentCategory = "Top";
        updateCategorySelection();
        displayItemsForCategory(currentCategory);
        
        outfitItemSelectorPanel.setVisibility(View.VISIBLE);
        mixMatchButton.setVisibility(View.GONE);
    }
    
    /**
     * Hide the item selector panel
     */
    private void hideItemSelectorPanel() {
        outfitItemSelectorPanel.setVisibility(View.GONE);
        mixMatchButton.setVisibility(View.VISIBLE);
    }
    
    /**
     * Extract all clothing items from the mixed folder (all available clothes)
     */
    private void extractAllItemsFromOutfits() {
        allTops.clear();
        allBottoms.clear();
        allShoes.clear();
        allAccessories.clear();
        
        String weatherFolder = (currentWeather != null && currentWeather.condition.equals("Rainy")) ? "Rainy" : "Sunny";
        
        // Load all tops from mixed folder
        allTops = loadClothingItemsFromAssets("ar_models/clothing/mixed/top/" + weatherFolder, "Top");
        
        // Load all bottoms from mixed folder
        allBottoms = loadClothingItemsFromAssets("ar_models/clothing/mixed/bottom/" + weatherFolder, "Bottom");
        
        // Load all shoes from mixed folder
        allShoes = loadClothingItemsFromAssets("ar_models/clothing/mixed/shoes/" + weatherFolder, "Shoes");
        
        // Load all accessories from mixed folder (includes bag, hat, glasses)
        allAccessories = new ArrayList<>();
        allAccessories.addAll(loadClothingItemsFromAssets("ar_models/clothing/mixed/accessories/" + weatherFolder + "/bag", "Accessories"));
        allAccessories.addAll(loadClothingItemsFromAssets("ar_models/clothing/mixed/accessories/" + weatherFolder + "/hat", "Accessories"));
        if (weatherFolder.equals("Sunny")) {
            allAccessories.addAll(loadClothingItemsFromAssets("ar_models/clothing/mixed/accessories/" + weatherFolder + "/glasses", "Accessories"));
        }
        
        // Set default selections
        if (selectedTop == null && !allTops.isEmpty()) {
            selectedTop = allTops.get(0);
        }
        if (selectedBottom == null && !allBottoms.isEmpty()) {
            selectedBottom = allBottoms.get(0);
        }
        if (selectedShoes == null && !allShoes.isEmpty()) {
            selectedShoes = allShoes.get(0);
        }
        if (selectedAccessories == null && !allAccessories.isEmpty()) {
            selectedAccessories = allAccessories.get(0);
        }
    }
    
    /**
     * Load clothing items from assets folder
     */
    private List<OutfitItem> loadClothingItemsFromAssets(String folderPath, String category) {
        List<OutfitItem> items = new ArrayList<>();
        
        try {
            String[] files = getAssets().list(folderPath);
            if (files != null) {
                for (String fileName : files) {
                    if (fileName.endsWith(".png")) {
                        OutfitItem item = new OutfitItem();
                        String itemName = fileName.replace(".png", "");
                        item.setItemId(category.toLowerCase() + "_" + itemName.hashCode());
                        item.setName(itemName);
                        item.setCategory(category);
                        item.setSubcategory(category);
                        item.setImageUrl("");
                        item.setArModelUrl(folderPath + "/" + fileName);
                        item.setConfidence(90);
                        items.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Check if an item with the same name already exists in the list
     */
    private boolean containsItem(List<OutfitItem> items, OutfitItem newItem) {
        for (OutfitItem item : items) {
            if (item.getName().equals(newItem.getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Switch to a different category tab
     */
    private void switchCategory(String category) {
        currentCategory = category;
        updateCategorySelection();
        displayItemsForCategory(category);
    }
    
    /**
     * Update the visual state of category tabs
     */
    private void updateCategorySelection() {
        int selectedColor = 0xFFFF6B35;
        int unselectedColor = 0x80000000;
        
        tabTop.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                currentCategory.equals("Top") ? selectedColor : unselectedColor
            )
        );
        tabBottom.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                currentCategory.equals("Bottom") ? selectedColor : unselectedColor
            )
        );
        tabShoes.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                currentCategory.equals("Shoes") ? selectedColor : unselectedColor
            )
        );
        tabAccessories.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                currentCategory.equals("Accessories") ? selectedColor : unselectedColor
            )
        );
    }
    
    /**
     * Display items for the selected category
     */
    private void displayItemsForCategory(String category) {
        itemsContainer.removeAllViews();
        
        List<OutfitItem> itemsToDisplay;
        OutfitItem currentSelection;
        
        switch (category) {
            case "Top":
                itemsToDisplay = allTops;
                currentSelection = selectedTop;
                break;
            case "Bottom":
                itemsToDisplay = allBottoms;
                currentSelection = selectedBottom;
                break;
            case "Shoes":
                itemsToDisplay = allShoes;
                currentSelection = selectedShoes;
                break;
            case "Accessories":
                itemsToDisplay = allAccessories;
                currentSelection = selectedAccessories;
                break;
            default:
                return;
        }
        
        LayoutInflater inflater = LayoutInflater.from(this);
        for (OutfitItem item : itemsToDisplay) {
            View itemView = inflater.inflate(R.layout.item_clothing_selector, itemsContainer, false);
            
            LinearLayout container = itemView.findViewById(R.id.itemContainer);
            ImageView icon = itemView.findViewById(R.id.itemIcon);
            TextView name = itemView.findViewById(R.id.itemName);
            View selectedIndicator = itemView.findViewById(R.id.selectedIndicator);
            
            String itemName = item.getName();
            if (itemName.length() > 15) {
                itemName = itemName.substring(0, 12) + "...";
            }
            name.setText(itemName);
            
            // Load the actual PNG image from assets as the icon
            loadImageFromAssets(icon, item.getArModelUrl());
            
            boolean isSelected = currentSelection != null && 
                               currentSelection.getName().equals(item.getName());
            selectedIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            
            if (isSelected) {
                container.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4ECDC4)
                );
            } else {
                container.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0x80000000)
                );
            }
            
            itemView.setOnClickListener(v -> {
                selectItem(item, category);
                displayItemsForCategory(category);
            });
            
            itemsContainer.addView(itemView);
        }
    }
    
    /**
     * Load image from assets folder and display it in ImageView
     */
    private void loadImageFromAssets(ImageView imageView, String assetPath) {
        try {
            java.io.InputStream inputStream = getAssets().open(assetPath);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setColorFilter(null); // Remove any tint
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to generic icon if loading fails
            imageView.setImageResource(R.drawable.ic_shirt);
        }
    }
    
    /**
     * Set icon based on category
     */
    private void setIconForCategory(ImageView icon, String category) {
        int iconResId;
        switch (category) {
            case "Top":
                iconResId = R.drawable.ic_shirt;
                break;
            case "Bottom":
                iconResId = R.drawable.ic_pants;
                break;
            case "Shoes":
                iconResId = R.drawable.ic_shoes;
                break;
            case "Accessories":
                iconResId = R.drawable.ic_accessories;
                break;
            default:
                iconResId = R.drawable.ic_shirt;
                break;
        }
        icon.setImageResource(iconResId);
    }
    
    /**
     * Select an item from a specific category
     */
    private void selectItem(OutfitItem item, String category) {
        switch (category) {
            case "Top":
                selectedTop = item;
                break;
            case "Bottom":
                selectedBottom = item;
                break;
            case "Shoes":
                selectedShoes = item;
                break;
            case "Accessories":
                selectedAccessories = item;
                break;
        }
        
        Toast.makeText(this, "Selected: " + item.getName(), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Apply the custom outfit (try on selected items)
     */
    private void applyCustomOutfit() {
        if (selectedTop == null || selectedBottom == null || selectedShoes == null || selectedAccessories == null) {
            Toast.makeText(this, "Please select items from all categories", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Outfit customOutfit = new Outfit();
        customOutfit.setName("Custom Mix & Match");
        customOutfit.setStyle("Custom");
        customOutfit.setConfidence(85);
        
        List<OutfitItem> items = new ArrayList<>();
        items.add(selectedTop);
        items.add(selectedBottom);
        items.add(selectedShoes);
        items.add(selectedAccessories);
        
        customOutfit.setItems(items);
        
        currentOutfit = customOutfit;
        showOutfitOverlay();
        
        hideItemSelectorPanel();
        
        Toast.makeText(this, "Trying on your custom outfit!", Toast.LENGTH_SHORT).show();
        instructionText.setText("Your custom mix & match outfit is ready!");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (poseLandmarker != null) {
            poseLandmarker.close();
        }
    }
}

