package com.aurafit.AuraFitApp.ui.login;

import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseUser;

public class LoginResult {
    @Nullable
    private LoggedInUserView success;
    @Nullable
    private Integer error;
    @Nullable
    private FirebaseUser googleUser;

    public LoginResult(@Nullable Integer error) {
        this.error = error;
    }

    public LoginResult(@Nullable LoggedInUserView success) {
        this.success = success;
    }

    public LoginResult(@Nullable FirebaseUser googleUser, boolean needsRegistration) {
        this.googleUser = googleUser;
    }

    @Nullable
    public LoggedInUserView getSuccess() {
        return success;
    }

    @Nullable
    public Integer getError() {
        return error;
    }

    @Nullable
    public FirebaseUser getGoogleUser() {
        return googleUser;
    }

    public boolean needsRegistration() {
        return googleUser != null;
    }
}