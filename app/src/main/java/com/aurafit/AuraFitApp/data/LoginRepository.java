package com.aurafit.AuraFitApp.data;

import com.aurafit.AuraFitApp.data.model.LoggedInUser;
import com.aurafit.AuraFitApp.data.model.User;

public class LoginRepository {

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if(instance == null){
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public Result<LoggedInUser> login(String email, String password) {
        // handle login
        Result<LoggedInUser> result = dataSource.login(email, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }

    public Result<LoggedInUser> register(String username, String email, String password) {
        // handle registration
        Result<LoggedInUser> result = dataSource.register(username, email, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }

    public Result<LoggedInUser> signInWithGoogle(String idToken) {
        // handle Google sign-in
        Result<LoggedInUser> result = dataSource.signInWithGoogle(idToken);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }

    public void getCurrentUserData(FirestoreManager.UserCallback callback) {
        FirestoreManager firestoreManager = FirestoreManager.getInstance();
        firestoreManager.getCurrentUserFromDatabase(callback);
    }

    public void getUserProfile(FirestoreManager.UserCallback callback) {
        FirestoreManager firestoreManager = FirestoreManager.getInstance();
        firestoreManager.getUserProfileWithAuthFallback(callback);
    }

    public void updateCurrentUserData(User updatedUser, FirestoreManager.DatabaseCallback callback) {
        FirestoreManager firestoreManager = FirestoreManager.getInstance();
        firestoreManager.fetchAndUpdateCurrentUser(updatedUser, callback);
    }

    public void updateUserData(String userId, User updatedUser, FirestoreManager.DatabaseCallback callback) {
        FirestoreManager firestoreManager = FirestoreManager.getInstance();
        firestoreManager.fetchAndUpdateUser(userId, updatedUser, callback);
    }

    public LoggedInUser getCurrentLoggedInUser() {
        return user;
    }
}