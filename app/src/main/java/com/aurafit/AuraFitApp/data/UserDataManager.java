package com.aurafit.AuraFitApp.data;

import android.util.Log;
import com.aurafit.AuraFitApp.data.model.User;
import com.aurafit.AuraFitApp.data.CommunityDataManager;

public class UserDataManager {
    
    private static final String TAG = "UserDataManager";
    private static UserDataManager instance;
    private LoginRepository loginRepository;
    private FirestoreManager firestoreManager;
    
    private UserDataManager() {
        loginRepository = LoginRepository.getInstance(new LoginDataSource());
        firestoreManager = FirestoreManager.getInstance();
    }
    
    public static UserDataManager getInstance() {
        if (instance == null) {
            instance = new UserDataManager();
        }
        return instance;
    }
    
    public void updateUserProfile(String newUsername, String newDisplayName, UpdateCallback callback) {
        // Create user object with only the fields to update
        User updatedUser = new User();
        updatedUser.setUsername(newUsername);
        updatedUser.setDisplayName(newDisplayName);
        
        // Fetch existing data and merge with updates
        loginRepository.updateCurrentUserData(updatedUser, new FirestoreManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "User profile updated successfully");
                if (callback != null) callback.onSuccess("Profile updated successfully");
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update user profile", e);
                if (callback != null) callback.onError("Failed to update profile: " + e.getMessage());
            }
        });
    }
    
    public void getCurrentUserProfile(ProfileCallback callback) {
        loginRepository.getUserProfile(new FirestoreManager.UserCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "User profile fetched successfully");
                if (callback != null) callback.onSuccess(user);
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch user profile", e);
                if (callback != null) callback.onError("Failed to fetch profile: " + e.getMessage());
            }
        });
    }
    
    public void updateUserEmail(String newEmail, UpdateCallback callback) {
        User updatedUser = new User();
        updatedUser.setEmail(newEmail);
        
        loginRepository.updateCurrentUserData(updatedUser, new FirestoreManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "User email updated successfully");
                if (callback != null) callback.onSuccess("Email updated successfully");
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update user email", e);
                if (callback != null) callback.onError("Failed to update email: " + e.getMessage());
            }
        });
    }
    
    public void updateCurrentUserData(User updatedUser, UpdateCallback callback) {
        loginRepository.updateCurrentUserData(updatedUser, new FirestoreManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Current user data updated successfully");
                if (callback != null) callback.onSuccess("User data updated successfully");
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update current user data", e);
                if (callback != null) callback.onError("Failed to update user data: " + e.getMessage());
            }
        });
    }

    public void updateCurrentUserMeasurements(float height, float chest, float waist, float hips, float shoeSize, UpdateCallback callback) {
        firestoreManager.updateCurrentUserMeasurements(height, chest, waist, hips, shoeSize, new FirestoreManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) callback.onSuccess("Measurements updated successfully");
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onError("Failed to update measurements: " + e.getMessage());
            }
        });
    }
    
    public void updateUserByUid(String userId, User updatedUser, UpdateCallback callback) {
        loginRepository.updateUserData(userId, updatedUser, new FirestoreManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "User data updated successfully for UID: " + userId);
                if (callback != null) callback.onSuccess("User data updated successfully");
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update user data for UID: " + userId, e);
                if (callback != null) callback.onError("Failed to update user data: " + e.getMessage());
            }
        });
    }
    
    public interface UpdateCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public void logout() {
        loginRepository.logout();
        Log.d(TAG, "User logged out successfully");
    }
    
    public void clearRememberUserPreference(android.content.Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE);
        prefs.edit().putBoolean("remember_user", false).apply();
        Log.d(TAG, "Remember user preference cleared");
    }
    
    public boolean isLoggedIn() {
        return loginRepository.isLoggedIn();
    }

    public void uploadProfileImage(byte[] data, UpdateCallback callback) {
        firestoreManager.uploadProfileImage(data, new FirestoreManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) callback.onSuccess("Profile image updated");
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onError("Failed to upload profile image: " + e.getMessage());
            }
        });
    }
    
    public void updateStylePreferences(String clothingPreference, String stylePreference, ProfileCallback callback) {
        // Create user object with style preferences to update
        User updatedUser = new User();
        if (clothingPreference != null) updatedUser.setClothingPreference(clothingPreference);
        if (stylePreference != null) updatedUser.setStylePreference(stylePreference);

        firestoreManager.fetchAndUpdateCurrentUser(updatedUser, new FirestoreManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                // Get the updated user profile to return
                getCurrentUserProfile(callback);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onError("Failed to update style preferences: " + e.getMessage());
            }
        });
    }
    
    public void deleteCurrentUser(UpdateCallback callback) {
        // Get current user ID before deletion
        String currentUserId = firestoreManager.getCurrentUser() != null ? 
            firestoreManager.getCurrentUser().getUid() : null;
        
        if (currentUserId == null) {
            if (callback != null) callback.onError("No authenticated user found");
            return;
        }
        
        // First delete community data (posts, comments, follows)
        CommunityDataManager communityDataManager = CommunityDataManager.getInstance();
        communityDataManager.deleteUserCommunityData(currentUserId, new CommunityDataManager.UpdateCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "Community data deleted: " + message);
                
                // Then delete user data from Firestore
                firestoreManager.deleteUserFromDatabase(currentUserId, new FirestoreManager.DatabaseCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "User data deleted from Firestore successfully");
                        
                        // Finally delete the Firebase Auth user
                        firestoreManager.getCurrentUser().delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Firebase Auth user deleted successfully");
                                    
                                    // Clear local data and logout
                                    logout();
                                    
                                    if (callback != null) callback.onSuccess("Account and all data deleted successfully");
                                } else {
                                    Log.e(TAG, "Failed to delete Firebase Auth user", task.getException());
                                    if (callback != null) callback.onError("Failed to delete account: " + 
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                                }
                            });
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to delete user data from Firestore", e);
                        if (callback != null) callback.onError("Failed to delete user data: " + e.getMessage());
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to delete community data: " + error);
                if (callback != null) callback.onError("Failed to delete community data: " + error);
            }
        });
    }
    
    public interface ProfileCallback {
        void onSuccess(User user);
        void onError(String error);
    }
}
