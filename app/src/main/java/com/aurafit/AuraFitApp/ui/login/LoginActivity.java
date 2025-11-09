package com.aurafit.AuraFitApp.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.UserDataManager;
import com.aurafit.AuraFitApp.data.model.User;
import com.aurafit.AuraFitApp.ui.register.RegisterActivity;
import com.aurafit.AuraFitApp.ui.homepage.HomepageActivity;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private LoginViewModel loginViewModel;
    private UserDataManager userDataManager;
    private GoogleSignInClient googleSignInClient;
    private ProgressBar loadingProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Check if user is already logged in and should be kept logged in
        checkAutoLogin();

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        userDataManager = UserDataManager.getInstance();

        // Configure Google Sign-In with forced account selection
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("997404871235-tphs342cu8k5ltjd30ctu5ie6rsnmquh.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        final EditText emailEditText = findViewById(R.id.email);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);
        final TextView registerPrompt = findViewById(R.id.registerPrompt);
        final ImageView googleButton = findViewById(R.id.googleButton);
        final CheckBox rememberMe = findViewById(R.id.rememberMe);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getemailError() != null) {
                    emailEditText.setError(getString(loginFormState.getemailError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    // Store remember user preference if checkbox is checked
                    if (rememberMe.isChecked()) {
                        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        prefs.edit().putBoolean("remember_user", true).apply();
                    }
                    updateUiWithUser(loginResult.getSuccess());
                }
                if (loginResult.needsRegistration()) {
                    // Redirect to registration with Google user info
                    redirectToRegistrationWithGoogleUser(loginResult.getGoogleUser());
                }
                setResult(Activity.RESULT_OK);
                // finish() is called after successful navigation in updateUiWithUser()
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(emailEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                boolean rememberUser = rememberMe.isChecked();
                loginViewModel.login(emailEditText.getText().toString(),
                        passwordEditText.getText().toString(), rememberUser);
            }
        });

        registerPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show loading state
                loadingProgressBar.setVisibility(View.VISIBLE);
                signInWithGoogle();
            }
        });
    }

    private void signInWithGoogle() {
        // Force account selection by signing out first, then showing account picker
        googleSignInClient.signOut().addOnCompleteListener(this, new com.google.android.gms.tasks.OnCompleteListener<Void>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<Void> task) {
                // Now start the sign-in intent to show account selection
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    private void checkAutoLogin() {
        // Check if user is already authenticated with Firebase
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            // Check if user wants to be remembered
            android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            boolean rememberUser = prefs.getBoolean("remember_user", false);
            
            if (rememberUser) {
                // User is already logged in and wants to be remembered, go to homepage
                Intent intent = new Intent(this, com.aurafit.AuraFitApp.ui.homepage.HomepageActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String idToken = account.getIdToken();
                    if (idToken != null) {
                        // Show loading state and proceed with Google sign-in
                        loginViewModel.signInWithGoogle(idToken);
                    } else {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to get Google ID token", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "No Google account selected", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void redirectToRegistrationWithGoogleUser(com.google.firebase.auth.FirebaseUser googleUser) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        intent.putExtra("google_email", googleUser.getEmail());
        intent.putExtra("google_display_name", googleUser.getDisplayName());
        intent.putExtra("google_uid", googleUser.getUid());
        startActivity(intent);
        finish();
    }

    private void updateUiWithUser(LoggedInUserView model) {
        // Fetch user data from Firestore to get the most up-to-date display name
        userDataManager.getCurrentUserProfile(new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    String displayName = user.getDisplayName() != null ? user.getDisplayName() : "User";
                    String welcome = getString(R.string.welcome) + displayName;
                    Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();

                    // Navigate to HomepageActivity after successful login
                    Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                    startActivity(intent);

                    // Complete and destroy login activity after successful navigation
                    // Small delay to ensure smooth transition
                    new Handler().postDelayed(() -> finish(), 100);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Fallback to the cached display name if Firestore fetch fails
                    String welcome = getString(R.string.welcome) + model.getDisplayName();
                    Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();

                    // Navigate to HomepageActivity after successful login
                    Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                    startActivity(intent);

                    // Complete and destroy login activity after successful navigation
                    // Small delay to ensure smooth transition
                    new Handler().postDelayed(() -> finish(), 100);
                });
            }
        });
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}