package com.aurafit.AuraFitApp.ui.measure;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.UserDataManager;
import com.aurafit.AuraFitApp.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class EditMeasurementsActivity extends AppCompatActivity {

    // UI Components
    private ImageView backButton;
    private TextView unitCentimeter;
    private TextView unitInches;
    private EditText heightInput;
    private EditText chestInput;
    private EditText waistInput;
    private EditText hipsInput;
    private EditText shoeSizeInput;
    private Button continueButton;
    
    // Measurement labels
    private TextView heightLabel;
    private TextView chestLabel;
    private TextView waistLabel;
    private TextView hipsLabel;

    // Measurement points for visual feedback
    private View heightPoint;
    private View chestLeftPoint, chestRightPoint;
    private View waistLeftPoint, waistRightPoint;
    private View hipLeftPoint, hipRightPoint;
    private View shoeSizeLeftPoint, shoeSizeRightPoint;

    // State management
    private boolean isMetricUnits = true;
    private UserDataManager userDataManager;
    private List<EditText> measurementInputs;

    // Intent extras keys
    public static final String EXTRA_HEIGHT = "height";
    public static final String EXTRA_CHEST = "chest";
    public static final String EXTRA_WAIST = "waist";
    public static final String EXTRA_HIPS = "hips";
    public static final String EXTRA_SHOE_SIZE = "shoe_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_measurements);

        userDataManager = UserDataManager.getInstance();

        initializeViews();
        setupClickListeners();
        setupTextWatchers();
        loadMeasurementsFromIntent();
        updateContinueButtonState();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        unitCentimeter = findViewById(R.id.unitCentimeter);
        unitInches = findViewById(R.id.unitInches);
        heightInput = findViewById(R.id.heightInput);
        chestInput = findViewById(R.id.chestInput);
        waistInput = findViewById(R.id.waistInput);
        hipsInput = findViewById(R.id.hipsInput);
        shoeSizeInput = findViewById(R.id.shoeSizeInput);
        continueButton = findViewById(R.id.continueButton);
        
        // Initialize measurement labels
        heightLabel = findViewById(R.id.heightLabel);
        chestLabel = findViewById(R.id.chestLabel);
        waistLabel = findViewById(R.id.waistLabel);
        hipsLabel = findViewById(R.id.hipsLabel);

        // Measurement points
        heightPoint = findViewById(R.id.heightPoint);
        chestLeftPoint = findViewById(R.id.chestLeftPoint);
        chestRightPoint = findViewById(R.id.chestRightPoint);
        waistLeftPoint = findViewById(R.id.waistLeftPoint);
        waistRightPoint = findViewById(R.id.waistRightPoint);
        hipLeftPoint = findViewById(R.id.hipLeftPoint);
        hipRightPoint = findViewById(R.id.hipRightPoint);
        shoeSizeLeftPoint = findViewById(R.id.shoeSizeLeftPoint);
        shoeSizeRightPoint = findViewById(R.id.shoeSizeRightPoint);

        // Create list of measurement inputs for easy iteration
        measurementInputs = new ArrayList<>();
        measurementInputs.add(heightInput);
        measurementInputs.add(chestInput);
        measurementInputs.add(waistInput);
        measurementInputs.add(hipsInput);
        measurementInputs.add(shoeSizeInput);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        unitCentimeter.setOnClickListener(v -> {
            if (!isMetricUnits) {
                switchToMetric();
            }
        });

        unitInches.setOnClickListener(v -> {
            if (isMetricUnits) {
                switchToImperial();
            }
        });

        continueButton.setOnClickListener(v -> saveMeasurements());
    }

    private void setupTextWatchers() {
        TextWatcher measurementWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateContinueButtonState();
                highlightMeasurementPoints();
            }
        };

        // Add text watchers to all measurement inputs
        for (EditText input : measurementInputs) {
            input.addTextChangedListener(measurementWatcher);
        }
    }

    private void loadMeasurementsFromIntent() {
        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_HEIGHT)) {
            heightInput.setText(String.valueOf(intent.getIntExtra(EXTRA_HEIGHT, 0)));
        }
        if (intent.hasExtra(EXTRA_CHEST)) {
            chestInput.setText(String.valueOf(intent.getIntExtra(EXTRA_CHEST, 0)));
        }
        if (intent.hasExtra(EXTRA_WAIST)) {
            waistInput.setText(String.valueOf(intent.getIntExtra(EXTRA_WAIST, 0)));
        }
        if (intent.hasExtra(EXTRA_HIPS)) {
            hipsInput.setText(String.valueOf(intent.getIntExtra(EXTRA_HIPS, 0)));
        }
        if (intent.hasExtra(EXTRA_SHOE_SIZE)) {
            shoeSizeInput.setText(String.valueOf(intent.getFloatExtra(EXTRA_SHOE_SIZE, 0)));
        }
    }

    private void switchToMetric() {
        if (!isMetricUnits) {
            isMetricUnits = true;
            updateUnitButtons();
            convertMeasurementsToMetric();
            updateLabels();
        }
    }

    private void switchToImperial() {
        if (isMetricUnits) {
            isMetricUnits = false;
            updateUnitButtons();
            convertMeasurementsToImperial();
            updateLabels();
        }
    }

    private void updateUnitButtons() {
        if (isMetricUnits) {
            unitCentimeter.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6B35")));
            unitCentimeter.setTextColor(Color.WHITE);
            unitInches.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            unitInches.setTextColor(Color.parseColor("#666666"));
        } else {
            unitInches.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6B35")));
            unitInches.setTextColor(Color.WHITE);
            unitCentimeter.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            unitCentimeter.setTextColor(Color.parseColor("#666666"));
        }
    }

    private void convertMeasurementsToMetric() {
        // Convert inches to centimeters (multiply by 2.54)
        convertInputValue(heightInput, 2.54f);
        convertInputValue(chestInput, 2.54f);
        convertInputValue(waistInput, 2.54f);
        convertInputValue(hipsInput, 2.54f);
        // Shoe size conversion is different, keep as is for now
    }

    private void convertMeasurementsToImperial() {
        // Convert centimeters to inches (divide by 2.54)
        convertInputValue(heightInput, 1 / 2.54f);
        convertInputValue(chestInput, 1 / 2.54f);
        convertInputValue(waistInput, 1 / 2.54f);
        convertInputValue(hipsInput, 1 / 2.54f);
        // Shoe size conversion is different, keep as is for now
    }

    private void convertInputValue(EditText input, float factor) {
        String currentValue = input.getText().toString();
        if (!currentValue.isEmpty()) {
            try {
                float value = Float.parseFloat(currentValue);
                float convertedValue = value * factor;
                input.setText(String.format("%.1f", convertedValue));
            } catch (NumberFormatException e) {
                // Ignore conversion if value is not a valid number
            }
        }
    }

    private void updateLabels() {
        String unit = isMetricUnits ? "(cm)" : "(in)";

        // Update all measurement labels
        if (heightLabel != null) {
            heightLabel.setText("Height " + unit);
        }
        if (chestLabel != null) {
            chestLabel.setText("Chest " + unit);
        }
        if (waistLabel != null) {
            waistLabel.setText("Waist " + unit);
        }
        if (hipsLabel != null) {
            hipsLabel.setText("Hips " + unit);
        }
    }

    private void updateContinueButtonState() {
        boolean allFieldsFilled = true;

        for (EditText input : measurementInputs) {
            if (input.getText().toString().trim().isEmpty()) {
                allFieldsFilled = false;
                break;
            }
        }

        if (allFieldsFilled) {
            continueButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6B35")));
            continueButton.setEnabled(true);
        } else {
            continueButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
            continueButton.setEnabled(true); // Keep enabled but show different color
        }
    }

    private void highlightMeasurementPoints() {
        // Highlight measurement points based on which field is being edited
        // This provides visual feedback to the user
        int activeColor = Color.parseColor("#FF6B35");
        int inactiveColor = Color.parseColor("#FFB399");

        // Reset all points to inactive color
        resetPointColors(inactiveColor);

        // Highlight active point based on current focus
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            highlightCorrespondingPoints(currentFocus, activeColor);
        }
    }

    private void resetPointColors(int color) {
        heightPoint.setBackgroundTintList(ColorStateList.valueOf(color));
        chestLeftPoint.setBackgroundTintList(ColorStateList.valueOf(color));
        chestRightPoint.setBackgroundTintList(ColorStateList.valueOf(color));
        waistLeftPoint.setBackgroundTintList(ColorStateList.valueOf(color));
        waistRightPoint.setBackgroundTintList(ColorStateList.valueOf(color));
        hipLeftPoint.setBackgroundTintList(ColorStateList.valueOf(color));
        hipRightPoint.setBackgroundTintList(ColorStateList.valueOf(color));
        shoeSizeLeftPoint.setBackgroundTintList(ColorStateList.valueOf(color));
        shoeSizeRightPoint.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void highlightCorrespondingPoints(View focused, int color) {
        ColorStateList colorState = ColorStateList.valueOf(color);

        if (focused == heightInput) {
            heightPoint.setBackgroundTintList(colorState);
        } else if (focused == chestInput) {
            chestLeftPoint.setBackgroundTintList(colorState);
            chestRightPoint.setBackgroundTintList(colorState);
        } else if (focused == waistInput) {
            waistLeftPoint.setBackgroundTintList(colorState);
            waistRightPoint.setBackgroundTintList(colorState);
        } else if (focused == hipsInput) {
            hipLeftPoint.setBackgroundTintList(colorState);
            hipRightPoint.setBackgroundTintList(colorState);
        } else if (focused == shoeSizeInput) {
            shoeSizeLeftPoint.setBackgroundTintList(colorState);
            shoeSizeRightPoint.setBackgroundTintList(colorState);
        }
    }

    private void saveMeasurements() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Get all measurements
            float height = Float.parseFloat(heightInput.getText().toString().trim());
            float chest = Float.parseFloat(chestInput.getText().toString().trim());
            float waist = Float.parseFloat(waistInput.getText().toString().trim());
            float hips = Float.parseFloat(hipsInput.getText().toString().trim());
            float shoeSize = Float.parseFloat(shoeSizeInput.getText().toString().trim());

            // Convert to metric if currently in imperial
            if (!isMetricUnits) {
                height *= 2.54f;
                chest *= 2.54f;
                waist *= 2.54f;
                hips *= 2.54f;
                // Keep shoe size as is
            }

            // Save separate float fields to Firestore
            userDataManager.updateCurrentUserMeasurements(height, chest, waist, hips, shoeSize, new UserDataManager.UpdateCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditMeasurementsActivity.this,
                                getString(R.string.measurements_saved_success),
                                Toast.LENGTH_SHORT).show();

                        // Go to Style Preferences screen next
                        Intent intent = new Intent(EditMeasurementsActivity.this,
                                com.aurafit.AuraFitApp.ui.preferences.StylePreferencesActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditMeasurementsActivity.this,
                                getString(R.string.measurements_save_failed, error),
                                Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.enter_valid_measurements), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        List<EditText> emptyFields = new ArrayList<>();

        for (EditText input : measurementInputs) {
            if (input.getText().toString().trim().isEmpty()) {
                emptyFields.add(input);
            }
        }

        if (!emptyFields.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            emptyFields.get(0).requestFocus();
            return false;
        }

        // Validate ranges (in metric)
        try {
            float height = Float.parseFloat(heightInput.getText().toString().trim());
            float chest = Float.parseFloat(chestInput.getText().toString().trim());
            float waist = Float.parseFloat(waistInput.getText().toString().trim());
            float hips = Float.parseFloat(hipsInput.getText().toString().trim());
            float shoeSize = Float.parseFloat(shoeSizeInput.getText().toString().trim());

            // Convert to metric for validation if in imperial
            if (!isMetricUnits) {
                height *= 2.54f;
                chest *= 2.54f;
                waist *= 2.54f;
                hips *= 2.54f;
            }

            // Validate reasonable ranges (in cm) - only maximum values
            if (height > 250) {
                Toast.makeText(this, getString(R.string.valid_height_range), Toast.LENGTH_SHORT).show();
                heightInput.requestFocus();
                return false;
            }

            if (chest > 150) {
                Toast.makeText(this, getString(R.string.valid_chest_range), Toast.LENGTH_SHORT).show();
                chestInput.requestFocus();
                return false;
            }

            if (shoeSize > 20) {
                Toast.makeText(this, getString(R.string.valid_shoe_size_range), Toast.LENGTH_SHORT).show();
                shoeSizeInput.requestFocus();
                return false;
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.enter_valid_numeric), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}