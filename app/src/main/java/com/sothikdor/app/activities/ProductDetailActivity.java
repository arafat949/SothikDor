package com.sothikdor.app.activities;

import com.sothikdor.R;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.sothikdor.app.models.Price;
import com.sothikdor.app.utils.FirebaseHelper;
import com.sothikdor.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private LineChart lineChart;
    private TextView tvProductName, tvCurrentPrice, tvMinPrice, tvMaxPrice, tvTrend;
    private TextView tvMarketName, tvUnit;

    private FirebaseHelper firebaseHelper;
    private SessionManager session;
    private String productId, productName, marketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Intent থেকে ডেটা নাও
        productId   = getIntent().getStringExtra("product_id");
        productName = getIntent().getStringExtra("product_name");
        marketId    = getIntent().getStringExtra("market_id");

        firebaseHelper = FirebaseHelper.getInstance();
        session = SessionManager.getInstance(this);

        initViews();
        setupToolbar();
        loadPriceHistory();
    }

    private void initViews() {
        lineChart     = findViewById(R.id.lineChart);
        tvProductName = findViewById(R.id.tvProductName);
        tvCurrentPrice = findViewById(R.id.tvCurrentPrice);
        tvMinPrice    = findViewById(R.id.tvMinPrice);
        tvMaxPrice    = findViewById(R.id.tvMaxPrice);
        tvTrend       = findViewById(R.id.tvTrend);
        tvMarketName  = findViewById(R.id.tvMarketName);
        tvUnit        = findViewById(R.id.tvUnit);

        tvProductName.setText(productName);
        tvMarketName.setText(session.getPreferredMarketName());
    }

    private void setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(productName);
        }
    }

    // ✅ গত ৩০ দিনের দামের ইতিহাস লোড করা
    private void loadPriceHistory() {
        firebaseHelper.getLast7DaysPrices(productId, marketId,
                new FirebaseHelper.PriceHistoryCallback() {
                    @Override
                    public void onSuccess(List<Price> prices) {
                        runOnUiThread(() -> {
                            if (!prices.isEmpty()) {
                                updateCurrentInfo(prices.get(prices.size() - 1));
                                setupChart(prices);
                            } else {
                                setupDemoChart();
                            }
                        });
                    }
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> setupDemoChart());
                    }
                });
    }

    private void updateCurrentInfo(Price latestPrice) {
        String priceText = "৳" + (int) latestPrice.getAvgPrice() + " / " + latestPrice.getUnit();
        tvCurrentPrice.setText(priceText);
        tvMinPrice.setText("সর্বনিম্ন: ৳" + (int) latestPrice.getMinPrice());
        tvMaxPrice.setText("সর্বোচ্চ: ৳" + (int) latestPrice.getMaxPrice());
        tvUnit.setText(latestPrice.getUnit());

        double trend = latestPrice.getPriceTrend();
        if (trend > 0) {
            tvTrend.setText("▲ ৳" + (int) trend + " বেড়েছে");
            tvTrend.setTextColor(Color.RED);
        } else if (trend < 0) {
            tvTrend.setText("▼ ৳" + Math.abs((int) trend) + " কমেছে");
            tvTrend.setTextColor(ContextCompat.getColor(this, R.color.green_primary));
        } else {
            tvTrend.setText("→ অপরিবর্তিত");
            tvTrend.setTextColor(Color.GRAY);
        }
    }

    // ✅ MPAndroidChart দিয়ে সুন্দর গ্রাফ
    private void setupChart(List<Price> prices) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < prices.size(); i++) {
            Price p = prices.get(i);
            entries.add(new Entry(i, (float) p.getAvgPrice()));

            // তারিখ থেকে শুধু দিন নম্বর
            String date = p.getDate();
            if (date != null && date.length() >= 10) {
                labels.add(date.substring(8)); // dd
            } else {
                labels.add(String.valueOf(i + 1));
            }
        }

        // DataSet তৈরি
        LineDataSet dataSet = new LineDataSet(entries, productName + " এর দাম (৳)");

        int greenColor = ContextCompat.getColor(this, R.color.green_primary);
        int amberColor = ContextCompat.getColor(this, R.color.amber_primary);
        int fillColor  = ContextCompat.getColor(this, R.color.green_pale);

        dataSet.setColor(greenColor);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(amberColor);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(fillColor);
        dataSet.setFillAlpha(80);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // মসৃণ লাইন

        // Highlight
        dataSet.setHighLightColor(amberColor);
        dataSet.setHighlightLineWidth(1.5f);

        // Chart কনফিগারেশন
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // X Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        xAxis.setGridColor(Color.parseColor("#E8E8E8"));
        xAxis.setAxisLineColor(Color.parseColor("#E8E8E8"));

        // Y Axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        leftAxis.setGridColor(Color.parseColor("#E8E8E8"));
        leftAxis.setAxisLineColor(Color.parseColor("#E8E8E8"));
        lineChart.getAxisRight().setEnabled(false);

        // চার্ট স্টাইল
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setBackgroundColor(Color.WHITE);

        // ✅ অ্যানিমেশন সহ চার্ট দেখানো
        lineChart.animateX(1200);
        lineChart.invalidate();
    }

    // Demo চার্ট যদি Firebase ডেটা না থাকে
    private void setupDemoChart() {
        tvCurrentPrice.setText("৳৩৫ / কেজি");
        tvMinPrice.setText("সর্বনিম্ন: ৳৩০");
        tvMaxPrice.setText("সর্বোচ্চ: ৳৪০");

        // ডেমো দামের ডেটা
        float[] demoPrices = {38, 40, 37, 35, 38, 42, 40, 38, 35, 33,
                35, 37, 40, 38, 36, 34, 35, 37, 39, 38,
                36, 34, 33, 35, 36, 38, 37, 35, 36, 35};

        List<Price> demoPriceList = new ArrayList<>();
        for (int i = 0; i < demoPrices.length; i++) {
            Price p = new Price();
            p.setAvgPrice(demoPrices[i]);
            p.setMinPrice(demoPrices[i] - 3);
            p.setMaxPrice(demoPrices[i] + 3);
            p.setDate("2026-02-" + String.format("%02d", i + 9));
            demoPriceList.add(p);
        }

        setupChart(demoPriceList);
        tvTrend.setText("→ ডেমো ডেটা");
        tvTrend.setTextColor(Color.GRAY);
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
