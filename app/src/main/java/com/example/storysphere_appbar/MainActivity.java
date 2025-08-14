package com.example.storysphere_appbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log; // Import Log for debugging
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button bttLogin;
    private TextView ClickSignUp, Forgotpass, txtShowPassword;

    private DBHelper dbHelper;
    private boolean isPasswordVisible = false;

    // --- HARDCODED ADMIN CREDENTIALS (FOR TESTING ONLY - NOT FOR PRODUCTION) ---
    // These credentials will grant admin access directly, bypassing database check for this specific case.
    private static final String HARDCODED_ADMIN_EMAIL = "storysphere63@gmail.com";
    private static final String HARDCODED_ADMIN_PASSWORD = "Storysphere987";
    // -------------------------------------------------------------------------

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        // Check if user is already logged in when the app starts
        String loggedInEmail = dbHelper.getLoggedInUserEmail();
        if (loggedInEmail != null && !loggedInEmail.isEmpty()) {
            String userRole = dbHelper.getUserRole(loggedInEmail); // Get user role from DB
            Log.d("MainActivity", "User already logged in: " + loggedInEmail + " with role: " + userRole);
            navigateUserBasedOnRole(loggedInEmail, userRole); // Navigate based on role
            return; // Stop further execution of onCreate
        }

        // Initialize UI elements if no user is logged in
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        bttLogin = findViewById(R.id.bttLogin);
        ClickSignUp = findViewById(R.id.ClickSignUp);
        Forgotpass = findViewById(R.id.Forgotpass);
        txtShowPassword = findViewById(R.id.txtShowPass);

        // Set OnClickListeners for buttons and text views
        bttLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(); // Call the login logic
            }
        });

        ClickSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToSignUp(); // Navigate to Sign Up activity
            }
        });

        Forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToForgotPassword(); // Navigate to Forgot Password activity
            }
        });

        txtShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePasswordVisibility(); // Toggle password visibility
            }
        });
    }

    /**
     * Handles the user login process.
     * Validates input fields and authenticates user credentials.
     * Includes a hardcoded admin bypass for testing.
     */
    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Input validation
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

        boolean isAuthenticated = false;
        String userRole = null;

        // --- Check for Hardcoded Admin Bypass ---
        // If the entered email and password match the hardcoded admin credentials,
        // force authentication as admin, bypassing the database check for this specific case.
        if (email.equals(HARDCODED_ADMIN_EMAIL) && password.equals(HARDCODED_ADMIN_PASSWORD)) {
            isAuthenticated = true;
            userRole = "admin"; // Force role to "admin" for this specific login
            Log.d("MainActivity", "Hardcoded Admin Login Bypass: " + email + " - Role forced to 'admin'");
        } else {
            // Normal database authentication for all other users
            isAuthenticated = dbHelper.checkUserCredentials(email, password);
            if (isAuthenticated) {
                userRole = dbHelper.getUserRole(email); // Get role from DB for regular users
                Log.d("MainActivity", "Regular Login: " + email + " - Role from DB: " + (userRole != null ? userRole : "null"));
            } else {
                Log.d("MainActivity", "Login failed for: " + email + " - Credentials not found in DB.");
            }
        }

        if (isAuthenticated) {
            Toast.makeText(MainActivity.this, "เข้าสู่ระบบสำเร็จ!", Toast.LENGTH_SHORT).show();
            dbHelper.saveLoginSession(email); // Save login state

            navigateUserBasedOnRole(email, userRole); // Navigate based on determined role
        } else {
            Toast.makeText(MainActivity.this, "อีเมลหรือรหัสผ่านไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigates the user to the appropriate activity based on their role.
     * @param email The email of the logged-in user.
     * @param role The role of the logged-in user.
     */
    private void navigateUserBasedOnRole(String email, String role) {
        Intent intent;
        Log.d("MainActivity", "Navigating user: " + email + " with determined role: " + (role != null ? role : "null"));
        if (role != null && role.equals("admin")) {
            Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
            intent = new Intent(MainActivity.this, AdminPanelActivity.class); // Assumes AdminPanelActivity exists
        } else {
            intent = new Intent(MainActivity.this, activity_writing.class);
        }
        intent.putExtra("email", email); // Pass email to the next activity
        intent.putExtra("role", role);   // Pass role to the next activity
        startActivity(intent);
        finish(); // Finish MainActivity so user can't go back to login screen
    }

    /**
     * Navigates to the activity_sign_up.class.
     */
    private void navigateToSignUp() {
        Intent intent = new Intent(MainActivity.this, activity_sign_up.class);
        startActivity(intent);
    }

    /**
     * Navigates to the activity_forgot_pass.class.
     * Assumes this class exists in the project.
     */
    private void navigateToForgotPassword() {
        Intent intent = new Intent(MainActivity.this, activity_forgot_pass.class);
        startActivity(intent);
    }

    /**
     * Toggles the visibility of the password in the EditText field.
     */
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            txtShowPassword.setText(R.string.Show);
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            txtShowPassword.setText(R.string.Hide);
        }
        edtPassword.setSelection(edtPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }
}
