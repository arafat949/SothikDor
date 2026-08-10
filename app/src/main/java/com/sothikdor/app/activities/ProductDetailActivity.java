package com.sothikdor.app.activities;

import com.sothikdor.R;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.sothikdor.app.models.Price;
import com.sothikdor.app.utils.ChartStyler;
import com.sothikdor.app.utils.FirebaseHelper;
import com.sothikdor.app.utils.PriceFormatter;
import com.sothikdor.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends BaseActivity {

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
        setupToolbar(findViewById(R.id.toolbar), productName);
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
        tvCurrentPrice.setText(PriceFormatter.taka(latestPrice.getAvgPrice())
                + " / " + latestPrice.getUnit());
        tvMinPrice.setText(PriceFormatter.minLabel(latestPrice.getMinPrice()));
        tvMaxPrice.setText(PriceFormatter.maxLabel(latestPrice.getMaxPrice()));
        tvUnit.setText(latestPrice.getUnit());

        double trend = latestPrice.getPriceTrend();
        tvTrend.setText(PriceFormatter.trendLabel(trend, PriceFormatter.STABLE_UNCHANGED));
        if (trend > 0) {
            tvTrend.setTextColor(Color.RED);
        } else if (trend < 0) {
            tvTrend.setTextColor(ContextCompat.getColor(this, R.color.green_primary));
        } else {
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

        ChartStyler.styleDataSet(dataSet, greenColor, amberColor, fillColor, 80, false);
        dataSet.setHighLightColor(amberColor);

        // Chart কনফিগারেশন
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        ChartStyler.applyBaseStyle(lineChart);

        // X Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGridColor(Color.parseColor("#E8E8E8"));
        xAxis.setAxisLineColor(Color.parseColor("#E8E8E8"));

        // Y Axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGridColor(Color.parseColor("#E8E8E8"));
        leftAxis.setAxisLineColor(Color.parseColor("#E8E8E8"));

        lineChart.getLegend().setTextColor(ContextCompat.getColor(this, R.color.text_primary));
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
}
