package com.aurafit.AuraFitApp.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.aurafit.AuraFitApp.data.model.LoggedInUser;
import com.aurafit.AuraFitApp.data.model.User;
import com.aurafit.AuraFitApp.data.FirestoreManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthCredential;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class LoginDataSource {

    private static final String TAG = "LoginDataSource";
    private FirebaseAuth firebaseAuth;
    private FirestoreManager firestoreManager;

    public LoginDataSource() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreManager = FirestoreManager.getInstance();
    }

    public Result<LoggedInUser> login(String email, String password) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Result<LoggedInUser>> result = new AtomicReference<>();

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Update only last login time in Firestore
                                updateLastLoginTime(firebaseUser.getUid());
                                
                                LoggedInUser loggedInUser = new LoggedInUser(
                                    firebaseUser.getUid(),
                                    firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User"
                                );
                                result.set(new Result.Success<>(loggedInUser));
                            } else {
                                result.set(new Result.Error(new Exception("User is null after login")));
                            }
                        } else {
                            Exception exception = task.getException() != null ? task.getException() : 
                                new Exception("Login failed");
                            result.set(new Result.Error(exception));
                        }
                        latch.countDown();
                    }
                });

            latch.await();
            return result.get();
        } catch (InterruptedException e) {
            return new Result.Error(new IOException("Login failed", e));
        }
    }

    public Result<LoggedInUser> register(String username, String email, String password) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Result<LoggedInUser>> result = new AtomicReference<>();
            
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Create user profile in Firestore
                                User user = new User(
                                    firebaseUser.getUid(),
                                    username,
                                    email,
                                    username
                                );
                                
                                // Save to Firestore
                                firestoreManager.saveUserToDatabase(user, new FirestoreManager.DatabaseCallback() {
                                    @Override
                                    public void onSuccess() {
                                        LoggedInUser loggedInUser = new LoggedInUser(
                                            firebaseUser.getUid(),
                                            username
                                        );
                                        result.set(new Result.Success<>(loggedInUser));
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        result.set(new Result.Error(e));
                                        latch.countDown();
                                    }
                                });
                            } else {
                                result.set(new Result.Error(new Exception("User is null after registration")));
                                latch.countDown();
                            }
                        } else {
                            Exception exception = task.getException() != null ? task.getException() : 
                                new Exception("Registration failed");
                            result.set(new Result.Error(exception));
                            latch.countDown();
                        }
                    }
                });

            latch.await();
            return result.get();
        } catch (InterruptedException e) {
            return new Result.Error(new IOException("Registration failed", e));
        }
    }

    private void updateLastLoginTime(String userId) {
        // Update ONLY the lastLoginAt field in Firestore
        // This method does NOT update email, username, displayName, or any other fields
        // It only touches the lastLoginAt timestamp to track when user last logged in
        firestoreManager.updateLastLoginTime(userId, new FirestoreManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Last login time updated successfully");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update last login time", e);
            }
        });
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public Result<LoggedInUser> signInWithGoogle(String idToken) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Result<LoggedInUser>> result = new AtomicReference<>();

            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Check if user exists in Firestore
                                checkUserExistsInFirestore(firebaseUser, result, latch);
                            } else {
                                result.set(new Result.Error(new Exception("User is null after Google sign-in")));
                                latch.countDown();
                            }
                        } else {
                            Exception exception = task.getException() != null ? task.getException() : 
                                new Exception("Google sign-in failed");
                            result.set(new Result.Error(exception));
                            latch.countDown();
                        }
                    }
                });

            latch.await();
            return result.get();
        } catch (InterruptedException e) {
            return new Result.Error(new IOException("Google sign-in failed", e));
        }
    }

    private void checkUserExistsInFirestore(FirebaseUser firebaseUser, 
                                          AtomicReference<Result<LoggedInUser>> result, 
                                          CountDownLatch latch) {
        Log.d(TAG, "Checking if user exists in Firestore for UID: " + firebaseUser.getUid());
        
        firestoreManager.getUserFromDatabase(firebaseUser.getUid(), new FirestoreManager.UserCallback() {
            @Override
            public void onSuccess(User user) {
                // User exists in Firestore, proceed with login
                Log.d(TAG, "User found in Firestore, proceeding with login for: " + user.getEmail());
                updateLastLoginTime(firebaseUser.getUid());
                
                LoggedInUser loggedInUser = new LoggedInUser(
                    firebaseUser.getUid(),
                    firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User"
                );
                result.set(new Result.Success<>(loggedInUser));
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                // User doesn't exist in Firestore, need to register
                Log.d(TAG, "User not found in Firestore, redirecting to registration for: " + firebaseUser.getEmail());
                // Return special result with Google user info for registration
                result.set(new Result.GoogleSignInNeedsRegistration(firebaseUser));
                latch.countDown();
            }
        });
    }
}