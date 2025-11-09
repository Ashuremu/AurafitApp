package com.aurafit.AuraFitApp.ui.measure;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
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
import com.aurafit.AuraFitApp.data.UserDataManager;
import com.aurafit.AuraFitApp.data.model.User;
import android.content.res.ColorStateList;
import android.graphics.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MeasureActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int ALL_PERMISSIONS_REQUEST = 103;
    
    // UI Components
    private PreviewView cameraPreview;
    private ImageView backButton;
    private ImageView captureButton;
    private TextView countdownTimer;
    private LinearLayout cameraControlsContainer;
    private TextView instructionText;
    private ImageView bodyOutlineOverlay;
    
    // Success indicators
    private View successBorderOverlay;
    private ImageView successCheckmark;
    private TextView successMessage;

    // Measurement points
    private View chestLeftPoint;
    private View chestRightPoint;
    private View waistLeftPoint;
    private View waistRightPoint;
    private View hipLeftPoint;
    private View hipRightPoint;
    private View heightLine;
    
    // Camera
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private CountDownTimer measurementTimer;
    
    // MediaPipe Pose Detection
    private PoseLandmarker poseLandmarker;
    private boolean isUserDetected = false;
    private boolean isFullBodyDetected = false;
    private boolean isHandGestureDetected = false;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private List<NormalizedLandmark> currentLandmarks;
    
    // Measurement calculation constants
    private static final float AVERAGE_HEAD_HEIGHT_CM = 21.0f; // Average head height in cm
    private static final float AVERAGE_SHOULDER_WIDTH_CM = 41.0f; // Average shoulder width in cm
    
    // Data
    private UserDataManager userDataManager;
    private boolean isMeasurementComplete = false;
    private boolean isAutoCaptureTriggered = false;
    private BodyMeasurements detectedMeasurements;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        
        userDataManager = UserDataManager.getInstance();
        detectedMeasurements = new BodyMeasurements();
        
        initializePoseDetector();
        initializeViews();
        setupClickListeners();
        
        // Request all necessary permissions upfront
        requestAllPermissions();
    }
    
    private void initializeViews() {
        cameraPreview = findViewById(R.id.cameraPreview);
        backButton = findViewById(R.id.backButton);
        captureButton = findViewById(R.id.captureButton);
        countdownTimer = findViewById(R.id.countdownTimer);
        cameraControlsContainer = findViewById(R.id.cameraControlsContainer);
        instructionText = findViewById(R.id.instructionText);
        bodyOutlineOverlay = findViewById(R.id.bodyOutlineOverlay);
        
        // Success indicators
        successBorderOverlay = findViewById(R.id.successBorderOverlay);
        successCheckmark = findViewById(R.id.successCheckmark);
        successMessage = findViewById(R.id.successMessage);

        // Measurement points
        chestLeftPoint = findViewById(R.id.chestLeftPoint);
        chestRightPoint = findViewById(R.id.chestRightPoint);
        waistLeftPoint = findViewById(R.id.waistLeftPoint);
        waistRightPoint = findViewById(R.id.waistRightPoint);
        hipLeftPoint = findViewById(R.id.hipLeftPoint);
        hipRightPoint = findViewById(R.id.hipRightPoint);
        heightLine = findViewById(R.id.heightLine);
    }

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
        
        captureButton.setOnClickListener(v -> {
            if (!isMeasurementComplete) {
                if (isHandGestureDetected && isFullBodyDetected) {
                    startBodyMeasurement();
                } else if (!isFullBodyDetected) {
                    Toast.makeText(this, "Please ensure your full body is visible in the camera frame", 
                                 Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please raise your hand above shoulder level to start measurement", 
                                 Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        
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
        return checkCameraPermission();
    }
    
    private void requestAllPermissions() {
        if (checkAllPermissions()) {
            // All permissions granted, start camera
            startCamera();
        } else {
            // Request missing permissions
            List<String> permissions = new ArrayList<>();

            if (!checkCameraPermission()) {
                permissions.add(Manifest.permission.CAMERA);
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
        Future<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        ((java.util.concurrent.Executor) ContextCompat.getMainExecutor(this)).execute(() -> {}); // no-op to keep imports
        ((com.google.common.util.concurrent.ListenableFuture<?>) ProcessCameraProvider.getInstance(this)).addListener(() -> {}, ContextCompat.getMainExecutor(this));
        // Add listener without referencing Guava at compile-time by using Future variable above inside executor
        ((com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider>) ProcessCameraProvider.getInstance(this)).addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), 
                             Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void bindCameraUseCases() {
        if (cameraProvider == null) return;
        
        // Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());
        
        // ImageCapture use case
        imageCapture = new ImageCapture.Builder().build();
        
        // Image analysis for pose detection
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
            processImageForPoseDetection(imageProxy);
        });
        
        // Camera selector (front camera)
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        
        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll();
            
            // Bind use cases to camera (preview + pose detection + image capture)
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
            
            // Start user detection after camera is ready
            startUserDetection();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error binding camera: " + e.getMessage(), 
                         Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startBodyMeasurement() {
        // Show measurement points
        showMeasurementPoints();
        
        // Start countdown
        startCountdown();
    }
    
    private void showMeasurementPoints() {
        chestLeftPoint.setVisibility(View.VISIBLE);
        chestRightPoint.setVisibility(View.VISIBLE);
        waistLeftPoint.setVisibility(View.VISIBLE);
        waistRightPoint.setVisibility(View.VISIBLE);
        hipLeftPoint.setVisibility(View.VISIBLE);
        hipRightPoint.setVisibility(View.VISIBLE);
        heightLine.setVisibility(View.VISIBLE);
    }
    
    private void startCountdown() {
        countdownTimer.setVisibility(View.VISIBLE);
        
        measurementTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                countdownTimer.setText(String.valueOf(secondsRemaining));
            }
            
            @Override
            public void onFinish() {
                countdownTimer.setVisibility(View.GONE);
                captureAndProcessMeasurement();
            }
        };
        
        measurementTimer.start();
    }
    
    private void captureAndProcessMeasurement() {
        if (imageCapture == null) return;
        
        // Create output file
        File outputFile = new File(getExternalFilesDir(null), 
                                 "measurement_" + System.currentTimeMillis() + ".jpg");
        
        ImageCapture.OutputFileOptions outputOptions = 
                new ImageCapture.OutputFileOptions.Builder(outputFile).build();
        
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        // Show success indicators immediately after capture
                        runOnUiThread(() -> {
                            showSuccessIndicators();
                            hideMeasurementPoints();
                        });
                        
                        // Process the captured image for body measurements
                        processBodyMeasurements(outputFile);
                    }
                    
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(MeasureActivity.this, 
                                     "Failed to capture image: " + exception.getMessage(), 
                                     Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    
    private void processBodyMeasurements(File imageFile) {
        if (currentLandmarks != null && currentLandmarks.size() >= 25) {
            // Calculate real measurements from landmarks
            calculateBodyMeasurementsFromLandmarks();
        } else {
            // Show error message if pose detection fails
            Toast.makeText(this, "Unable to detect body pose. Please ensure you are fully visible in the camera frame and try again.", Toast.LENGTH_LONG).show();
            // Reset measurement state to allow retry
            isMeasurementComplete = false;
            hideMeasurementPoints();
            hideSuccessIndicators();
        }
    }

    private void calculateBodyMeasurementsFromLandmarks() {
        try {

            NormalizedLandmark nose = currentLandmarks.get(0);
            NormalizedLandmark leftShoulder = currentLandmarks.get(11);
            NormalizedLandmark rightShoulder = currentLandmarks.get(12);
            NormalizedLandmark leftHip = currentLandmarks.get(23);
            NormalizedLandmark rightHip = currentLandmarks.get(24);
            NormalizedLandmark leftAnkle = currentLandmarks.get(27);
            NormalizedLandmark rightAnkle = currentLandmarks.get(28);
            
            // Calculate height using head-to-ankle distance
            float headToAnkleDistance = calculateDistance(nose, leftAnkle);
            float heightInPixels = headToAnkleDistance;

            // Calculate Height measurement
            float headHeightInPixels = calculateHeadHeight();
            float pixelToCmRatio = AVERAGE_HEAD_HEIGHT_CM / headHeightInPixels;
            detectedMeasurements.height = (int)(heightInPixels * pixelToCmRatio * 1.04f);
            
            // Calculate chest measurement
            float shoulderWidthInPixels = calculateDistance(leftShoulder, rightShoulder);
            detectedMeasurements.chest = (int)(shoulderWidthInPixels * pixelToCmRatio * 1.09f);
            
            // Calculate waist measurement
            float hipWidthInPixels = calculateDistance(leftHip, rightHip);
            detectedMeasurements.waist = (int)(hipWidthInPixels * pixelToCmRatio * 1.65f);
            
            // Calculate hip measurement
            detectedMeasurements.hips = (int)(hipWidthInPixels * pixelToCmRatio * 1.9f);
            
            // default shoe size
            detectedMeasurements.shoeSize = 8.5;
            
            Toast.makeText(this, "Body measurements calculated from pose detection!", Toast.LENGTH_SHORT).show();
            navigateToEditMeasurements();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error calculating measurements: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            isMeasurementComplete = false;
            hideMeasurementPoints();
            hideSuccessIndicators();
        }
    }

    private float calculateDistance(NormalizedLandmark landmark1, NormalizedLandmark landmark2) {
        float dx = landmark1.x() - landmark2.x();
        float dy = landmark1.y() - landmark2.y();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float calculateHeadHeight() {
        if (currentLandmarks.size() > 8) {
            NormalizedLandmark nose = currentLandmarks.get(0);
            NormalizedLandmark leftEar = currentLandmarks.get(7);
            NormalizedLandmark rightEar = currentLandmarks.get(8);
            
            // Use ear-to-ear distance as head width, then estimate height
            float earDistance = calculateDistance(leftEar, rightEar);
            return earDistance * 1.2f; // Head height is typically 20% more than width
        }
        return 0.1f; // Default fallback
    }
    
    private void hideMeasurementPoints() {
        chestLeftPoint.setVisibility(View.GONE);
        chestRightPoint.setVisibility(View.GONE);
        waistLeftPoint.setVisibility(View.GONE);
        waistRightPoint.setVisibility(View.GONE);
        hipLeftPoint.setVisibility(View.GONE);
        hipRightPoint.setVisibility(View.GONE);
        heightLine.setVisibility(View.GONE);
    }
    
    private void showSuccessIndicators() {
        // Show green border around body outline
        successBorderOverlay.setVisibility(View.VISIBLE);
        
        // Show checkmark with animation
        successCheckmark.setVisibility(View.VISIBLE);
        successCheckmark.setAlpha(0f);
        successCheckmark.setScaleX(0.5f);
        successCheckmark.setScaleY(0.5f);
        successCheckmark.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
        
        // Show success message
        successMessage.setVisibility(View.VISIBLE);
        successMessage.setAlpha(0f);
        successMessage.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(150)
                .start();
        
        // Update instruction text
        instructionText.setText("Measurement Complete! Processing...");
        instructionText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
    }
    
    private void hideSuccessIndicators() {
        successBorderOverlay.setVisibility(View.GONE);
        successCheckmark.setVisibility(View.GONE);
        successMessage.setVisibility(View.GONE);
    }

    private void navigateToEditMeasurements() {
        Intent intent = new Intent(this, EditMeasurementsActivity.class);

        // Pass detected measurements to the edit activity
        intent.putExtra(EditMeasurementsActivity.EXTRA_HEIGHT, detectedMeasurements.height);
        intent.putExtra(EditMeasurementsActivity.EXTRA_CHEST, detectedMeasurements.chest);
        intent.putExtra(EditMeasurementsActivity.EXTRA_WAIST, detectedMeasurements.waist);
        intent.putExtra(EditMeasurementsActivity.EXTRA_HIPS, detectedMeasurements.hips);
        intent.putExtra(EditMeasurementsActivity.EXTRA_SHOE_SIZE, 8.5f); // Default shoe size

        startActivity(intent);
        finish(); // Close the measure activity
    }

    @androidx.camera.core.ExperimentalGetImage
    private void processImageForPoseDetection(ImageProxy imageProxy) {
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null && poseLandmarker != null) {
            // Convert to bitmap
            android.graphics.Bitmap bitmap = imageProxyToBitmap(imageProxy);
            if (bitmap != null) {
                // Store image dimensions
                imageWidth = bitmap.getWidth();
                imageHeight = bitmap.getHeight();
                
                // Create MPImage
                MPImage mpImage = new BitmapImageBuilder(bitmap).build();
                
                // Detect pose asynchronously
                long frameTime = System.currentTimeMillis();
                poseLandmarker.detectAsync(mpImage, frameTime);
            }
        }
        imageProxy.close();
    }

    private android.graphics.Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        android.graphics.Bitmap bitmap = imageProxy.toBitmap();
        // Rotate if needed based on image rotation
        int rotation = imageProxy.getImageInfo().getRotationDegrees();
        if (rotation != 0) {
            android.graphics.Matrix matrix = new android.graphics.Matrix();
            matrix.postRotate(rotation);
            bitmap = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, 
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    private void onPoseDetectionResult(PoseLandmarkerResult result, MPImage mpImage) {
        if (result == null || result.landmarks().isEmpty()) {
            updateDetectionStatus(false, 0, false, false);
            return;
        }
        
        // Get the first detected pose
        List<NormalizedLandmark> landmarks = result.landmarks().get(0);
        
        // Store current landmarks for measurement calculation
        currentLandmarks = landmarks;
        
        // Check for full body detection
        boolean fullBodyDetected = isFullBodyVisible(landmarks);
        
        // Check for hand gesture (hand raised)
        boolean handGestureDetected = isHandGestureDetected(landmarks);
        
        // Update detection status
        updateDetectionStatus(true, landmarks.size(), fullBodyDetected, handGestureDetected);
    }

    private void updateDetectionStatus(boolean detected, int landmarkCount, boolean fullBodyDetected, boolean handGestureDetected) {
        // Consider user detected if we have at least 25 of 33 landmarks
        boolean newDetectionStatus = detected && landmarkCount >= 25;
        
        // Update detection states
        boolean userStatusChanged = newDetectionStatus != isUserDetected;
        boolean fullBodyStatusChanged = fullBodyDetected != isFullBodyDetected;
        boolean handGestureStatusChanged = handGestureDetected != isHandGestureDetected;
        
        isUserDetected = newDetectionStatus;
        isFullBodyDetected = fullBodyDetected;
        isHandGestureDetected = handGestureDetected;
        
        if (userStatusChanged || fullBodyStatusChanged || handGestureStatusChanged) {
            runOnUiThread(() -> {
                updateUIForDetectionStatus();
            });
        }
    }

    private void startUserDetection() {
        // Detection starts automatically via image analysis
        // Just show the initial state
        showUserNotDetected();
        instructionText.setText("Positioning camera...");
        
        // Reset detection states
        isUserDetected = false;
        isFullBodyDetected = false;
        isHandGestureDetected = false;
        isAutoCaptureTriggered = false;
    }

    private void showUserDetected() {
        // Change outline to green when user is detected
        bodyOutlineOverlay.setImageResource(R.drawable.outline_green);
        bodyOutlineOverlay.setAlpha(0.8f);
    }

    private void showUserNotDetected() {
        // Change outline back to normal when user is not detected
        bodyOutlineOverlay.setImageResource(R.drawable.outline);
        bodyOutlineOverlay.setAlpha(0.6f);
    }
    
    private boolean isFullBodyVisible(List<NormalizedLandmark> landmarks) {
        if (landmarks.size() < 33) return false;
        
        // Check for key body parts: head, shoulders, hips, knees, ankles
        // Landmark indices: nose(0), left/right shoulder(11,12), left/right hip(23,24), 
        // left/right knee(25,26), left/right ankle(27,28)
        boolean hasHead = landmarks.get(0).visibility().orElse(0f) > 0.5f; // nose
        boolean hasShoulders = landmarks.get(11).visibility().orElse(0f) > 0.5f && landmarks.get(12).visibility().orElse(0f) > 0.5f;
        boolean hasHips = landmarks.get(23).visibility().orElse(0f) > 0.5f && landmarks.get(24).visibility().orElse(0f) > 0.5f;
        boolean hasKnees = landmarks.get(25).visibility().orElse(0f) > 0.5f && landmarks.get(26).visibility().orElse(0f) > 0.5f;
        boolean hasAnkles = landmarks.get(27).visibility().orElse(0f) > 0.5f && landmarks.get(28).visibility().orElse(0f) > 0.5f;
        
        // Full body is visible if we have head, shoulders, hips, and at least knees or ankles
        return hasHead && hasShoulders && hasHips && (hasKnees || hasAnkles);
    }
    
    private boolean isHandGestureDetected(List<NormalizedLandmark> landmarks) {
        if (landmarks.size() < 33) return false;
        
        // Check if either hand is raised (wrist is above shoulder level)
        NormalizedLandmark leftShoulder = landmarks.get(11);
        NormalizedLandmark rightShoulder = landmarks.get(12);
        NormalizedLandmark leftWrist = landmarks.get(15);
        NormalizedLandmark rightWrist = landmarks.get(16);
        NormalizedLandmark leftElbow = landmarks.get(13);
        NormalizedLandmark rightElbow = landmarks.get(14);
        
        // Get visibility scores
        float leftWristVisibility = leftWrist.visibility().orElse(0f);
        float rightWristVisibility = rightWrist.visibility().orElse(0f);
        float leftElbowVisibility = leftElbow.visibility().orElse(0f);
        float rightElbowVisibility = rightElbow.visibility().orElse(0f);
        
        // Check if wrists are visible (lower threshold for better detection)
        boolean leftWristVisible = leftWristVisibility > 0.3f;
        boolean rightWristVisible = rightWristVisibility > 0.3f;
        boolean leftElbowVisible = leftElbowVisibility > 0.3f;
        boolean rightElbowVisible = rightElbowVisibility > 0.3f;
        
        if (!leftWristVisible && !rightWristVisible) {
            return false; // No wrists visible
        }
        
        // Check if either wrist is raised above shoulder level (with some tolerance)
        boolean leftHandRaised = false;
        boolean rightHandRaised = false;
        
        if (leftWristVisible) {
            // Method 1: Check if wrist is above shoulder
            float shoulderToWristDistance = leftShoulder.y() - leftWrist.y();
            boolean wristAboveShoulder = shoulderToWristDistance > 0.03f; // 3% tolerance
            
            // Method 2: Check if elbow is above shoulder (arm raised)
            boolean elbowAboveShoulder = false;
            if (leftElbowVisible) {
                float shoulderToElbowDistance = leftShoulder.y() - leftElbow.y();
                elbowAboveShoulder = shoulderToElbowDistance > 0.02f; // 2% tolerance
            }
            
            leftHandRaised = wristAboveShoulder || elbowAboveShoulder;
        }
        
        if (rightWristVisible) {
            // Method 1: Check if wrist is above shoulder
            float shoulderToWristDistance = rightShoulder.y() - rightWrist.y();
            boolean wristAboveShoulder = shoulderToWristDistance > 0.03f; // 3% tolerance
            
            // Method 2: Check if elbow is above shoulder (arm raised)
            boolean elbowAboveShoulder = false;
            if (rightElbowVisible) {
                float shoulderToElbowDistance = rightShoulder.y() - rightElbow.y();
                elbowAboveShoulder = shoulderToElbowDistance > 0.02f; // 2% tolerance
            }
            
            rightHandRaised = wristAboveShoulder || elbowAboveShoulder;
        }
        
        return leftHandRaised || rightHandRaised;
    }
    
    private void updateUIForDetectionStatus() {
        if (!isUserDetected) {
            showUserNotDetected();
            instructionText.setText("Please stand your whole body in the box front view, don't move for 5secs");
        } else if (!isFullBodyDetected) {
            showUserNotDetected();
            instructionText.setText("Please ensure your full body is visible in the camera frame");
        } else if (!isHandGestureDetected) {
            showUserDetected(); // Green outline for full body detected
            instructionText.setText("Full body detected! Raise your hand above shoulder level to start measurement.");
        } else {
            showUserDetected(); // Green outline
            instructionText.setText("Hand detected! Starting measurement in 3 seconds...");
            
            // Automatically start measurement when hand gesture is detected (only once)
            if (!isMeasurementComplete && !isAutoCaptureTriggered) {
                isAutoCaptureTriggered = true;
                // Add a small delay before starting measurement
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (!isMeasurementComplete) {
                        startBodyMeasurement();
                    }
                }, 3000); // 3 second delay
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (measurementTimer != null) {
            measurementTimer.cancel();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (poseLandmarker != null) {
            poseLandmarker.close();
        }
    }
    
    // Helper class to store body measurements
    private static class BodyMeasurements {
        int height;
        int chest;
        int waist;
        int hips;
        double shoeSize;
        
        public BodyMeasurements() {
            height = 0;
            chest = 0;
            waist = 0;
            hips = 0;
            shoeSize = 8.5;
        }
    }
}
