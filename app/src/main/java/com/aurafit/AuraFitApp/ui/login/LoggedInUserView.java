package com.aurafit.AuraFitApp.ui.login;

public class LoggedInUserView {
    private final String displayName;
    //... other data fields that may be accessible to the UI

    public LoggedInUserView(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}