package com.aurafit.AuraFitApp.data;

import com.aurafit.AuraFitApp.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FirestoreManager {

    private static FirestoreManager instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private FirestoreManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

 
    public interface DatabaseCallback {
        void onSuccess();
        void onFailure(Exception e);
    }


    public void saveUserToDatabase(User user, DatabaseCallback callback) {
        DocumentReference userRef = firestore.collection("users").document(user.getUserId());
        
        userRef.set(user)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }

   
    public void getUserFromDatabase(String userId, UserCallback callback) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (callback != null) callback.onSuccess(user);
                } else {
                    if (callback != null) callback.onFailure(new Exception("User not found"));
                }
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }


    public void getCurrentUserFromDatabase(UserCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onFailure(new Exception("No authenticated user"));
            return;
        }
        
        getUserFromDatabase(currentUser.getUid(), callback);
    }

 
    public void fetchAndUpdateUser(String userId, User updatedUser, DatabaseCallback callback) {
        getUserFromDatabase(userId, new UserCallback() {
            @Override
            public void onSuccess(User existingUser) {
                
                User mergedUser = mergeUserData(existingUser, updatedUser);
                
                
                updateUserInDatabase(mergedUser, callback);
            }

            @Override
            public void onFailure(Exception e) {
                
                if (e.getMessage() != null && e.getMessage().contains("User not found")) {
                    saveUserToDatabase(updatedUser, callback);
                } else {
                    if (callback != null) callback.onFailure(e);
                }
            }
        });
    }

  
    public void fetchAndUpdateCurrentUser(User updatedUser, DatabaseCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onFailure(new Exception("No authenticated user"));
            return;
        }
        
        
        updatedUser.setUserId(currentUser.getUid());
        fetchAndUpdateUser(currentUser.getUid(), updatedUser, callback);
    }

 
    public void updateUserInDatabase(User user, DatabaseCallback callback) {
        DocumentReference userRef = firestore.collection("users").document(user.getUserId());
        
        userRef.update(
            "username", user.getUsername(),
            "email", user.getEmail(),
            "displayName", user.getDisplayName(),
            "age", user.getAge(),
            "lastLoginAt", user.getLastLoginAt(),
            "onboardingCompleted", user.isOnboardingCompleted(),
            "clothingPreference", user.getClothingPreference(),
            "stylePreference", user.getStylePreference()
            
        )
        .addOnSuccessListener(aVoid -> {
            if (callback != null) callback.onSuccess();
        })
        .addOnFailureListener(e -> {
            if (callback != null) callback.onFailure(e);
        });
    }

    public void updateCurrentUserMeasurements(float height, float chest, float waist, float hips, float shoeSize, DatabaseCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onFailure(new Exception("No authenticated user"));
            return;
        }

        DocumentReference userRef = firestore.collection("users").document(currentUser.getUid());
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("height", height);
        updates.put("chest", chest);
        updates.put("waist", waist);
        updates.put("hips", hips);
        updates.put("shoeSize", shoeSize);
        
        updates.put("body_measurements_captured", true);

        userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }

    public void deleteUserFromDatabase(String userId, DatabaseCallback callback) {
        firestore.collection("users").document(userId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void uploadProfileImage(byte[] imageData, DatabaseCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onFailure(new Exception("No authenticated user"));
            return;
        }

        String path = "users/" + currentUser.getUid() + "/profile.jpg";
        StorageReference ref = storage.getReference().child(path);
        ref.putBytes(imageData)
            .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                
                DocumentReference userRef = firestore.collection("users").document(currentUser.getUid());
                java.util.Map<String, Object> updates = new java.util.HashMap<>();
                updates.put("profileImageUrl", uri.toString());
                userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                    .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e); });
            }).addOnFailureListener(e -> { if (callback != null) callback.onFailure(e); }))
            .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e); });
    }

    public void updateLastLoginTime(String userId, DatabaseCallback callback) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        
        
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("lastLoginAt", new java.util.Date());
        
        
        
        userRef.update(updates)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                
                
                if (callback != null) callback.onFailure(e);
            });
    }
    
    public void updateLastLoginTime(String userId, String email, String displayName, DatabaseCallback callback) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        
        
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("lastLoginAt", new java.util.Date());
        
        
        if (email != null) {
            updates.put("email", email);
        }
        if (displayName != null) {
            updates.put("displayName", displayName);
            updates.put("username", displayName); 
        }
        if (email != null || displayName != null) {
            updates.put("createdAt", new java.util.Date());
        }
        
        
        userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    private User mergeUserData(User existingUser, User updatedUser) {
        User mergedUser = new User();
        
        
        mergedUser.setUserId(existingUser.getUserId());
        
        
        mergedUser.setUsername(updatedUser.getUsername() != null ? 
            updatedUser.getUsername() : existingUser.getUsername());
        
        mergedUser.setEmail(updatedUser.getEmail() != null ? 
            updatedUser.getEmail() : existingUser.getEmail());
        
        mergedUser.setDisplayName(updatedUser.getDisplayName() != null ? 
            updatedUser.getDisplayName() : existingUser.getDisplayName());
        
        mergedUser.setAge(updatedUser.getAge() != null ? 
            updatedUser.getAge() : existingUser.getAge());
        
        
        mergedUser.setCreatedAt(existingUser.getCreatedAt());
        mergedUser.setLastLoginAt(updatedUser.getLastLoginAt() != null ? 
            updatedUser.getLastLoginAt() : existingUser.getLastLoginAt());
        
        
        mergedUser.setOnboardingCompleted(updatedUser.isOnboardingCompleted() || existingUser.isOnboardingCompleted());
        mergedUser.setPreferredStyle(updatedUser.getPreferredStyle() != null ? 
            updatedUser.getPreferredStyle() : existingUser.getPreferredStyle());
        
        
        mergedUser.setClothingPreference(updatedUser.getClothingPreference() != null ? 
            updatedUser.getClothingPreference() : existingUser.getClothingPreference());
        mergedUser.setStylePreference(updatedUser.getStylePreference() != null ? 
            updatedUser.getStylePreference() : existingUser.getStylePreference());
        
        
        mergedUser.setHeight(updatedUser.getHeight() != null ? 
            updatedUser.getHeight() : existingUser.getHeight());
        mergedUser.setChest(updatedUser.getChest() != null ? 
            updatedUser.getChest() : existingUser.getChest());
        mergedUser.setWaist(updatedUser.getWaist() != null ? 
            updatedUser.getWaist() : existingUser.getWaist());
        mergedUser.setHips(updatedUser.getHips() != null ? 
            updatedUser.getHips() : existingUser.getHips());
        mergedUser.setShoeSize(updatedUser.getShoeSize() != null ? 
            updatedUser.getShoeSize() : existingUser.getShoeSize());
        mergedUser.setBody_measurements_captured(updatedUser.getBody_measurements_captured() != null ? 
            updatedUser.getBody_measurements_captured() : existingUser.getBody_measurements_captured());
        mergedUser.setProfileImageUrl(updatedUser.getProfileImageUrl() != null ? 
            updatedUser.getProfileImageUrl() : existingUser.getProfileImageUrl());
        
        return mergedUser;
    }

    public void getUserProfileWithAuthFallback(UserCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onFailure(new Exception("No authenticated user"));
            return;
        }
        
        getCurrentUserFromDatabase(new UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (callback != null) callback.onSuccess(user);
            }

            @Override
            public void onFailure(Exception e) {
                
                User authUser = new User(
                    currentUser.getUid(),
                    currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User",
                    currentUser.getEmail() != null ? currentUser.getEmail() : "",
                    currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User"
                );
                
                
                saveUserToDatabase(authUser, new DatabaseCallback() {
                    @Override
                    public void onSuccess() {
                        if (callback != null) callback.onSuccess(authUser);
                    }

                    @Override
                    public void onFailure(Exception saveError) {
                        
                        if (callback != null) callback.onSuccess(authUser);
                    }
                });
            }
        });
    }

    public void getWardrobeItems(WardrobeItemsCallback callback) {
        firestore.collection("wardrobeItems")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<com.aurafit.AuraFitApp.ui.homepage.model.WardrobeItem> items = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                    com.aurafit.AuraFitApp.ui.homepage.model.WardrobeItem item = document.toObject(com.aurafit.AuraFitApp.ui.homepage.model.WardrobeItem.class);
                    if (item != null) {
                        item.setId(document.getId());
                        items.add(item);
                    }
                }
                if (callback != null) callback.onSuccess(items);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public interface WardrobeItemsCallback {
        void onSuccess(List<com.aurafit.AuraFitApp.ui.homepage.model.WardrobeItem> items);
        void onFailure(Exception e);
    }
}
