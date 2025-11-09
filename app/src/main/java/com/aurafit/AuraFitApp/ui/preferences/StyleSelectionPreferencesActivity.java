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

public class StyleSelectionPreferencesActivity extends AppCompatActivity {

    private Button casualButton;
    private Button elegantButton;
    private Button chicButton;
    private Button discardButton;
    private Button doneButton;
    private ImageView backButton;
    
    private String selectedStyle = "Casual"; // Default selection
    private UserDataManager userDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_selection_preferences);

        userDataManager = UserDataManager.getInstance();
        initializeViews();
        setupClickListeners();
        setInitialSelection();
    }

    private void initializeViews() {
        casualButton = findViewById(R.id.casualButton);
        elegantButton = findViewById(R.id.elegantButton);
        chicButton = findViewById(R.id.chicButton);
        discardButton = findViewById(R.id.discardButton);
        doneButton = findViewById(R.id.doneButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        // Style selection buttons
        casualButton.setOnClickListener(v -> selectStyle("Casual", casualButton));
        elegantButton.setOnClickListener(v -> selectStyle("Elegant", elegantButton));
        chicButton.setOnClickListener(v -> selectStyle("Chic", chicButton));

        // Action buttons
        discardButton.setOnClickListener(v -> discardChanges());
        doneButton.setOnClickListener(v -> saveAndFinish());
        backButton.setOnClickListener(v -> finish());
    }

    private void selectStyle(String style, Button selectedButton) {
        // Reset all buttons to unselected state
        resetButtonSelection();
        
        // Set the selected button as selected
        selectedButton.setSelected(true);
        selectedStyle = style;
        
        // Programmatically set backgrounds and text
        casualButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        elegantButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        chicButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        
        // Reset text for all buttons
        casualButton.setText(getString(R.string.style_casual));
        elegantButton.setText(getString(R.string.style_elegant));
        chicButton.setText(getString(R.string.style_chic));
        
        // Set selected button to selected background with outline and add checkmark
        selectedButton.setBackgroundResource(R.drawable.style_button_background_selected_outline);
        selectedButton.setText("✓ " + selectedButton.getText().toString());
        
        // Optional: Show a brief feedback
        Toast.makeText(this, "Selected: " + style, Toast.LENGTH_SHORT).show();
    }

    private void resetButtonSelection() {
        casualButton.setSelected(false);
        elegantButton.setSelected(false);
        chicButton.setSelected(false);
    }

    private void setInitialSelection() {
        // Set Casual as initially selected
        casualButton.setSelected(true);
        
        // Set initial backgrounds
        casualButton.setBackgroundResource(R.drawable.style_button_background_selected_outline);
        elegantButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        chicButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        
        // Set initial text with checkmark for selected button
        casualButton.setText("✓ " + getString(R.string.style_casual));
        elegantButton.setText(getString(R.string.style_elegant));
        chicButton.setText(getString(R.string.style_chic));
    }

    private void discardChanges() {
        // Reset to default selection
        resetButtonSelection();
        casualButton.setSelected(true);
        selectedStyle = "Casual";
        
        // Reset backgrounds
        casualButton.setBackgroundResource(R.drawable.style_button_background_selected_outline);
        elegantButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        chicButton.setBackgroundResource(R.drawable.style_button_background_unselected);
        
        // Reset text with checkmark for selected button
        casualButton.setText("✓ " + getString(R.string.style_casual));
        elegantButton.setText(getString(R.string.style_elegant));
        chicButton.setText(getString(R.string.style_chic));
        
        // Optional: Show feedback
        Toast.makeText(this, "Changes discarded", Toast.LENGTH_SHORT).show();
    }

    private void saveAndFinish() {
        // Save style preference to Firestore under current user
        userDataManager.updateStylePreferences(null, selectedStyle, new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    Toast.makeText(StyleSelectionPreferencesActivity.this,
                            "Style preference saved: " + selectedStyle,
                            Toast.LENGTH_SHORT).show();

                    // Return to Settings
                    Intent intent = new Intent(StyleSelectionPreferencesActivity.this,
                            com.aurafit.AuraFitApp.ui.settings.SettingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(StyleSelectionPreferencesActivity.this,
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




