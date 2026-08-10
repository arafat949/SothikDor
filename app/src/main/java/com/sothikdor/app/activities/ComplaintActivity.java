package com.sothikdor.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sothikdor.R;
import com.sothikdor.app.utils.AuthUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ComplaintActivity extends AppCompatActivity {

    private static final int MAX_SHORT_FIELD = 100;
    private static final int MAX_PHONE_LENGTH = 20;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private EditText etName, etPhone, etMarket, etProduct, etDescription;
    private Spinner spinnerType;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private DatabaseReference complaintsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthUtils.isSignedIn()) {
            Toast.makeText(this, "অভিযোগ জমা দিতে লগইন করুন", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_complaint);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("অভিযোগ / Complaint Box");

        complaintsRef = FirebaseDatabase.getInstance(
            "https://sothik-dor-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("complaints");

        etName        = findViewById(R.id.etComplaintName);
        etPhone       = findViewById(R.id.etComplaintPhone);
        etMarket      = findViewById(R.id.etComplaintMarket);
        etProduct     = findViewById(R.id.etComplaintProduct);
        etDescription = findViewById(R.id.etComplaintDescription);
        spinnerType   = findViewById(R.id.spinnerComplaintType);
        btnSubmit     = findViewById(R.id.btnSubmitComplaint);
        progressBar   = findViewById(R.id.progressBarComplaint);

        btnSubmit.setOnClickListener(v -> submitComplaint());
    }

    private void submitComplaint() {
        String name    = etName.getText().toString().trim();
        String phone   = etPhone.getText().toString().trim();
        String market  = etMarket.getText().toString().trim();
        String product = etProduct.getText().toString().trim();
        String desc    = etDescription.getText().toString().trim();
        String type    = spinnerType.getSelectedItem().toString();

        if (name.isEmpty() || desc.isEmpty() || market.isEmpty()) {
            Toast.makeText(this, "নাম, বাজার এবং বিবরণ দিন", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.length() > MAX_SHORT_FIELD || market.length() > MAX_SHORT_FIELD
                || product.length() > MAX_SHORT_FIELD || phone.length() > MAX_PHONE_LENGTH
                || desc.length() > MAX_DESCRIPTION_LENGTH) {
            Toast.makeText(this, "ইনপুট অনুমতিত সীমার চেয়ে বড়", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.isEmpty() && !phone.matches("[0-9+\\-\\s]{6,20}")) {
            Toast.makeText(this, "সঠিক ফোন নম্বর দিন", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        String id   = complaintsRef.push().getKey();
        if (id == null) {
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            Toast.makeText(this, "❗ অভিযোগ জমা দেওয়া যায়নি", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> complaint = new HashMap<>();
        complaint.put("id",          id);
        complaint.put("name",        name);
        complaint.put("phone",       phone);
        complaint.put("market",      market);
        complaint.put("product",     product);
        complaint.put("type",        type);
        complaint.put("description", desc);
        complaint.put("date",        date);
        complaint.put("status",      "pending");

        complaintsRef.child(id).setValue(complaint)
            .addOnSuccessListener(unused -> {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(this, "✅ অভিযোগ সফলভাবে জমা হয়েছে!", Toast.LENGTH_LONG).show();
                etName.setText(""); etPhone.setText(""); etMarket.setText("");
                etProduct.setText(""); etDescription.setText("");
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(this, "❌ অভিযোগ জমা দেওয়া যায়নি", Toast.LENGTH_SHORT).show();
            });
    }
}
