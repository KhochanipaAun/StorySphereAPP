package com.example.storysphere_appbar;

import android.Manifest; // Import Manifest for permissions
import android.content.Intent;
import android.content.pm.PackageManager; // Import PackageManager
import android.graphics.Bitmap;
import android.graphics.BitmapFactory; // Import BitmapFactory
import android.net.Uri; // Import Uri
import android.os.Bundle;
import android.provider.MediaStore; // Import MediaStore for image selection
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView; // Import ImageView
import android.widget.Toast;

import androidx.annotation.NonNull; // Import NonNull
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat; // Import ActivityCompat
import androidx.core.content.ContextCompat; // Import ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream; // Import InputStream
import java.util.List;

public class activity_writing extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WritingAdapter adapter;
    private DBHelper dbHelper;

    private String currentUserEmail;
    private String currentUserRole;

    private ImageView profileImageView; // Declare ImageView for profile pic

    // Request codes for Intents and Permissions
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            currentUserEmail = intent.getStringExtra("email");
            currentUserRole = intent.getStringExtra("role");
        }

        profileImageView = findViewById(R.id.profileImageView); // Initialize profileImageView

        recyclerView = findViewById(R.id.recyclerViewWriting);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);

        // Load profile picture when the activity is created
        loadProfilePicture();
        loadData();
    }

    /**
     * Loads the current user's profile picture from the database and displays it.
     */
    private void loadProfilePicture() {
        if (currentUserEmail != null) {
            String imageUriString = dbHelper.getUserImageUri(currentUserEmail); // Method to get user's image URI from DB
            if (imageUriString != null && !imageUriString.isEmpty()) {
                try {
                    Uri imageUri = Uri.parse(imageUriString);
                    profileImageView.setImageURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                    profileImageView.setImageResource(R.drawable.default_profile_pic); // Fallback on error
                    Toast.makeText(this, "Failed to load profile picture.", Toast.LENGTH_SHORT).show();
                }
            } else {
                profileImageView.setImageResource(R.drawable.default_profile_pic); // Set default if no image
            }
        }
    }

    /**
     * Called when the profileImageView is clicked (via android:onClick="selectProfileImage" in XML).
     * Initiates the process to pick an image from the gallery after checking permissions.
     * @param view The View that was clicked.
     */
    public void selectProfileImage(View view) {
        // Request storage permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, proceed to pick image
            pickImageFromGallery();
        }
    }

    /**
     * Launches an Intent to pick an image from the device's gallery.
     */
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    /**
     * Handles the result from external activities like image picker.
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Load bitmap from URI
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedBitmap = BitmapFactory.decodeStream(imageStream);

                // Display the selected image immediately
                profileImageView.setImageBitmap(selectedBitmap);

                // Save the image to internal storage and update DB
                String savedImageUriString = ImageStorageHelper.saveImageToInternalStorage(this, selectedBitmap, "profile_pic_" + currentUserEmail.replace("@", "_").replace(".", "_"));

                if (savedImageUriString != null) {
                    // Update the user's image URI in the database
                    // Note: We are only updating image_uri, leaving username and password as null in this specific call
                    dbHelper.updateUser(currentUserEmail, null, null, savedImageUriString);
                    Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save profile picture.", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handles the result of permission requests.
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[], int).
     * @param permissions The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to pick image
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Permission denied to read external storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --- Existing methods (mostly unchanged, except for passing user info to AddNewWriting) ---

    private void loadData() {
        List<WritingItem> list = dbHelper.getAllWritingItems();
        adapter = new WritingAdapter(this, list);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data and profile picture every time the activity resumes
        loadProfilePicture(); // Ensure profile pic is refreshed if updated elsewhere
        loadData();
    }

    public void AddNewWriting (View view){
        Intent intent = new Intent(this, Writing_Add_Episode1.class);
        intent.putExtra("email", currentUserEmail);
        intent.putExtra("role", currentUserRole);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        dbHelper.clearLoginSession();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            dbHelper.clearLoginSession();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
