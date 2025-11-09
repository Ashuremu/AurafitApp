package com.aurafit.AuraFitApp.ui.reservation.service;

import android.util.Log;

import com.aurafit.AuraFitApp.ui.reservation.model.ReservationItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    private static final String TAG = "ReservationService";
    private static final String COLLECTION_RESERVATIONS = "reservations";
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference reservationsRef;

    public ReservationService() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        reservationsRef = db.collection(COLLECTION_RESERVATIONS);
    }

    public void addReservation(ReservationItem reservationItem, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (auth.getCurrentUser() == null) {
            onFailure.onFailure(new Exception("User not authenticated"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        reservationItem.setUserId(userId);

        // Check for existing reservation to prevent duplicates
        checkExistingReservation(userId, reservationItem.getItemId(), reservationItem.getSelectedSize(), 
            new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean exists) {
                    if (exists) {
                        onFailure.onFailure(new Exception("Item already reserved in this size"));
                        return;
                    }
                    
                    // No duplicate found, proceed with reservation
                    reservationsRef.add(reservationItem)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "Reservation added with ID: " + documentReference.getId());
                                    reservationItem.setId(documentReference.getId());
                                    onSuccess.onSuccess(null);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Log.e(TAG, "Error adding reservation", e);
                                    onFailure.onFailure(e);
                                }
                            });
                }
            }, onFailure);
    }

    private void checkExistingReservation(String userId, String itemId, String size, 
                                        OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        // Query with fewer conditions to avoid composite index requirement
        reservationsRef.whereEqualTo("userId", userId)
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("selectedSize", size)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Filter by status in memory to avoid composite index
                            boolean exists = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ReservationItem reservation = document.toObject(ReservationItem.class);
                                if ("reserved".equals(reservation.getStatus())) {
                                    exists = true;
                                    break;
                                }
                            }
                            onSuccess.onSuccess(exists);
                        } else {
                            Log.e(TAG, "Error checking existing reservation", task.getException());
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    public void getUserReservations(OnSuccessListener<List<ReservationItem>> onSuccess, OnFailureListener onFailure) {
        if (auth.getCurrentUser() == null) {
            onFailure.onFailure(new Exception("User not authenticated"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        // Query without orderBy to avoid composite index requirement
        reservationsRef.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ReservationItem> reservations = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ReservationItem reservation = document.toObject(ReservationItem.class);
                                reservation.setId(document.getId());
                                reservations.add(reservation);
                            }
                            
                            // Sort in memory by reservedAt descending
                            reservations.sort((r1, r2) -> {
                                if (r1.getReservedAt() == null && r2.getReservedAt() == null) return 0;
                                if (r1.getReservedAt() == null) return 1;
                                if (r2.getReservedAt() == null) return -1;
                                return r2.getReservedAt().compareTo(r1.getReservedAt());
                            });
                            
                            onSuccess.onSuccess(reservations);
                        } else {
                            Log.e(TAG, "Error getting reservations", task.getException());
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    public void getUserReservationsWithOrdering(OnSuccessListener<List<ReservationItem>> onSuccess, OnFailureListener onFailure) {
        if (auth.getCurrentUser() == null) {
            onFailure.onFailure(new Exception("User not authenticated"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        // This query requires a composite index: userId (Ascending), reservedAt (Descending)
        reservationsRef.whereEqualTo("userId", userId)
                .orderBy("reservedAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ReservationItem> reservations = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ReservationItem reservation = document.toObject(ReservationItem.class);
                                reservation.setId(document.getId());
                                reservations.add(reservation);
                            }
                            onSuccess.onSuccess(reservations);
                        } else {
                            Log.e(TAG, "Error getting reservations with ordering", task.getException());
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    public void updateReservationStatus(String reservationId, String status, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        reservationsRef.document(reservationId)
                .update("status", status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Reservation status updated");
                        onSuccess.onSuccess(aVoid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Error updating reservation status", e);
                        onFailure.onFailure(e);
                    }
                });
    }

    public void removeReservation(String reservationId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        reservationsRef.document(reservationId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Reservation removed");
                        onSuccess.onSuccess(aVoid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Error removing reservation", e);
                        onFailure.onFailure(e);
                    }
                });
    }

    public boolean isUserAuthenticated() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    public String getCurrentUserEmail() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getEmail();
        }
        return null;
    }

    public void canUserReserve(OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        if (!isUserAuthenticated()) {
            onFailure.onFailure(new Exception("User not authenticated"));
            return;
        }

        // Add any additional business rules here
        // For example: check if user has reached reservation limit, etc.
        onSuccess.onSuccess(true);
    }

    public void getUserReservationCount(OnSuccessListener<Integer> onSuccess, OnFailureListener onFailure) {
        if (auth.getCurrentUser() == null) {
            onFailure.onFailure(new Exception("User not authenticated"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        reservationsRef.whereEqualTo("userId", userId)
                .whereEqualTo("status", "reserved")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            onSuccess.onSuccess(count);
                        } else {
                            Log.e(TAG, "Error getting reservation count", task.getException());
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    public void continueReservations(List<ReservationItem> reservedItems, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (auth.getCurrentUser() == null) {
            onFailure.onFailure(new Exception("User not authenticated"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        CollectionReference userReservedItemsRef = db.collection("reservedItems").document(userId).collection("items");

        // Use batch write for atomic operation
        com.google.firebase.firestore.WriteBatch batch = db.batch();

        try {
            // Add each reserved item to user's reservedItems collection
            for (ReservationItem item : reservedItems) {
                DocumentReference itemRef = userReservedItemsRef.document();
                batch.set(itemRef, item);
            }

            // Commit the batch
            batch.commit()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Reserved items saved to user collection");
                            
                            // Now delete from cart (reservations collection)
                            deleteReservedItemsFromCart(reservedItems, onSuccess, onFailure);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error saving reserved items to user collection", e);
                            onFailure.onFailure(e);
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error preparing batch write", e);
            onFailure.onFailure(e);
        }
    }

    private void deleteReservedItemsFromCart(List<ReservationItem> reservedItems, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (reservedItems == null || reservedItems.isEmpty()) {
            onSuccess.onSuccess(null);
            return;
        }

        com.google.firebase.firestore.WriteBatch batch = db.batch();

        try {
            // Delete each item from the reservations collection
            for (ReservationItem item : reservedItems) {
                if (item.getId() != null && !item.getId().isEmpty()) {
                    DocumentReference itemRef = reservationsRef.document(item.getId());
                    batch.delete(itemRef);
                }
            }

            // Commit the batch
            batch.commit()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Reserved items deleted from cart");
                            onSuccess.onSuccess(null);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error deleting reserved items from cart", e);
                            onFailure.onFailure(e);
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error preparing delete batch", e);
            onFailure.onFailure(e);
        }
    }

    public void getUserReservedItems(OnSuccessListener<List<ReservationItem>> onSuccess, OnFailureListener onFailure) {
        if (auth.getCurrentUser() == null) {
            onFailure.onFailure(new Exception("User not authenticated"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        CollectionReference userReservedItemsRef = db.collection("reservedItems").document(userId).collection("items");

        userReservedItemsRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ReservationItem> reservedItems = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ReservationItem item = document.toObject(ReservationItem.class);
                                item.setId(document.getId());
                                reservedItems.add(item);
                            }
                            onSuccess.onSuccess(reservedItems);
                        } else {
                            Log.e(TAG, "Error getting user reserved items", task.getException());
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    private boolean validateReservationData(ReservationItem reservationItem) {
        if (reservationItem == null) {
            Log.e(TAG, "Reservation item is null");
            return false;
        }
        
        if (reservationItem.getItemId() == null || reservationItem.getItemId().isEmpty()) {
            Log.e(TAG, "Item ID is required");
            return false;
        }
        
        if (reservationItem.getSelectedSize() == null || reservationItem.getSelectedSize().isEmpty()) {
            Log.e(TAG, "Selected size is required");
            return false;
        }
        
        if (reservationItem.getUserId() == null || reservationItem.getUserId().isEmpty()) {
            Log.e(TAG, "User ID is required");
            return false;
        }
        
        return true;
    }
}
