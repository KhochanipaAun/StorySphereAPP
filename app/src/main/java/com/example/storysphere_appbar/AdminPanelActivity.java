package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
// Removed MenuItem import as toolbar navigation is removed
import android.widget.TextView;
import android.widget.Toast;

// Removed NonNull import as it's not directly used without MenuItem
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
// Removed Toolbar import as it's not used for ActionBar setup here


public class AdminPanelActivity extends AppCompatActivity {

    private TextView adminWelcomeText; // This will now refer to textView8 in XML
    private DBHelper dbHelper; // To handle logout or other DB operations

    private String adminEmail;
    private String adminRole;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Correctly link to activity_admin.xml

        // --- Removed Toolbar initialization ---
        // Your activity_admin.xml does not have a Toolbar with ID toolbar_admin
        // that is set as the ActionBar. So, remove this code.
        // If you want a Toolbar, you need to add it to your XML and set it up.
        // Toolbar toolbar = findViewById(R.id.toolbar_admin);
        // setSupportActionBar(toolbar);
        // if (getSupportActionBar() != null) {
        //     getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //     getSupportActionBar().setTitle("Admin Panel");
        // }

        dbHelper = new DBHelper(this); // Initialize DBHelper

        // Correctly initialize adminWelcomeText to reference textView8 from your XML
        adminWelcomeText = findViewById(R.id.textView8);

        // Get admin email and role from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            adminEmail = intent.getStringExtra("email");
            adminRole = intent.getStringExtra("role");

            if (adminEmail != null) {
                // Update textView8 with the admin's email
                adminWelcomeText.setText("Hi Admin: " + adminEmail + "!");
            } else {
                adminWelcomeText.setText("Hi Admin!"); // Default welcome if email not found
            }
            // Optional: Verify role here again, though MainActivity should have handled it
            if (adminRole == null || !adminRole.equals("admin")) {
                Toast.makeText(this, "Access Denied: Not an admin.", Toast.LENGTH_LONG).show();
                // Redirect to login or previous activity if somehow non-admin accesses this
                Intent backToMain = new Intent(AdminPanelActivity.this, MainActivity.class);
                startActivity(backToMain);
                finish();
            }
        } else {
            // If no intent data, something went wrong, redirect
            Toast.makeText(this, "Admin access failed. Please log in again.", Toast.LENGTH_LONG).show();
            Intent backToMain = new Intent(AdminPanelActivity.this, MainActivity.class);
            startActivity(backToMain);
            finish();
        }

        // You can add more admin-specific UI elements and logic here
        // e.g., Buttons for "Manage Users", "View All Writings", "App Settings"
        // And set up their onClick listeners
    }

    // --- Removed onOptionsItemSelected ---
    // This method handles clicks on items in the ActionBar/Toolbar.
    // Since your activity_admin.xml does not have a Toolbar set as the ActionBar,
    // this method is not relevant here and would not be called for android.R.id.home.
    // If you want a back button, you'll need to add a custom ImageView/Button in your XML
    // and handle its click listener.
    // @Override
    // public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    //     if (item.getItemId() == android.R.id.home) {
    //         dbHelper.clearLoginSession();
    //         Intent intent = new Intent(AdminPanelActivity.this, MainActivity.class);
    //         startActivity(intent);
    //         finish();
    //         Toast.makeText(this, "Admin logged out.", Toast.LENGTH_SHORT).show();
    //         return true;
    //     }
    //     return super.onOptionsItemSelected(item);
    // }

    // Override onBackPressed to handle the hardware back button
    @Override
    public void onBackPressed() {
        // Example: Log out on hardware back press
        super.onBackPressed();
        dbHelper.clearLoginSession();
        Intent intent = new Intent(AdminPanelActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Admin logged out.", Toast.LENGTH_SHORT).show();
        // Do NOT call super.onBackPressed() after finish() if you want to prevent going back to previous activity.
        // super.onBackPressed(); // Removed this as finish() already handles navigation.
    }
}
