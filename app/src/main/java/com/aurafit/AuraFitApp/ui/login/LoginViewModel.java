package com.aurafit.AuraFitApp.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Patterns;

import com.aurafit.AuraFitApp.data.LoginRepository;
import com.aurafit.AuraFitApp.data.Result;
import com.aurafit.AuraFitApp.data.model.LoggedInUser;
import com.aurafit.AuraFitApp.R;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    public LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        login(email, password, true); // Default to remember user
    }

    public void login(String email, String password, boolean rememberUser) {
        // Launch in a separate asynchronous job to avoid blocking UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Result<LoggedInUser> result = loginRepository.login(email, password);

                // Post result back to main thread
                if (result instanceof Result.Success) {
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    // Store remember user preference
                    if (rememberUser) {
                        // This will be handled in the Activity after successful login
                    }
                    loginResult.postValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
                } else {
                    loginResult.postValue(new LoginResult(R.string.login_failed));
                }
            }
        }).start();
    }

    public void signInWithGoogle(String idToken) {
        // Launch in a separate asynchronous job to avoid blocking UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Result<LoggedInUser> result = loginRepository.signInWithGoogle(idToken);

                // Post result back to main thread
                if (result instanceof Result.Success) {
                    // User exists in database - proceed with login
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    loginResult.postValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
                } else if (result instanceof Result.GoogleSignInNeedsRegistration) {
                    // User doesn't exist in database - redirect to registration
                    com.google.firebase.auth.FirebaseUser googleUser = 
                        ((Result.GoogleSignInNeedsRegistration) result).getFirebaseUser();
                    loginResult.postValue(new LoginResult(googleUser, true));
                } else {
                    // Error occurred during Google sign-in
                    loginResult.postValue(new LoginResult(R.string.login_failed));
                }
            }
        }).start();
    }

    public void loginDataChanged(String email, String password) {
        if (!isemailValid(email)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_email, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder email validation check
    private boolean isemailValid(String email) {
        if (email == null) {
            return false;
        }
        if (email.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        } else {
            return !email.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}