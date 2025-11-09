package com.aurafit.AuraFitApp.ui.reservation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.ui.reservation.adapter.ReservedItemAdapter;
import com.aurafit.AuraFitApp.ui.reservation.model.ReservationItem;
import com.aurafit.AuraFitApp.ui.reservation.service.ReservationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class ReservedItemsActivity extends AppCompatActivity {

    private RecyclerView reservedItemsRecyclerView;
    private ReservedItemAdapter adapter;
    private LinearLayout loadingContainer;
    private LinearLayout emptyContainer;
    private Button continueButton;
    private ReservationService reservationService;
    private List<ReservationItem> reservedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserved_items);

        initializeViews();
        setupClickListeners();
        initializeData();
    }

    private void initializeViews() {
        // Back button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // RecyclerView
        reservedItemsRecyclerView = findViewById(R.id.reservedItemsRecyclerView);
        reservedItemsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Other views
        loadingContainer = findViewById(R.id.loadingContainer);
        emptyContainer = findViewById(R.id.emptyContainer);
        continueButton = findViewById(R.id.continueButton);

        // Initialize adapter
        reservedItems = new ArrayList<>();
        adapter = new ReservedItemAdapter(reservedItems);
        reservedItemsRecyclerView.setAdapter(adapter);

        // Initialize service
        reservationService = new ReservationService();
    }

    private void setupClickListeners() {
        // Continue button
        continueButton.setOnClickListener(v -> handleContinue());

        // Item click listener
        adapter.setOnItemClickListener(new ReservedItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ReservationItem reservationItem) {
                handleItemClick(reservationItem);
            }
        });
    }

    private void initializeData() {
        showLoadingState();
        loadReservedItems();
    }

    private void showLoadingState() {
        loadingContainer.setVisibility(View.VISIBLE);
        reservedItemsRecyclerView.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showContentState() {
        loadingContainer.setVisibility(View.GONE);
        reservedItemsRecyclerView.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingContainer.setVisibility(View.GONE);
        reservedItemsRecyclerView.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.VISIBLE);
    }

    private void loadReservedItems() {
        // Check authentication with UID
        if (!reservationService.isUserAuthenticated()) {
            Toast.makeText(this, "Please log in to view reservations", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Get current user UID for logging
        String currentUserId = reservationService.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Fetch reservations for the authenticated user using UID
        reservationService.getUserReservations(
            new OnSuccessListener<List<ReservationItem>>() {
                @Override
                public void onSuccess(List<ReservationItem> items) {
                    reservedItems.clear();
                    reservedItems.addAll(items);
                    adapter.updateData(reservedItems);

                    if (items.isEmpty()) {
                        showEmptyState();
                    } else {
                        showContentState();
                        // Show success message with count
                        String message = String.format("Found %d reserved items", items.size());
                        Toast.makeText(ReservedItemsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            },
            new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    String errorMessage = "Failed to load reservations";
                    
                    // Handle specific error cases
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("not authenticated")) {
                            errorMessage = "Authentication error. Please log in again.";
                        } else if (e.getMessage().contains("network")) {
                            errorMessage = "Network error. Please check your connection.";
                        }
                    }
                    
                    Toast.makeText(ReservedItemsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    showEmptyState();
                }
            }
        );
    }

    private void handleItemClick(ReservationItem reservationItem) {
        // Show item details and allow status updates
        String message = String.format("Item: %s\nSize: %s\nStatus: %s\nPrice: ₱%.2f", 
            reservationItem.getItemName(), 
            reservationItem.getSelectedSize(),
            reservationItem.getStatus(),
            reservationItem.getItemPrice());
        
        // For now, show a toast with details
        // In the future, this could open a detailed view or allow status updates
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // TODO: Implement detailed item view or status update functionality
    }

    private void handleContinue() {
        if (reservedItems.isEmpty()) {
            Toast.makeText(this, "No items to continue with", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if all items are in valid status for continuation
        boolean hasValidItems = false;
        for (ReservationItem item : reservedItems) {
            if ("reserved".equals(item.getStatus()) || "confirmed".equals(item.getStatus())) {
                hasValidItems = true;
                break;
            }
        }

        if (!hasValidItems) {
            Toast.makeText(this, "No valid items to continue with", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        showLoadingState();
        continueButton.setEnabled(false);
        continueButton.setText("Processing...");

        // Save to reservedItems/{UID}/items and delete from cart
        reservationService.continueReservations(
            reservedItems,
            new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Success - items saved to user's reservedItems collection and removed from cart
                    String message = String.format("Successfully processed %d reserved items", reservedItems.size());
                    Toast.makeText(ReservedItemsActivity.this, message, Toast.LENGTH_LONG).show();
                    
                    // Clear the current list since items are now in user's collection
                    reservedItems.clear();
                    adapter.updateData(reservedItems);
                    showEmptyState();
                    
                    // Reset button
                    continueButton.setEnabled(true);
                    continueButton.setText("Continue");
                }
            },
            new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // Error handling
                    String errorMessage = "Failed to process reservations. Please try again.";
                    
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("not authenticated")) {
                            errorMessage = "Authentication error. Please log in again.";
                        } else if (e.getMessage().contains("network")) {
                            errorMessage = "Network error. Please check your connection.";
                        }
                    }
                    
                    Toast.makeText(ReservedItemsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    
                    // Reset button and show content
                    continueButton.setEnabled(true);
                    continueButton.setText("Continue");
                    showContentState();
                }
            }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (reservationService != null && reservationService.isUserAuthenticated()) {
            loadReservedItems();
        }
    }

    public void refreshReservedItems() {
        if (reservationService != null && reservationService.isUserAuthenticated()) {
            showLoadingState();
            loadReservedItems();
        }
    }

    private void checkReservationCount() {
        if (reservationService != null && reservationService.isUserAuthenticated()) {
            reservationService.getUserReservationCount(
                new OnSuccessListener<Integer>() {
                    @Override
                    public void onSuccess(Integer count) {
                        // Update UI based on count if needed
                        if (count == 0) {
                            showEmptyState();
                        }
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle count check failure
                        Log.e("ReservedItemsActivity", "Failed to get reservation count", e);
                    }
                }
            );
        }
    }

    private void loadUserReservedItems() {
        if (!reservationService.isUserAuthenticated()) {
            Toast.makeText(this, "Please log in to view your reserved items", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        showLoadingState();

        reservationService.getUserReservedItems(
            new OnSuccessListener<List<ReservationItem>>() {
                @Override
                public void onSuccess(List<ReservationItem> items) {
                    reservedItems.clear();
                    reservedItems.addAll(items);
                    adapter.updateData(reservedItems);

                    if (items.isEmpty()) {
                        showEmptyState();
                    } else {
                        showContentState();
                        String message = String.format("Found %d reserved items in your collection", items.size());
                        Toast.makeText(ReservedItemsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            },
            new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    String errorMessage = "Failed to load your reserved items";
                    
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("not authenticated")) {
                            errorMessage = "Authentication error. Please log in again.";
                        } else if (e.getMessage().contains("network")) {
                            errorMessage = "Network error. Please check your connection.";
                        }
                    }
                    
                    Toast.makeText(ReservedItemsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    showEmptyState();
                }
            }
        );
    }
}
