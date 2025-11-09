package com.aurafit.AuraFitApp.ui.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.UserDataManager;
import com.aurafit.AuraFitApp.data.model.User;

public class StylePreferencesActivity extends AppCompatActivity {

    private Button malePreferenceButton;
    private Button femalePreferenceButton;
    private Button mixedPreferenceButton;
    private Button discardButton;
    private Button doneButton;
    private ImageView backButton;
    
    private String selectedClothingPreference = "Male"; // Default selection
    private UserDataManager userDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_preferences);

        userDataManager = UserDataManager.getInstance();
        initializeViews();
        setupClickListeners();
        setInitialSelection();
    }

    private void initializeViews() {
        malePreferenceButton = findViewById(R.id.malePreferenceButton);
        femalePreferenceButton = findViewById(R.id.femalePreferenceButton);
        mixedPreferenceButton = findViewById(R.id.mixedPreferenceButton);
        discardButton = findViewById(R.id.discardButton);
        doneButton = findViewById(R.id.doneButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        // Clothing preference buttons
        malePreferenceButton.setOnClickListener(v -> selectClothingPreference("Male", malePreferenceButton));
        femalePreferenceButton.setOnClickListener(v -> selectClothingPreference("Female", femalePreferenceButton));
        mixedPreferenceButton.setOnClickListener(v -> selectClothingPreference("Mixed", mixedPreferenceButton));

        // Action buttons
        discardButton.setOnClickListener(v -> discardChanges());
        doneButton.setOnClickListener(v -> saveAndFinish());
        backButton.setOnClickListener(v -> finish());
    }

    private void selectClothingPreference(String preference, Button selectedButton) {
        // Reset all buttons to unselected state
        resetButtonSelection();
        
        // Set the selected button as selected
        selectedButton.setSelected(true);
        selectedClothingPreference = preference;
        
        // Programmatically set backgrounds and text
        malePreferenceButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        femalePreferenceButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        mixedPreferenceButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        
        // Reset text for all buttons
        malePreferenceButton.setText("Male");
        femalePreferenceButton.setText("Female");
        mixedPreferenceButton.setText("Mixed / All styles");
        
        // Set selected button to selected background with outline and add checkmark
        selectedButton.setBackgroundResource(R.drawable.style_button_background_selected_outline);
        selectedButton.setText("✓ " + selectedButton.getText().toString());
        
        // Optional: Show a brief feedback
        Toast.makeText(this, "Selected: " + preference, Toast.LENGTH_SHORT).show();
    }

    private void resetButtonSelection() {
        malePreferenceButton.setSelected(false);
        femalePreferenceButton.setSelected(false);
        mixedPreferenceButton.setSelected(false);
    }

    private void setInitialSelection() {
        // Set Male as initially selected
        malePreferenceButton.setSelected(true);
        
        // Set initial backgrounds
        malePreferenceButton.setBackgroundResource(R.drawable.style_button_background_selected_outline);
        femalePreferenceButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        mixedPreferenceButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        
        // Set initial text with checkmark for selected button
        malePreferenceButton.setText("✓ Male");
        femalePreferenceButton.setText("Female");
        mixedPreferenceButton.setText("Mixed / All styles");
    }

    private void discardChanges() {
        // Reset to default selection
        resetButtonSelection();
        malePreferenceButton.setSelected(true);
        selectedClothingPreference = "Male";
        
        // Reset backgrounds
        malePreferenceButton.setBackgroundResource(R.drawable.style_button_background_selected_outline);
        femalePreferenceButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        mixedPreferenceButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        
        // Reset text with checkmark for selected button
        malePreferenceButton.setText("✓ Male");
        femalePreferenceButton.setText("Female");
        mixedPreferenceButton.setText("Mixed / All styles");
        
        // Optional: Show feedback
        Toast.makeText(this, "Changes discarded", Toast.LENGTH_SHORT).show();
    }

    private void saveAndFinish() {
        // Save clothing preference to Firestore under current user
        userDataManager.updateStylePreferences(selectedClothingPreference, null, new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    Toast.makeText(StylePreferencesActivity.this,
                            "Clothing preference saved: " + selectedClothingPreference,
                            Toast.LENGTH_SHORT).show();

                    // Navigate to next page (Style Selection)
                    Intent intent = new Intent(StylePreferencesActivity.this,
                            com.aurafit.AuraFitApp.ui.preferences.StyleSelectionPreferencesActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(StylePreferencesActivity.this,
                        "Failed to save preference: " + error,
                        Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Handle back button press
        finish();
    }
}
