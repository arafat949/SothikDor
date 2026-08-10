package com.sothikdor.app.activities;

import com.sothikdor.R;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sothikdor.app.adapters.CompareAdapter;
import com.sothikdor.app.models.Market;
import com.sothikdor.app.models.Price;
import com.sothikdor.app.utils.DateUtils;
import com.sothikdor.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompareActivity extends AppCompatActivity {

    private static final String TAG = "CompareActivity";

    private Spinner spinnerProduct;
    private RecyclerView recyclerCompare;
    private ProgressBar progressBar;
    private TextView tvBestMarket, tvBestPrice;
    private CompareAdapter compareAdapter;

    private List<Price> allProducts = new ArrayList<>();
    private List<Market> marketList = new ArrayList<>();
    private String[] productNames;
    private String[] productIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        if (getSupportActionBar() != null) {
            if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("বাজার দাম তুলনা");
        }

        spinnerProduct = findViewById(R.id.spinnerProduct);
        recyclerCompare = findViewById(R.id.recyclerCompare);
        progressBar = findViewById(R.id.progressBar);
        tvBestMarket = findViewById(R.id.tvBestMarket);
        tvBestPrice = findViewById(R.id.tvBestPrice);

        recyclerCompare.setLayoutManager(new LinearLayoutManager(this));
        compareAdapter = new CompareAdapter(this, new ArrayList<>());
        recyclerCompare.setAdapter(compareAdapter);

        loadProductsForSpinner();
    }

    private void loadProductsForSpinner() {
        progressBar.setVisibility(View.VISIBLE);
        String today = DateUtils.getTodayDate();

        // সব বাজারের সব দাম লোড করা
        FirebaseHelper.getInstance().getPricesByDate(today, null,
                new FirebaseHelper.PriceCallback() {
                    @Override
                    public void onSuccess(List<Price> prices) {
                        allProducts.clear();
                        allProducts.addAll(prices);

                        // Unique products বের করা
                        Map<String, String> uniqueProducts = new HashMap<>();
                        for (Price p : prices) {
                            uniqueProducts.put(p.getProductId(), p.getProductName());
                        }

                        productIds = uniqueProducts.keySet().toArray(new String[0]);
                        productNames = uniqueProducts.values().toArray(new String[0]);

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                CompareActivity.this,
                                android.R.layout.simple_spinner_item,
                                productNames
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerProduct.setAdapter(adapter);

                        spinnerProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                compareProductPrices(productIds[position], productNames[position]);
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                        progressBar.setVisibility(View.GONE);

                        // প্রথম পণ্য দেখানো
                        if (productIds.length > 0) {
                            compareProductPrices(productIds[0], productNames[0]);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Price comparison load failed: " + error);
                        progressBar.setVisibility(View.GONE);
                        tvBestMarket.setText("দাম লোড হয়নি");
                        tvBestPrice.setText("");
                        Toast.makeText(CompareActivity.this,
                                "দাম লোড হয়নি: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void compareProductPrices(String productId, String productName) {
        List<Price> productPrices = new ArrayList<>();
        for (Price p : allProducts) {
            if (p.getProductId().equals(productId)) {
                productPrices.add(p);
            }
        }

        // দাম অনুযায়ী সাজানো (সস্তা থেকে দামি)
        Collections.sort(productPrices, (a, b) ->
                Double.compare(a.getMinPrice(), b.getMinPrice()));

        compareAdapter.updateData(productPrices, productName);

        // সেরা বাজার হাইলাইট করা
        if (!productPrices.isEmpty()) {
            Price cheapest = productPrices.get(0);
            tvBestMarket.setText("💡 সেরা: " + cheapest.getMarketName());
            tvBestPrice.setText("৳" + (int) cheapest.getMinPrice() + " থেকে");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
