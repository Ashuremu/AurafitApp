package com.aurafit.AuraFitApp.ui.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.databinding.ActivityRegisterBinding;
import com.aurafit.AuraFitApp.ui.login.LoggedInUserView;
import com.aurafit.AuraFitApp.data.UserDataManager;
import com.aurafit.AuraFitApp.data.model.User;

public class RegisterActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private ActivityRegisterBinding binding;
    private RegisterViewModel registerViewModel;
    private UserDataManager userDataManager;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerViewModel = new ViewModelProvider(this, new RegisterViewModelFactory())
                .get(RegisterViewModel.class);
        
        userDataManager = UserDataManager.getInstance();

        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("997404871235-tphs342cu8k5ltjd30ctu5ie6rsnmquh.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        final ImageButton backButton = binding.backButton;
        final EditText username = binding.username;
        final EditText email = binding.email;
        final EditText password = binding.password;
        final EditText confirmPassword = binding.confirmPassword;
        final Button registerButton = binding.register;
        final ImageView googleButton = binding.googleButton;
        final ProgressBar loading = binding.loading;
        final CheckBox terms = binding.terms;
        final TextView termsText = binding.termsText;

        
        handleGoogleUserData(username, email, password, confirmPassword);

        registerViewModel.getRegisterFormState().observe(this, new Observer<RegisterFormState>() {
            @Override
            public void onChanged(@Nullable RegisterFormState registerFormState) {
                if (registerFormState == null) {
                    return;
                }
                
                
                Intent intent = getIntent();
                String googleUid = intent.getStringExtra("google_uid");
                boolean isGoogleUser = googleUid != null || password.getVisibility() == View.GONE;
                
                if (isGoogleUser) {
                    
                    boolean isGoogleFormValid = !username.getText().toString().trim().isEmpty() && 
                                             !email.getText().toString().trim().isEmpty() &&
                                             terms.isChecked();
                    registerButton.setEnabled(isGoogleFormValid);
                } else {
                    
                    registerButton.setEnabled(registerFormState.isDataValid() && terms.isChecked());
                }
                
                
                if (registerFormState.getUsernameError() != null) {
                    username.setError(getString(registerFormState.getUsernameError()));
                } else {
                    username.setError(null);
                }
                
                
                if (registerFormState.getEmailError() != null) {
                    email.setError(getString(registerFormState.getEmailError()));
                } else {
                    email.setError(null);
                }
                
                
                if (!isGoogleUser) {
                    if (registerFormState.getPasswordError() != null) {
                        password.setError(getString(registerFormState.getPasswordError()));
                    } else {
                        password.setError(null);
                    }
                    
                    
                    if (registerFormState.getConfirmPasswordError() != null) {
                        confirmPassword.setError(getString(registerFormState.getConfirmPasswordError()));
                    } else {
                        confirmPassword.setError(null);
                    }
                }
            }
        });

        registerViewModel.getRegisterResult().observe(this, new Observer<RegisterResult>() {
            @Override
            public void onChanged(@Nullable RegisterResult registerResult) {
                if (registerResult == null) {
                    return;
                }
                loading.setVisibility(View.GONE);
                if (registerResult.getError() != null) {
                    showRegisterFailed(registerResult.getError());
                }
                if (registerResult.getSuccess() != null) {
                    updateUiWithUser(registerResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                
            }

            @Override
            public void afterTextChanged(Editable s) {
                
                Intent intent = getIntent();
                String googleUid = intent.getStringExtra("google_uid");
                boolean isGoogleUser = googleUid != null || password.getVisibility() == View.GONE;
                
                if (isGoogleUser) {
                    
                    registerViewModel.registerDataChanged(
                            username.getText().toString(),
                            email.getText().toString(),
                            "", 
                            ""  
                    );
                } else {
                    
                    registerViewModel.registerDataChanged(
                            username.getText().toString(),
                            email.getText().toString(),
                            password.getText().toString(),
                            confirmPassword.getText().toString()
                    );
                }
            }
        };

        username.addTextChangedListener(afterTextChangedListener);
        email.addTextChangedListener(afterTextChangedListener);
        password.addTextChangedListener(afterTextChangedListener);
        confirmPassword.addTextChangedListener(afterTextChangedListener);

        terms.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull android.widget.CompoundButton buttonView, boolean isChecked) {
                
                Intent intent = getIntent();
                String googleUid = intent.getStringExtra("google_uid");
                boolean isGoogleUser = googleUid != null || password.getVisibility() == View.GONE;
                
                if (isGoogleUser) {
                    
                    boolean isGoogleFormValid = !username.getText().toString().trim().isEmpty() && 
                                             !email.getText().toString().trim().isEmpty() &&
                                             isChecked;
                    registerButton.setEnabled(isGoogleFormValid);
                } else {
                    
                    registerButton.setEnabled(registerViewModel.getRegisterFormState().getValue() != null && 
                            registerViewModel.getRegisterFormState().getValue().isDataValid() && isChecked);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                
                
                Intent intent = getIntent();
                String googleUid = intent.getStringExtra("google_uid");
                
                
                boolean isGoogleUser = googleUid != null || password.getVisibility() == View.GONE;
                
                if (isGoogleUser) {
                    
                    handleGoogleUserRegistration(username.getText().toString(), email.getText().toString());
                } else {
                    
                    registerViewModel.register(
                            username.getText().toString(),
                            email.getText().toString(),
                            password.getText().toString(),
                            confirmPassword.getText().toString()
                    );
                }
            }
        });

        binding.loginPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        
        setupClickableTermsText(termsText);

        
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (terms.isChecked()) {
                    
                    terms.setChecked(false);
                    showTermsAndConditionsDialog();
                }
            }
        });

        
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void setupClickableTermsText(TextView termsTextView) {
        String fullText = "By creating an account you agree to our Terms and Conditions & Privacy Policy";
        SpannableString spannableString = new SpannableString(fullText);
        
        
        int termsStart = fullText.indexOf("Terms and Conditions");
        int termsEnd = termsStart + "Terms and Conditions".length();
        int privacyStart = fullText.indexOf("Privacy Policy");
        int privacyEnd = privacyStart + "Privacy Policy".length();
        
        
        ClickableSpan termsClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showTermsAndConditionsDialog();
            }
            
            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.BLUE);
            }
        };
        
        
        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showPrivacyPolicyDialog();
            }
            
            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.BLUE);
            }
        };
        
        
        spannableString.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        
        termsTextView.setText(spannableString);
        termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        termsTextView.setHighlightColor(Color.TRANSPARENT);
    }

    private void showTermsAndConditionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.terms_and_conditions_title));
        
        
        ScrollView scrollView = new ScrollView(this);
        TextView contentView = new TextView(this);
        contentView.setText(Html.fromHtml(getString(R.string.terms_and_conditions_content), Html.FROM_HTML_MODE_COMPACT));
        contentView.setPadding(50, 30, 50, 30);
        contentView.setTextSize(14);
        contentView.setLineSpacing(2, 1.2f); 
        
        scrollView.addView(contentView);
        scrollView.setPadding(0, 0, 0, 0);
        
        builder.setView(scrollView);
        
        
        builder.setPositiveButton(getString(R.string.terms_dialog_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
                binding.terms.setChecked(true);
                
                registerViewModel.registerDataChanged(
                    binding.username.getText().toString(),
                    binding.email.getText().toString(),
                    binding.password.getText().toString(),
                    binding.confirmPassword.getText().toString()
                );
            }
        });
        
        builder.setNegativeButton(getString(R.string.terms_dialog_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
                binding.terms.setChecked(false);
            }
        });
        
        AlertDialog dialog = builder.create();
        
        
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (positiveButton != null) {
                    positiveButton.setEnabled(false);
                    positiveButton.setBackgroundColor(android.graphics.Color.parseColor("#D1D5DB"));
                    positiveButton.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
                }
                
                
                scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        
                        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                            @Override
                            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                
                                View child = scrollView.getChildAt(0);
                                if (child != null) {
                                    int childHeight = child.getHeight();
                                    int scrollViewHeight = scrollView.getHeight();
                                    int scrollYPosition = scrollY;
                                    
                                    
                                    if (scrollYPosition + scrollViewHeight >= childHeight - 50) {
                                        if (positiveButton != null) {
                                            positiveButton.setEnabled(true);
                                            positiveButton.setBackgroundColor(android.graphics.Color.parseColor("#8B5CF6"));
                                            positiveButton.setTextColor(android.graphics.Color.WHITE);
                                        }
                                    } else {
                                        if (positiveButton != null) {
                                            positiveButton.setEnabled(false);
                                            positiveButton.setBackgroundColor(android.graphics.Color.parseColor("#D1D5DB"));
                                            positiveButton.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
        
        dialog.show();
    }

    private void showPrivacyPolicyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.privacy_policy_title));
        
        
        ScrollView scrollView = new ScrollView(this);
        TextView contentView = new TextView(this);
        contentView.setText(Html.fromHtml(getString(R.string.privacy_policy_content), Html.FROM_HTML_MODE_COMPACT));
        contentView.setPadding(50, 30, 50, 30);
        contentView.setTextSize(14);
        contentView.setLineSpacing(2, 1.2f); 
        
        scrollView.addView(contentView);
        scrollView.setPadding(0, 0, 0, 0);
        
        builder.setView(scrollView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleGoogleUserData(EditText username, EditText email, EditText password, EditText confirmPassword) {
        Intent intent = getIntent();
        if (intent != null) {
            String googleEmail = intent.getStringExtra("google_email");
            String googleDisplayName = intent.getStringExtra("google_display_name");
            String googleUid = intent.getStringExtra("google_uid");
            
            if (googleEmail != null) {
                
                email.setText(googleEmail);
                email.setEnabled(false); 
                
                
                if (googleDisplayName != null && !googleDisplayName.isEmpty()) {
                    username.setText(googleDisplayName);
                } else {
                    
                    String usernameFromEmail = googleEmail.split("@")[0];
                    username.setText(usernameFromEmail);
                }
                
                
                password.setVisibility(View.GONE);
                confirmPassword.setVisibility(View.GONE);
                
                
                binding.terms.setChecked(true);
                
                
                Toast.makeText(this, "Completing registration with your Google account", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleGoogleUserRegistration(String username, String email) {
        
        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            
            com.aurafit.AuraFitApp.data.model.User user = new com.aurafit.AuraFitApp.data.model.User(
                firebaseUser.getUid(),
                username, 
                email,
                username 
            );
            
            
            com.aurafit.AuraFitApp.data.FirestoreManager.getInstance().saveUserToDatabase(user, new com.aurafit.AuraFitApp.data.FirestoreManager.DatabaseCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        binding.loading.setVisibility(View.GONE);
                        String welcome = getString(R.string.welcome) + username;
                        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
                        
                        
                        android.content.Intent intent = new android.content.Intent(RegisterActivity.this, com.aurafit.AuraFitApp.ui.homepage.HomepageActivity.class);
                        startActivity(intent);
                        
                        
                        new android.os.Handler().postDelayed(() -> finish(), 100);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        binding.loading.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Registration completed! Welcome to AURAfit!", Toast.LENGTH_LONG).show();
                        
                        
                        android.content.Intent intent = new android.content.Intent(RegisterActivity.this, com.aurafit.AuraFitApp.ui.homepage.HomepageActivity.class);
                        startActivity(intent);
                        
                        
                        new android.os.Handler().postDelayed(() -> finish(), 100);
                    });
                }
            });
        } else {
            
            runOnUiThread(() -> {
                binding.loading.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Registration completed! Welcome to AURAfit!", Toast.LENGTH_LONG).show();
                
                
                android.content.Intent intent = new android.content.Intent(RegisterActivity.this, com.aurafit.AuraFitApp.ui.homepage.HomepageActivity.class);
                startActivity(intent);
                
                
                new android.os.Handler().postDelayed(() -> finish(), 100);
            });
        }
    }

    private void signInWithGoogle() {
        
        googleSignInClient.signOut().addOnCompleteListener(this, new com.google.android.gms.tasks.OnCompleteListener<Void>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<Void> task) {
                
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
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
                        
                        handleGoogleSignInForRegistration(account);
                    } else {
                        Toast.makeText(this, "Failed to get Google ID token", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGoogleSignInForRegistration(GoogleSignInAccount account) {
        
        binding.email.setText(account.getEmail());
        binding.email.setEnabled(false); 
        
        if (account.getDisplayName() != null && !account.getDisplayName().isEmpty()) {
            binding.username.setText(account.getDisplayName());
        } else {
            
            String usernameFromEmail = account.getEmail().split("@")[0];
            binding.username.setText(usernameFromEmail);
        }
        
        
        binding.password.setVisibility(View.GONE);
        binding.confirmPassword.setVisibility(View.GONE);
        
        
        binding.terms.setChecked(true);
        
        
        Toast.makeText(this, "Completing registration with your Google account", Toast.LENGTH_LONG).show();
        
        
        Intent intent = getIntent();
        intent.putExtra("google_email", account.getEmail());
        intent.putExtra("google_display_name", account.getDisplayName());
        intent.putExtra("google_uid", account.getId());
    }

    private void updateUiWithUser(LoggedInUserView model) {
        
        userDataManager.getCurrentUserProfile(new UserDataManager.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    String displayName = user.getDisplayName() != null ? user.getDisplayName() : "User";
                    String welcome = getString(R.string.welcome) + displayName;
                    Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
                    
                    
                    android.content.Intent intent = new android.content.Intent(RegisterActivity.this, com.aurafit.AuraFitApp.ui.homepage.HomepageActivity.class);
                    startActivity(intent);
                    
                    
                    
                    new android.os.Handler().postDelayed(() -> finish(), 100);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    
                    String welcome = getString(R.string.welcome) + model.getDisplayName();
                    Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
                    
                    
                    android.content.Intent intent = new android.content.Intent(RegisterActivity.this, com.aurafit.AuraFitApp.ui.homepage.HomepageActivity.class);
                    startActivity(intent);
                    
                    
                    
                    new android.os.Handler().postDelayed(() -> finish(), 100);
                });
            }
        });
    }

    private void showRegisterFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}


