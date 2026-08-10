package com.sothikdor.app.activities;

import com.sothikdor.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sothikdor.app.models.User;
import com.sothikdor.app.utils.FirebaseHelper;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword, etName;
    private Button btnLogin, btnRegister;
    private TextView tvToggleMode;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        progressBar = findViewById(R.id.progressBar);

        // Guest mode - Demo
        Button btnGuest = findViewById(R.id.btnGuest);
        btnGuest.setOnClickListener(v -> signInAnonymously());

        btnLogin.setOnClickListener(v -> {
            if (isLoginMode) {
                loginUser();
            } else {
                registerUser();
            }
        });

        tvToggleMode.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateUI();
        });
    }

    private void updateUI() {
        if (isLoginMode) {
            btnLogin.setText("লগইন করুন");
            findViewById(R.id.tilName).setVisibility(View.GONE);
            etName.setVisibility(View.GONE);
            tvToggleMode.setText("নতুন অ্যাকাউন্ট তৈরি করুন");
        } else {
            btnLogin.setText("রেজিস্ট্রেশন করুন");
            findViewById(R.id.tilName).setVisibility(View.VISIBLE);
            etName.setVisibility(View.VISIBLE);
            tvToggleMode.setText("আগে থেকে অ্যাকাউন্ট আছে? লগইন করুন");
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("ইমেইল দিন");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("পাসওয়ার্ড দিন");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        goToMainActivity();
                    } else {
                        Log.w(TAG, "Email login failed", task.getException());
                        Toast.makeText(this, "লগইন ব্যর্থ হয়েছে। ইমেইল বা পাসওয়ার্ড ভুল।",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setVisibility(View.VISIBLE); etName.setError("নাম দিন"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("ইমেইল দিন"); return; }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            Log.e(TAG, "Registration reported success but current user is null");
                            Toast.makeText(this, "❌ রেজিস্ট্রেশন ব্যর্থ: আবার চেষ্টা করুন",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        User user = new User(firebaseUser.getUid(), name, "");
                        FirebaseHelper.getInstance().saveUser(user, new FirebaseHelper.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(LoginActivity.this,
                                    "✅ Account Create Success! স্বাগতম " + name,
                                    Toast.LENGTH_LONG).show();
                                new android.os.Handler().postDelayed(() -> goToMainActivity(), 1500);
                            }
                            @Override
                            public void onError(String error) {
                                // অ্যাকাউন্ট তৈরি হয়েছে, কিন্তু প্রোফাইল সেভ হয়নি
                                Log.e(TAG, "Account created but profile save failed: " + error);
                                Toast.makeText(LoginActivity.this,
                                    "অ্যাকাউন্ট তৈরি হয়েছে, তবে প্রোফাইল সেভ হয়নি: " + error,
                                    Toast.LENGTH_LONG).show();
                                new android.os.Handler().postDelayed(() -> goToMainActivity(), 1500);
                            }
                        });
                    } else {
                        Log.w(TAG, "Registration failed", task.getException());
                        String errorMsg = task.getException() != null && task.getException().getMessage() != null
                                ? task.getException().getMessage()
                                : "অজানা সমস্যা";
                        if (errorMsg.contains("email address is already in use")) {
                            etEmail.setError("এই ইমেইল দিয়ে আগেই একাউন্ট আছে");
                            Toast.makeText(this, "❌ এই ইমেইল আগেই ব্যবহার হয়েছে", Toast.LENGTH_LONG).show();
                        } else if (errorMsg.contains("badly formatted")) {
                            etEmail.setError("সঠিক ইমেইল দিন");
                        } else if (errorMsg.contains("weak password")) {
                            etPassword.setError("পাসওয়ার্ড আরও শক্তিশালী করুন");
                        } else {
                            Toast.makeText(this, "❌ রেজিস্ট্রেশন ব্যর্থ: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void signInAnonymously() {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        goToMainActivity();
                    } else {
                        Log.w(TAG, "Anonymous sign-in failed", task.getException());
                        Toast.makeText(this, "গেস্ট লগইন ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
