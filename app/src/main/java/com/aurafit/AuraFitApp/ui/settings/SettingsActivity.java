package com.aurafit.AuraFitApp.ui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.UserDataManager;
import com.aurafit.AuraFitApp.data.model.User;
import com.aurafit.AuraFitApp.ui.login.LoginActivity;
import com.aurafit.AuraFitApp.ui.measure.MeasureActivity;
import com.aurafit.AuraFitApp.ui.preferences.StylePreferencesActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageView backButton;
    private ImageView profilePicture;
    private Button editProfileButton;
    private Button updateMeasurementsButton;
    private TextView nameText;
    private TextView ageText;
    private TextView heightText;
    private TextView chestText;
    private TextView waistText;
    private TextView hipsText;
    private TextView logoutOption;
    private TextView deleteAccountOption;
    
    // Style preference views
    private TextView clothingPreferenceText;
    private TextView stylePreferenceText;
    private Button updatePreferencesButton;
    
    private UserDataManager userDataManager;
    private static final int REQUEST_PICK_IMAGE = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userDataManager = UserDataManager.getInstance();
        
        initializeViews();
        setupClickListeners();
        loadUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning from preference activities
        loadUserData();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        profilePicture = findViewById(R.id.profilePicture);
        editProfileButton = findViewById(R.id.editProfileButton);
        updateMeasurementsButton = findViewById(R.id.updateMeasurementsButton);
        nameText = findViewById(R.id.nameText);
        ageText = findViewById(R.id.ageText);
        heightText = findViewById(R.id.heightText);
        chestText = findViewById(R.id.chestText);
        waistText = findViewById(R.id.waistText);
        hipsText = findViewById(R.id.hipsText);
        // notifications removed from layout
        logoutOption = findViewById(R.id.logoutOption);
        deleteAccountOption = findViewById(R.id.deleteAccountOption);
        
        // Style preference views
        clothingPreferenceText = findViewById(R.id.clothingPreferenceText);
        stylePreferenceText = findViewById(R.id.stylePreferenceText);
        updatePreferencesButton = findViewById(R.id.updatePreferencesButton);
       
    }

    private void setupClickListeners() {
        // Navigation
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        // Profile actions
        editProfileButton.setOnClickListener(v -> handleEditProfile());
        profilePicture.setOnClickListener(v -> chooseProfileImage());
        updateMeasurementsButton.setOnClickListener(v -> handleUpdateMeasurements());
        updatePreferencesButton.setOnClickListener(v -> handleUpdatePreferences());
        
        // General options
        logoutOption.setOnClickListener(v -> handleLogout());
        deleteAccountOption.setOnClickListener(v -> handleDeleteAccount());
        
    }

    private void loadUserData() {
        userDataManager.getCurrentUserProfile(new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    if (user != null) {
                        // Name / username
                        String display = user.getDisplayName() != null ? user.getDisplayName() : (user.getUsername() != null ? user.getUsername() : "User");
                        nameText.setText("Name: " + display);

                        // Age display
                        if (user.getAge() != null) {
                            ageText.setText(getString(R.string.age_label) + user.getAge());
                        } else {
                            ageText.setText(getString(R.string.age_not_set));
                        }

                        // Load style preferences directly from the user object
                        displayStylePreferences(user);
                        
                        // Load profile image if available
                        try {
                            java.lang.reflect.Method getMethod = user.getClass().getMethod("getProfileImageUrl");
                            Object urlObj = getMethod.invoke(user);
                            if (urlObj instanceof String) {
                                String url = (String) urlObj;
                                if (url != null && !url.isEmpty()) {
                                    // Use Glide if available, otherwise fallback to default
                                    try {
                                        com.bumptech.glide.Glide.with(SettingsActivity.this)
                                                .load(url)
                                                .placeholder(R.drawable.ic_person_placeholder)
                                                .into(profilePicture);
                                    } catch (Throwable ignored) {}
                                }
                            }
                        } catch (Exception ignored) {}

                        // Measurements (in cm). Fallbacks only show label when null
                        if (user.getHeight() != null) heightText.setText("Height(cm): " + String.format("%.1f", user.getHeight()));
                        if (user.getChest() != null) chestText.setText("Chest(cm): " + String.format("%.1f", user.getChest()));
                        if (user.getWaist() != null) waistText.setText("Waist(cm): " + String.format("%.1f", user.getWaist()));
                        if (user.getHips() != null) hipsText.setText("Hips(cm): " + String.format("%.1f", user.getHips()));
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void chooseProfileImage() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        startActivityForResult(pickIntent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    // Optionally show preview immediately
                    profilePicture.setImageBitmap(bitmap);

                    // Compress to JPEG bytes
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                    byte[] bytes = baos.toByteArray();

                    // Upload
                    userDataManager.uploadProfileImage(bytes, new UserDataManager.UpdateCallback() {
                        @Override
                        public void onSuccess(String message) {
                            runOnUiThread(() -> Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show());
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to process image", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void handleEditProfile() {
        // Create a custom layout for the edit profile dialog
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Name input
        android.widget.EditText nameInput = new android.widget.EditText(this);
        nameInput.setHint(getString(R.string.display_name_hint));
        nameInput.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(nameInput);

        // Age input
        android.widget.EditText ageInput = new android.widget.EditText(this);
        ageInput.setHint(getString(R.string.age_hint));
        ageInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        ageInput.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(ageInput);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.edit_profile_title))
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    String newName = nameInput.getText().toString().trim();
                    String ageInputText = ageInput.getText().toString().trim();
                    
                    // Validate inputs
                    if (newName.isEmpty() && ageInputText.isEmpty()) {
                        Toast.makeText(SettingsActivity.this, getString(R.string.enter_at_least_one_field), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    Integer newAge = null;
                    if (!ageInputText.isEmpty()) {
                        try {
                            newAge = Integer.parseInt(ageInputText);
                            if (newAge < 1 || newAge > 120) {
                                Toast.makeText(SettingsActivity.this, getString(R.string.enter_valid_age_range), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(SettingsActivity.this, getString(R.string.enter_valid_age), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    
                    // Create final copies for lambda usage
                    final String finalNewName = newName;
                    final Integer finalNewAge = newAge;
                    
                    // Create user object with updates
                    User updatedUser = new User();
                    if (!newName.isEmpty()) {
                        updatedUser.setDisplayName(newName);
                    }
                    if (newAge != null) {
                        updatedUser.setAge(newAge);
                    }
                    
                    userDataManager.updateCurrentUserData(updatedUser, new UserDataManager.UpdateCallback() {
                        @Override
                        public void onSuccess(String message) {
                            runOnUiThread(() -> {
                                if (!finalNewName.isEmpty()) {
                                    nameText.setText("Name: " + finalNewName);
                                }
                                if (finalNewAge != null) {
                                    ageText.setText(getString(R.string.age_label) + finalNewAge);
                                }
                                Toast.makeText(SettingsActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleUpdateMeasurements() {
        // Navigate to measure activity (camera-based measurement)
        Intent intent = new Intent(this, MeasureActivity.class);
        startActivity(intent);
    }


    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear user data and navigate to login
                    userDataManager.logout();
                    userDataManager.clearRememberUserPreference(this);
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone and will permanently remove all your data.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Show loading dialog
                    AlertDialog loadingDialog = new AlertDialog.Builder(this)
                            .setTitle("Deleting Account...")
                            .setMessage("Please wait while we delete your account and data.")
                            .setCancelable(false)
                            .show();
                    
                    // Call the delete user method
                    userDataManager.deleteCurrentUser(new UserDataManager.UpdateCallback() {
                        @Override
                        public void onSuccess(String message) {
                            runOnUiThread(() -> {
                                loadingDialog.dismiss();
                                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                                
                                // Clear remember user preference
                                userDataManager.clearRememberUserPreference(SettingsActivity.this);
                                
                                // Navigate to login screen after successful deletion
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                loadingDialog.dismiss();
                                Toast.makeText(SettingsActivity.this, "Failed to delete account: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    
    private void handleUpdatePreferences() {
        Intent intent = new Intent(this, StylePreferencesActivity.class);
        startActivity(intent);
    }

    private void displayStylePreferences(User user) {
        // Display clothing preference
        if (user.getClothingPreference() != null && !user.getClothingPreference().isEmpty()) {
            clothingPreferenceText.setText(user.getClothingPreference());
        } else {
            clothingPreferenceText.setText("Not set");
        }
        
        
        // Display style preference
        if (user.getStylePreference() != null && !user.getStylePreference().isEmpty()) {
            stylePreferenceText.setText(user.getStylePreference());
        } else {
            stylePreferenceText.setText("Not set");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
