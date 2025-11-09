package com.aurafit.AuraFitApp.ui.reservation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.ui.homepage.model.WardrobeItem;
import com.aurafit.AuraFitApp.ui.reservation.model.ReservationItem;
import com.aurafit.AuraFitApp.ui.reservation.service.ReservationService;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class ItemReservationActivity extends AppCompatActivity {

    private WardrobeItem wardrobeItem;
    private String selectedSize = null; // No default size selected
    private Button sizeS, sizeM, sizeL;
    private Button reserveButton;
    private ReservationService reservationService;
    private TextView stockInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_reservation);

        // Get the wardrobe item from intent
        wardrobeItem = (WardrobeItem) getIntent().getSerializableExtra("wardrobeItem");
        if (wardrobeItem == null) {
            finish();
            return;
        }

        // Initialize reservation service
        reservationService = new ReservationService();

        initializeViews();
        setupClickListeners();
        populateItemData();
        updateSizeButtons(); // Update button states based on stock availability
    }

    private void initializeViews() {
        // Back button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Size buttons
        sizeS = findViewById(R.id.sizeS);
        sizeM = findViewById(R.id.sizeM);
        sizeL = findViewById(R.id.sizeL);

        // Reserve button
        reserveButton = findViewById(R.id.reserveButton);
        
        // Stock info
        stockInfo = findViewById(R.id.stockInfo);
    }

    private void setupClickListeners() {
        // Size selection listeners
        sizeS.setOnClickListener(v -> selectSize("S"));
        sizeM.setOnClickListener(v -> selectSize("M"));
        sizeL.setOnClickListener(v -> selectSize("L"));

        // Reserve button listener
        reserveButton.setOnClickListener(v -> handleReservation());
    }

    private void selectSize(String size) {
        // Debug logging
        System.out.println("selectSize called for size: " + size);
        if (wardrobeItem != null) {
            boolean isAvailable = wardrobeItem.isSizeAvailable(size);
            int stock = wardrobeItem.getStockForSize(size);
            System.out.println("Size " + size + " - Available: " + isAvailable + ", Stock: " + stock);
        }
        
        // Check if the selected size has stock available
        if (wardrobeItem != null && !wardrobeItem.isSizeAvailable(size)) {
            // Show toast message for out of stock
            Toast.makeText(this, "Size " + size + " is out of stock", Toast.LENGTH_SHORT).show();
            return;
        }
        
        selectedSize = size;
        
        // Reset all buttons to default state
        resetSizeButtons();
        
        // Set selected button to active state (black filled circle)
        Button selectedButton = getSizeButton(size);
        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.circle_filled);
            selectedButton.setTextColor(getResources().getColor(android.R.color.white));
        }
        
        // Update stock information for selected size
        updateStockInfo(size);
    }

    private void resetSizeButtons() {
        // Set default styling for unselected buttons (white circle with black outline)
        sizeS.setBackgroundResource(R.drawable.circle_outline);
        sizeS.setTextColor(getResources().getColor(android.R.color.black));
        
        sizeM.setBackgroundResource(R.drawable.circle_outline);
        sizeM.setTextColor(getResources().getColor(android.R.color.black));
        
        sizeL.setBackgroundResource(R.drawable.circle_outline);
        sizeL.setTextColor(getResources().getColor(android.R.color.black));
    }

    private Button getSizeButton(String size) {
        switch (size) {
            case "S": return sizeS;
            case "M": return sizeM;
            case "L": return sizeL;
            default: return null;
        }
    }

    private void populateItemData() {
        // Set item image
        ImageView itemImage = findViewById(R.id.itemImage);
        if (wardrobeItem.getImageUrl() != null && !wardrobeItem.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(wardrobeItem.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(itemImage);
        } else {
            itemImage.setImageResource(R.drawable.placeholder_image);
        }

        // Set item name
        TextView itemName = findViewById(R.id.itemName);
        itemName.setText(wardrobeItem.getName() != null ? wardrobeItem.getName() : "Untitled");

        // Set item price
        TextView itemPrice = findViewById(R.id.itemPrice);
        itemPrice.setText(String.format("₱%.2f", wardrobeItem.getPrice()));

        // Set initial stock info
        updateStockInfo(selectedSize);

        // Set item description
        TextView itemDescription = findViewById(R.id.itemDescription);
        itemDescription.setText(wardrobeItem.getDescription() != null ? 
            wardrobeItem.getDescription() : "No description available.");

        // Set gender
        TextView itemGender = findViewById(R.id.itemGender);
        itemGender.setText(wardrobeItem.getGender() != null ? wardrobeItem.getGender() : "Unknown");

        // Set weather
        TextView itemWeather = findViewById(R.id.itemWeather);
        itemWeather.setText(wardrobeItem.getWeather() != null ? wardrobeItem.getWeather() : "Unknown");

        // Update size buttons based on stock availability
        updateSizeButtons();
    }

    private void handleReservation() {
        // Check if a size is selected
        if (selectedSize == null) {
            Toast.makeText(this, "Please select a size first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if user is authenticated
        if (!reservationService.isUserAuthenticated()) {
            Toast.makeText(this, "Please log in to make a reservation", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if selected size is available
        if (!isSelectedSizeAvailable()) {
            Toast.makeText(this, "Selected size is out of stock", Toast.LENGTH_LONG).show();
            return;
        }

        // Show loading state
        reserveButton.setEnabled(false);
        reserveButton.setText("Reserving...");

        // Validate user can make reservation
        reservationService.canUserReserve(
            new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean canReserve) {
                    if (!canReserve) {
                        Toast.makeText(ItemReservationActivity.this, 
                            "Unable to make reservation at this time", Toast.LENGTH_LONG).show();
                        resetReserveButton();
                        return;
                    }

                    // Create reservation item with UID authentication
                    ReservationItem reservationItem = new ReservationItem(
                        reservationService.getCurrentUserId(), // This ensures UID is used
                        wardrobeItem.getId(),
                        wardrobeItem.getName(),
                        wardrobeItem.getImageUrl(),
                        wardrobeItem.getPrice(),
                        selectedSize,
                        wardrobeItem.getGender(),
                        wardrobeItem.getWeather(),
                        wardrobeItem.getDescription()
                    );

                    // Save to Firestore with UID-based authentication
                    reservationService.addReservation(
                        reservationItem,
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Success - reservation saved with unique UID
                                String message = String.format("Successfully reserved %s in size %s", 
                                    wardrobeItem.getName(), selectedSize);
                                Toast.makeText(ItemReservationActivity.this, message, Toast.LENGTH_LONG).show();
                                
                                // Navigate back to homepage
                                finish();
                            }
                        },
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                // Handle specific error cases
                                String errorMessage = "Failed to reserve item. Please try again.";
                                
                                if (e.getMessage() != null) {
                                    if (e.getMessage().contains("already reserved")) {
                                        errorMessage = "This item is already reserved in the selected size.";
                                    } else if (e.getMessage().contains("not authenticated")) {
                                        errorMessage = "Please log in to make a reservation.";
                                    }
                                }
                                
                                Toast.makeText(ItemReservationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                resetReserveButton();
                            }
                        }
                    );
                }
            },
            new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ItemReservationActivity.this, 
                        "Authentication error. Please try again.", Toast.LENGTH_LONG).show();
                    resetReserveButton();
                }
            }
        );
    }

    private void resetReserveButton() {
        reserveButton.setEnabled(true);
        reserveButton.setText("RESERVE");
    }

    private void updateStockInfo(String size) {
        if (wardrobeItem == null || stockInfo == null) {
            return;
        }

        try {
            int stock = wardrobeItem.getStockForSize(size);
            boolean isAvailable = wardrobeItem.isSizeAvailable(size);
            
            if (isAvailable) {
                stockInfo.setText(String.format("Stock: %d", stock));
                stockInfo.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                stockInfo.setText("Out of Stock");
                stockInfo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        } catch (Exception e) {
            // Fallback for any parsing errors
            stockInfo.setText("Stock: Unknown");
            stockInfo.setTextColor(getResources().getColor(android.R.color.darker_gray));
            System.err.println("Error updating stock info: " + e.getMessage());
        }
    }

    private boolean isSelectedSizeAvailable() {
        if (wardrobeItem == null || selectedSize == null) {
            return false;
        }
        return wardrobeItem.isSizeAvailable(selectedSize);
    }

    private void updateSizeButtons() {
        if (wardrobeItem == null) {
            System.out.println("updateSizeButtons: wardrobeItem is null");
            return;
        }

        try {
            // Check availability for each size
            boolean sizeSAvailable = wardrobeItem.isSizeAvailable("S");
            boolean sizeMAvailable = wardrobeItem.isSizeAvailable("M");
            boolean sizeLAvailable = wardrobeItem.isSizeAvailable("L");

            System.out.println("updateSizeButtons - S: " + sizeSAvailable + ", M: " + sizeMAvailable + ", L: " + sizeLAvailable);

            // Update button states
            updateSizeButtonState(sizeS, sizeSAvailable);
            updateSizeButtonState(sizeM, sizeMAvailable);
            updateSizeButtonState(sizeL, sizeLAvailable);
        } catch (Exception e) {
            // If there's an error, enable all buttons as fallback
            System.err.println("Error updating size buttons: " + e.getMessage());
            updateSizeButtonState(sizeS, true);
            updateSizeButtonState(sizeM, true);
            updateSizeButtonState(sizeL, true);
        }
    }

    private void updateSizeButtonState(Button button, boolean isAvailable) {
        if (button == null) {
            return;
        }

        System.out.println("updateSizeButtonState - Button: " + button.getText() + ", Available: " + isAvailable);

        if (isAvailable) {
            button.setEnabled(true);
            button.setAlpha(1.0f);
            // Reset to normal background for available sizes
            button.setBackgroundResource(R.drawable.circle_outline);
            button.setTextColor(getResources().getColor(android.R.color.black));
        } else {
            // Keep button enabled so click events work for toast messages
            button.setEnabled(true);
            button.setAlpha(0.5f);
            // Set disabled appearance but keep clickable
            button.setBackgroundResource(R.drawable.circle_outline);
            button.setTextColor(getResources().getColor(android.R.color.black));
        }
    }
}
