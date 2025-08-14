package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns; // Import Patterns for email validation
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class activity_sign_up extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword;
    Button bttSignUp; // Changed name to bttSignUp for clarity
    TextView ClickLogin;

    // Instance of DBHelper to interact with the database
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize DBHelper
        dbHelper = new DBHelper(this);

        // Initialize UI elements
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail); // Make sure this ID is correct in your layout
        edtPassword = findViewById(R.id.edtPassword);
        bttSignUp = findViewById(R.id.bttLogin); // Still using the original ID from layout
        ClickLogin = findViewById(R.id.ClickLogin);

        // Set OnClickListener for the Sign Up button
        bttSignUp.setOnClickListener(v -> {
            registerUser(); // Call method to handle user registration
        });

        // Set OnClickListener for the "Already have an account? Login" text
        ClickLogin.setOnClickListener(v -> {
            // Navigate back to the MainActivity (Login screen)
            Intent intent = new Intent(activity_sign_up.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish current activity to prevent going back to SignUp
        });
    }

    /**
     * Handles the user registration process using DBHelper (SQLite).
     * This method will insert new user data into the 'users' table.
     */
    private void registerUser() {
        String name = edtName.getText().toString().trim(); // Trim whitespace
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Basic input validation
        if (name.isEmpty()) {
            edtName.setError("กรุณากรอกชื่อผู้ใช้");
            edtName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            edtEmail.setError("กรุณากรอกอีเมล");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("กรุณากรอกอีเมลที่ถูกต้อง");
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("กรุณากรอกรหัสผ่าน");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) { // Example: Minimum password length
            edtPassword.setError("รหัสผ่านต้องมีความยาวอย่างน้อย 6 ตัวอักษร");
            edtPassword.requestFocus();
            return;
        }

        // Check if the email already exists in the database using DBHelper
        if (dbHelper.checkEmailExists(email)) {
            Toast.makeText(this, "อีเมลนี้ถูกใช้แล้ว กรุณาใช้อีเมลอื่น", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Assign User Role ---
        // For simplicity, new users will be assigned the "user" role by default.
        // You can add logic here to assign "admin" role based on specific conditions,
        // e.g., if a specific email is used, or via a separate admin interface.
        String role = "user"; // Default role for new registrations

        // Insert user data into the SQLite database using DBHelper, including the role
        boolean isInserted = dbHelper.insertUser(name, email, password, role);

        if (isInserted) {
            Toast.makeText(this, "สมัครสมาชิกเรียบร้อย", Toast.LENGTH_SHORT).show();

            // --- NAVIGATION CHANGE ---
            // After successful registration, navigate back to the MainActivity (Login screen).
            // The user will then need to log in manually with their new account.
            Intent intent = new Intent(activity_sign_up.this, MainActivity.class);
            // No need to pass email or role here as the user will log in on MainActivity.
            startActivity(intent);
            finish(); // Finish current activity so user cannot go back to sign-up
        } else {
            // This might happen if there's a database error (e.g., UNIQUE constraint violation if not checked before)
            // or if the DBHelper.insertUser method returns false for other reasons.
            Toast.makeText(this, "การสมัครสมาชิกล้มเหลว", Toast.LENGTH_SHORT).show();
        }
    }
}
