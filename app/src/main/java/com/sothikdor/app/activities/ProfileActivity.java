package com.sothikdor.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sothikdor.app.utils.AuthUtils;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.sothikdor.R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("প্রোফাইল");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView tvName  = findViewById(com.sothikdor.R.id.tv_profile_name);
        TextView tvEmail = findViewById(com.sothikdor.R.id.tv_profile_email);
        LinearLayout btnAdmin     = findViewById(com.sothikdor.R.id.btn_admin);
        LinearLayout btnComplaint = findViewById(com.sothikdor.R.id.btn_complaint);
        LinearLayout btnLogout    = findViewById(com.sothikdor.R.id.btn_logout);

        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();

            tvName.setText(name != null && !name.isEmpty() ? name : "ব্যবহারকারী");
            tvEmail.setText(user.isAnonymous() ? "গেস্ট মোড" : email);

            // শুধু Admin email হলে Admin Panel দেখাবে
            btnAdmin.setVisibility(AuthUtils.isAdmin(user) ? View.VISIBLE : View.GONE);
        } else {
            tvName.setText("ব্যবহারকারী");
            tvEmail.setText("গেস্ট মোড");
            btnAdmin.setVisibility(View.GONE);
        }

        btnAdmin.setOnClickListener(v ->
            startActivity(new Intent(this, AdminActivity.class)));

        btnComplaint.setOnClickListener(v ->
            startActivity(new Intent(this, ComplaintActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
