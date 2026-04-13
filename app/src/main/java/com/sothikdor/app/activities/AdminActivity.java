package com.sothikdor.app.activities;

import com.sothikdor.R;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.sothikdor.app.models.Price;
import com.sothikdor.app.utils.FirebaseHelper;

public class AdminActivity extends AppCompatActivity {

    private Spinner spinnerProduct, spinnerMarket;
    private EditText etMinPrice, etMaxPrice;
    private Button btnSave;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;

    private final String[] productNames = {
            "আলু (দেশি)","পেঁয়াজ (দেশি)","টমেটো","বেগুন","লাউ",
            "মিনিকেট চাল","নাজিরশাইল চাল","মসুর ডাল",
            "সয়াবিন তেল","রসুন","আদা",
            "রুই মাছ","কাতলা মাছ","ইলিশ মাছ",
            "মুরগি (ব্রয়লার)","গরুর মাংস",
            "ডিম (হালি)","কলা"
    };
    private final String[] productIds = {
            "p001","p002","p003","p004","p005",
            "p006","p007","p008",
            "p009","p010","p011",
            "p012","p013","p014",
            "p015","p016",
            "p017","p018"
    };
    private final String[] productEmojis = {
            "🥔","🧅","🍅","🍆","🥬",
            "🌾","🌾","🫘",
            "🛢️","🧄","🫚",
            "🐟","🐠","🐡",
            "🍗","🥩",
            "🥚","🍌"
    };
    private final String[] productCategories = {
            "সবজি","সবজি","সবজি","সবজি","সবজি",
            "চাল-ডাল","চাল-ডাল","চাল-ডাল",
            "তেল-মশলা","তেল-মশলা","তেল-মশলা",
            "মাছ","মাছ","মাছ",
            "মাংস","মাংস",
            "দুগ্ধজাত","ফলমূল"
    };
    private final String[] productUnits = {
            "কেজি","কেজি","কেজি","কেজি","পিস",
            "কেজি","কেজি","কেজি",
            "লিটার","কেজি","কেজি",
            "কেজি","কেজি","কেজি",
            "কেজি","কেজি",
            "হালি","হালি"
    };

    private final String[] marketNames = {
            "কারওয়ান বাজার","শান্তিনগর বাজার",
            "মোহাম্মদপুর বাজার","মিরপুর ১০ বাজার",
            "রামপুরা বাজার","যাত্রাবাড়ী বাজার"
    };
    private final String[] marketIds = {
            "m001","m002","m003","m004","m005","m006"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        firebaseHelper = FirebaseHelper.getInstance();

        initViews();
        setupToolbar();
        setupSpinners();
        setupSaveButton();
    }

    private void initViews() {
        spinnerProduct = findViewById(R.id.spinnerProduct);
        spinnerMarket  = findViewById(R.id.spinnerMarket);
        etMinPrice     = findViewById(R.id.etMinPrice);
        etMaxPrice     = findViewById(R.id.etMaxPrice);
        btnSave        = findViewById(R.id.btnSave);
        progressBar    = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("⚙️ দাম আপডেট (Admin)");
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> productAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, productNames);
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(productAdapter);

        ArrayAdapter<String> marketAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, marketNames);
        marketAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarket.setAdapter(marketAdapter);
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            String minStr = etMinPrice.getText().toString().trim();
            String maxStr = etMaxPrice.getText().toString().trim();

            if (minStr.isEmpty()) { etMinPrice.setError("সর্বনিম্ন দাম দিন"); return; }
            if (maxStr.isEmpty()) { etMaxPrice.setError("সর্বোচ্চ দাম দিন"); return; }

            double min = Double.parseDouble(minStr);
            double max = Double.parseDouble(maxStr);

            if (min > max) {
                Toast.makeText(this, "সর্বনিম্ন দাম সর্বোচ্চ দামের চেয়ে বেশি হতে পারবে না", Toast.LENGTH_SHORT).show();
                return;
            }

            int pIdx = spinnerProduct.getSelectedItemPosition();
            int mIdx = spinnerMarket.getSelectedItemPosition();

            String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
            Price price = new Price(productIds[pIdx], marketIds[mIdx], min, max, today);
            price.setMarketName(marketNames[mIdx]);
            price.setProductName(productNames[pIdx]);
            price.setProductEmoji(productEmojis[pIdx]);
            price.setCategory(productCategories[pIdx]);
            price.setUnit(productUnits[pIdx]);

            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            // ✅ Firebase এ Real-time আপডেট
            firebaseHelper.addPrice(price.getDate(), price, new FirebaseHelper.SimpleCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        etMinPrice.setText("");
                        etMaxPrice.setText("");
                        Toast.makeText(AdminActivity.this,
                                "✅ দাম সফলভাবে আপডেট হয়েছে!\nসব ইউজার এখনই দেখতে পাবেন।",
                                Toast.LENGTH_LONG).show();
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(AdminActivity.this,
                                "আপডেট ব্যর্থ: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
