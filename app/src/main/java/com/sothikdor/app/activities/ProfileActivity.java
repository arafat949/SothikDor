package com.sothikdor.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        TextView tvName = findViewById(com.sothikdor.R.id.tv_profile_name);
        TextView tvEmail = findViewById(com.sothikdor.R.id.tv_profile_email);
        Button btnLogout = findViewById(com.sothikdor.R.id.btn_logout);

        if (user != null) {
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "ব্যবহারকারী");
            tvEmail.setText(user.isAnonymous() ? "গেস্ট মোড" : user.getEmail());
        }

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
