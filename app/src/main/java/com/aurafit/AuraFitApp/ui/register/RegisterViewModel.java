package com.aurafit.AuraFitApp.ui.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Patterns;

import com.aurafit.AuraFitApp.data.LoginRepository;
import com.aurafit.AuraFitApp.data.Result;
import com.aurafit.AuraFitApp.data.model.LoggedInUser;
import com.aurafit.AuraFitApp.ui.login.LoggedInUserView;
import com.aurafit.AuraFitApp.R;

public class RegisterViewModel extends ViewModel {

    private MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private MutableLiveData<RegisterResult> registerResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    public RegisterViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    public LiveData<RegisterResult> getRegisterResult() {
        return registerResult;
    }

    public void register(String username, String email, String password, String confirmPassword) {
        // Validate all fields before attempting registration
        Integer usernameError = getUsernameError(username);
        Integer emailError = getEmailError(email);
        Integer passwordError = getPasswordError(password);
        Integer confirmPasswordError = getConfirmPasswordError(password, confirmPassword);
        
        if (usernameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            // Update form state to show all validation errors
            registerFormState.setValue(new RegisterFormState(usernameError, emailError, passwordError, confirmPasswordError));
            return;
        }
        
        // Launch in a separate asynchronous job to avoid blocking UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Result<LoggedInUser> result = loginRepository.register(username, email, password);

                // Post result back to main thread
                if (result instanceof Result.Success) {
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    registerResult.postValue(new RegisterResult(new LoggedInUserView(data.getDisplayName())));
                } else {
                    registerResult.postValue(new RegisterResult(R.string.registration_failed));
                }
            }
        }).start();
    }

    public void registerDataChanged(String username, String email, String password, String confirmPassword) {
        Integer usernameError = getUsernameError(username);
        Integer emailError = getEmailError(email);
        Integer passwordError = getPasswordError(password);
        Integer confirmPasswordError = getConfirmPasswordError(password, confirmPassword);
        
        if (usernameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            registerFormState.setValue(new RegisterFormState(usernameError, emailError, passwordError, confirmPasswordError));
        } else {
            registerFormState.setValue(new RegisterFormState(true));
        }
    }

    // Comprehensive username validation
    private Integer getUsernameError(String username) {
        if (username == null || username.trim().isEmpty()) {
            return R.string.invalid_username;
        }
        
        String trimmedUsername = username.trim();
        
        // Check length
        if (trimmedUsername.length() < 3 || trimmedUsername.length() > 20) {
            return R.string.invalid_username_length;
        }
        
        // Check format (only letters, numbers, and underscores)
        if (!trimmedUsername.matches("^[a-zA-Z0-9_]+$")) {
            return R.string.invalid_username_format;
        }
        
        return null; // No error
    }

    // Comprehensive email validation
    private Integer getEmailError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return R.string.invalid_email_empty;
        }
        
        String trimmedEmail = email.trim();
        
        // Use Android's built-in email pattern as base validation
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return R.string.invalid_email_format;
        }
        
        // Additional comprehensive email validation
        if (!isValidEmailFormat(trimmedEmail)) {
            return R.string.invalid_email_format;
        }
        
        return null; // No error
    }
    
    // Enhanced email format validation
    private boolean isValidEmailFormat(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        // Check for basic email structure
        if (!email.contains("@") || !email.contains(".")) {
            return false;
        }
        
        // Split email into local and domain parts
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        String localPart = parts[0];
        String domainPart = parts[1];
        
        // Validate local part (before @)
        if (localPart.isEmpty() || localPart.length() > 64) {
            return false;
        }
        
        // Local part cannot start or end with dot
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return false;
        }
        
        // Local part cannot have consecutive dots
        if (localPart.contains("..")) {
            return false;
        }
        
        // Validate domain part (after @)
        if (domainPart.isEmpty() || domainPart.length() > 253) {
            return false;
        }
        
        // Domain must have at least one dot
        if (!domainPart.contains(".")) {
            return false;
        }
        
        // Domain cannot start or end with dot or hyphen
        if (domainPart.startsWith(".") || domainPart.endsWith(".") ||
            domainPart.startsWith("-") || domainPart.endsWith("-")) {
            return false;
        }
        
        // Domain cannot have consecutive dots
        if (domainPart.contains("..")) {
            return false;
        }
        
        // Check for valid characters in local part (letters, numbers, dots, hyphens, underscores, plus)
        if (!localPart.matches("^[a-zA-Z0-9._+-]+$")) {
            return false;
        }
        
        // Check for valid characters in domain part (letters, numbers, dots, hyphens)
        if (!domainPart.matches("^[a-zA-Z0-9.-]+$")) {
            return false;
        }
        
        // Domain must have a valid TLD (at least 2 characters after the last dot)
        String[] domainParts = domainPart.split("\\.");
        if (domainParts.length < 2) {
            return false;
        }
        
        String tld = domainParts[domainParts.length - 1];
        if (tld.length() < 2 || !tld.matches("^[a-zA-Z]+$")) {
            return false;
        }
        
        return true;
    }

    // Comprehensive password validation
    private Integer getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return R.string.invalid_password_empty;
        }
        
        // Check minimum length
        if (password.length() < 8) {
            return R.string.invalid_password_length;
        }
        
        // Check password strength (at least one uppercase, one lowercase, one number)
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        
        if (!hasUppercase || !hasLowercase || !hasNumber) {
            return R.string.invalid_password_weak;
        }
        
        return null; // No error
    }

    // Confirm password validation
    private Integer getConfirmPasswordError(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return R.string.confirm_password_empty;
        }
        
        if (!confirmPassword.equals(password)) {
            return R.string.password_mismatch;
        }
        
        return null; // No error
    }
    
    // Password strength calculator (for future use)
    public int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0; // No password
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character variety checks
        if (password.matches(".*[a-z].*")) score++; // lowercase
        if (password.matches(".*[A-Z].*")) score++; // uppercase
        if (password.matches(".*\\d.*")) score++; // numbers
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++; // special chars
        
        // Return strength level: 0-2 = weak, 3-4 = medium, 5-6 = strong
        if (score <= 2) return 1; // weak
        else if (score <= 4) return 2; // medium
        else return 3; // strong
    }
}
