package com.aurafit.AuraFitApp.ui.community;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.CommunityDataManager;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CreatePostActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_IMAGE = 1001;
    
    private CommunityDataManager communityDataManager;
    private ImageView postImagePreview;
    private EditText captionEditText;
    private Button selectImageButton;
    private Button createPostButton;
    
    private byte[] selectedImageData;
    private String selectedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        
        communityDataManager = CommunityDataManager.getInstance();
        
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        postImagePreview = findViewById(R.id.postImagePreview);
        captionEditText = findViewById(R.id.captionEditText);
        selectImageButton = findViewById(R.id.selectImageButton);
        createPostButton = findViewById(R.id.createPostButton);
    }
    
    private void setupClickListeners() {
        selectImageButton.setOnClickListener(v -> selectImage());
        
        createPostButton.setOnClickListener(v -> createPost());
        
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
    
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    postImagePreview.setImageBitmap(bitmap);
                    
                    // Convert to byte array for upload
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                    selectedImageData = baos.toByteArray();
                    
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void createPost() {
        String caption = captionEditText.getText().toString().trim();
        
        if (selectedImageData == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (caption.isEmpty()) {
            Toast.makeText(this, "Please enter a caption", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating post...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Upload image first
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        communityDataManager.uploadPostImage(selectedImageData, userId, new CommunityDataManager.ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                selectedImageUrl = imageUrl;
                
                // Create post with uploaded image
                communityDataManager.createPost(caption, imageUrl, new CommunityDataManager.UpdateCallback() {
                    @Override
                    public void onSuccess(String message) {
                        progressDialog.dismiss();
                        Toast.makeText(CreatePostActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        progressDialog.dismiss();
                        Toast.makeText(CreatePostActivity.this, "Failed to create post: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(CreatePostActivity.this, "Failed to upload image: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
