package com.sothikdor.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.sothikdor.R;
import com.sothikdor.app.utils.DateUtils;
import com.sothikdor.app.utils.FirebaseHelper;
import java.util.HashMap;
import java.util.Map;

public class ComplaintActivity extends BaseActivity {
    private EditText etName, etPhone, etMarket, etProduct, etDescription;
    private Spinner spinnerType;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private DatabaseReference complaintsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        setToolbarTitle("অভিযোগ / Complaint Box");

        complaintsRef = FirebaseHelper.getDatabase().getReference("complaints");

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

        setLoading(progressBar, btnSubmit, true);

        String date = DateUtils.getCurrentDateTime();
        String id   = complaintsRef.push().getKey();

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
                setLoading(progressBar, btnSubmit, false);
                Toast.makeText(this, "✅ অভিযোগ সফলভাবে জমা হয়েছে!", Toast.LENGTH_LONG).show();
                etName.setText(""); etPhone.setText(""); etMarket.setText("");
                etProduct.setText(""); etDescription.setText("");
            })
            .addOnFailureListener(e -> {
                setLoading(progressBar, btnSubmit, false);
                Toast.makeText(this, "❌ সমস্যা হয়েছে: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
