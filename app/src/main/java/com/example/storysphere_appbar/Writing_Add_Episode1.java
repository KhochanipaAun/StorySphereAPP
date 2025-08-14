package com.example.storysphere_appbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap; // Import Bitmap
import android.graphics.BitmapFactory; // Import BitmapFactory
import android.net.Uri;
import android.os.Build; // Import Build for version check
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.InputStream; // Import InputStream
import java.util.Arrays;
import java.util.List;

public class Writing_Add_Episode1 extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int REQUEST_CODE_PERMISSION = 101;

    private EditText edtTitle, edtTagline, edtTag, edtAddContent; // Added edtAddContent
    private Spinner spinnerCategory;
    private Button bttCreate;
    private ImageView imageViewUpload;
    private TextView uploadImageText;
    private DBHelper dbHelper;
    private String savedImagePath = ""; // Stores the path after saving to internal storage

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing_add_episode1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(Writing_Add_Episode1.this, activity_writing.class);
            startActivity(intent);
            finish();
        });

        // เชื่อม UI
        edtTitle = findViewById(R.id.edtAddTitle);
        edtTagline = findViewById(R.id.edtAddTagline);
        edtTag = findViewById(R.id.edtAddTag);
        edtAddContent = findViewById(R.id.edtAddContent); // Initialize new EditText for content
        spinnerCategory = findViewById(R.id.spinner_custom);
        bttCreate = findViewById(R.id.bttCreate);
        imageViewUpload = findViewById(R.id.imageView4);
        uploadImageText = findViewById(R.id.UplodeImage);

        dbHelper = new DBHelper(this);

        // ตั้งค่าหมวดหมู่ใน Spinner
        List<String> categories = Arrays.asList("Fantasy", "Romance", "Action", "Drama", "Fiction", "Non-fiction", "Science Fiction", "Mystery", "Horror", "Biography", "Poetry", "Other"); // Ensure this matches your categories_array in strings.xml
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // คลิก ImageView หรือ TextView เพื่อเลือกรูป
        imageViewUpload.setOnClickListener(v -> checkPermissionAndOpenGallery());
        uploadImageText.setOnClickListener(v -> checkPermissionAndOpenGallery());

        // เมื่อกดปุ่ม Create
        bttCreate.setOnClickListener(v -> createWriting()); // Refactor to a method
    }

    private void checkPermissionAndOpenGallery() {
        // Determine which permission to request based on Android version
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else { // API 32 and lower
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE_PERMISSION);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedBitmap = BitmapFactory.decodeStream(imageStream);

                // Display the selected image immediately
                imageViewUpload.setImageBitmap(selectedBitmap);

                // Save the image to internal storage and update savedImagePath
                // Use a unique name for each writing cover image
                savedImagePath = ImageStorageHelper.saveImageToInternalStorage(this, selectedBitmap, "writing_cover_" + System.currentTimeMillis());

                if (savedImagePath != null) {
                    Toast.makeText(this, "รูปภาพถูกเลือกและบันทึกภายในเครื่องแล้ว", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "ไม่สามารถบันทึกรูปภาพในเครื่องได้", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "เกิดข้อผิดพลาดในการโหลดรูปภาพ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                savedImagePath = ""; // Clear path if error occurs
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call super first
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery(); // Permission granted, retry opening gallery
            } else {
                Toast.makeText(this, "ต้องอนุญาตสิทธิ์เพื่อเลือกภาพ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createWriting() {
        String title = edtTitle.getText().toString().trim();
        String tagline = edtTagline.getText().toString().trim();
        String tag = edtTag.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String content = edtAddContent.getText().toString().trim(); // Get content from new EditText

        if (title.isEmpty()) {
            edtTitle.setError("กรุณากรอกหัวข้อ");
            edtTitle.requestFocus();
            return;
        }
        if (tagline.isEmpty()) {
            edtTagline.setError("กรุณากรอก Tagline");
            edtTagline.requestFocus();
            return;
        }
        if (tag.isEmpty()) {
            edtTag.setError("กรุณากรอก Tag");
            edtTag.requestFocus();
            return;
        }
        if (content.isEmpty()) { // Validate content field
            edtAddContent.setError("กรุณากรอกเนื้อหา");
            edtAddContent.requestFocus();
            return;
        }

        if (savedImagePath.isEmpty()) { // Use savedImagePath, not initial imagePath
            Toast.makeText(this, "กรุณาเลือกรูปภาพสำหรับงานเขียน", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert writing data into the database
        long id = dbHelper.insertWriting(title, tagline, tag, category, savedImagePath, content);

        if (id > 0) {
            Toast.makeText(this, "สร้างงานเขียนใหม่สำเร็จ", Toast.LENGTH_SHORT).show();

            // Reset fields (optional)
            edtTitle.setText("");
            edtTagline.setText("");
            edtTag.setText("");
            edtAddContent.setText(""); // Clear content field
            imageViewUpload.setImageResource(R.drawable.ic_placeholder_image); // Set back to placeholder
            savedImagePath = ""; // Reset path

            // กลับหน้า activity_writing
            Intent intent = new Intent(this, activity_writing.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "เกิดข้อผิดพลาดในการบันทึกข้อมูล", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle back button in toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
