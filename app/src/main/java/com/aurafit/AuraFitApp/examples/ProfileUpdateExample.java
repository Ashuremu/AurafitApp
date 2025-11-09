package com.aurafit.AuraFitApp.examples;

import android.util.Log;
import com.aurafit.AuraFitApp.data.UserDataManager;
import com.aurafit.AuraFitApp.data.model.User;

public class ProfileUpdateExample {
    
    private static final String TAG = "ProfileUpdateExample";
    private UserDataManager userDataManager;
    
    public ProfileUpdateExample() {
        userDataManager = UserDataManager.getInstance();
    }
    
    public void updateUserProfile(String newUsername, String newDisplayName) {
        userDataManager.updateUserProfile(newUsername, newDisplayName, 
            new UserDataManager.UpdateCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "Profile update success: " + message);
                    // Update UI or show success message
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Profile update error: " + error);
                    // Show error message to user
                }
            });
    }
    
    public void loadUserProfile() {
        userDataManager.getCurrentUserProfile(new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Profile loaded: " + user.getUsername());
                // Update UI with user data
                displayUserProfile(user);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load profile: " + error);
                // Show error or load default profile
            }
        });
    }
    
    public void updateEmail(String newEmail) {
        userDataManager.updateUserEmail(newEmail, new UserDataManager.UpdateCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "Email updated: " + message);
                // Refresh profile or show success
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Email update failed: " + error);
                // Show error message
            }
        });
    }
    
    public void updateCompleteProfile(String username, String email, String displayName) {
        // First fetch current profile to validate changes
        userDataManager.getCurrentUserProfile(new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User currentUser) {
                // Create updated user object
                User updatedUser = new User();
                
                // Only update fields that have changed
                if (!username.equals(currentUser.getUsername())) {
                    updatedUser.setUsername(username);
                }
                if (!email.equals(currentUser.getEmail())) {
                    updatedUser.setEmail(email);
                }
                if (!displayName.equals(currentUser.getDisplayName())) {
                    updatedUser.setDisplayName(displayName);
                }
                
                // Perform the update
                userDataManager.updateUserProfile(username, displayName, 
                    new UserDataManager.UpdateCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Log.d(TAG, "Complete profile updated: " + message);
                        }
                        
                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Complete profile update failed: " + error);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to fetch current profile for comparison: " + error);
            }
        });
    }
    
    private void displayUserProfile(User user) {
        Log.d(TAG, "Displaying profile:");
        Log.d(TAG, "Username: " + user.getUsername());
        Log.d(TAG, "Email: " + user.getEmail());
        Log.d(TAG, "Display Name: " + user.getDisplayName());
        Log.d(TAG, "Created At: " + user.getCreatedAt());
        Log.d(TAG, "Last Login: " + user.getLastLoginAt());
        
        // In a real Activity/Fragment, you would update UI elements here:
        // usernameTextView.setText(user.getUsername());
        // emailTextView.setText(user.getEmail());
        // displayNameTextView.setText(user.getDisplayName());
    }
    
    public void batchUpdateUserData(String username, String email, String displayName) {
        // Create user object with all updates
        User updatedUser = new User();
        updatedUser.setUsername(username);
        updatedUser.setEmail(email);
        updatedUser.setDisplayName(displayName);
        
        // Use the direct repository method for batch updates
        userDataManager.updateUserByUid(null, updatedUser, // null UID means current user
            new UserDataManager.UpdateCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "Batch update successful: " + message);
                    // Reload profile to show changes
                    loadUserProfile();
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Batch update failed: " + error);
                }
            });
    }
}
