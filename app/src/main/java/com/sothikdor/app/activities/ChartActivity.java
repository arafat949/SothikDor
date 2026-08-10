package com.sothikdor.app.activities;

import com.sothikdor.R;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sothikdor.app.models.Price;
import com.sothikdor.app.utils.DateUtils;
import com.sothikdor.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    private static final String TAG = "ChartActivity";

    private LineChart lineChart;
    private TextView tvProductName, tvCurrentPrice, tvPriceChange, tvHighPrice, tvLowPrice;
    private String productId, productName, marketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        // Toolbar back button
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        productId = getIntent().getStringExtra("product_id");
        productName = getIntent().getStringExtra("product_name");
        marketId = getIntent().getStringExtra("market_id");

        lineChart = findViewById(R.id.lineChart);
        tvProductName = findViewById(R.id.tvChartProductName);
        tvCurrentPrice = findViewById(R.id.tvCurrentPrice);
        tvPriceChange = findViewById(R.id.tvPriceChange);
        tvHighPrice = findViewById(R.id.tvHighPrice);
        tvLowPrice = findViewById(R.id.tvLowPrice);

        tvProductName.setText(productName);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(productName + " - দামের ইতিহাস");

        setupChart();
        loadChartData();
    }

    private void setupChart() {
        // Chart
        lineChart.setBackgroundColor(ContextCompat.getColor(this, R.color.card_background));
        lineChart.setGridBackgroundColor(Color.TRANSPARENT);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);

        // Description
        Description desc = new Description();
        desc.setText("");
        lineChart.setDescription(desc);

        // Legend
        lineChart.getLegend().setEnabled(false);

        // Touch interaction
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // X Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        xAxis.setTextSize(10f);
        xAxis.setGranularity(1f);

        // Left Y Axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ContextCompat.getColor(this, R.color.divider));
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        leftAxis.setTextSize(10f);

        // Right Y Axis
        lineChart.getAxisRight().setEnabled(false);

        // clik for price
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                tvCurrentPrice.setText("৳" + (int) e.getY());
            }
            @Override
            public void onNothingSelected() {}
        });
    }

    // animation for 7 days
    private void loadChartData() {
        FirebaseHelper.getInstance().getLast7DaysPrices(productId, marketId,
                new FirebaseHelper.PriceHistoryCallback() {
                    @Override
                    public void onSuccess(List<Price> history) {
                        if (history.isEmpty()) {
                            // Demo data দেখানো
                            showDemoChart();
                            return;
                        }
                        updateChart(history);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Price history load failed for product " + productId + ": " + error);
                        Toast.makeText(ChartActivity.this,
                                "দামের ইতিহাস লোড হয়নি, ডেমো ডেটা দেখানো হচ্ছে", Toast.LENGTH_SHORT).show();
                        showDemoChart();
                    }
                });
    }

    private void updateChart(List<Price> history) {
        List<Entry> minEntries = new ArrayList<>();
        List<Entry> maxEntries = new ArrayList<>();
        String[] labels = new String[history.size()];

        double maxVal = 0, minVal = Double.MAX_VALUE;

        for (int i = 0; i < history.size(); i++) {
            Price p = history.get(i);
            minEntries.add(new Entry(i, (float) p.getMinPrice()));
            maxEntries.add(new Entry(i, (float) p.getMaxPrice()));
            labels[i] = DateUtils.getShortDateLabel(p.getDate());
            if (p.getMaxPrice() > maxVal) maxVal = p.getMaxPrice();
            if (p.getMinPrice() < minVal) minVal = p.getMinPrice();
        }

        // Min Price Line (green)
        LineDataSet minSet = new LineDataSet(minEntries, "সর্বনিম্ন দাম");
        styleDataSet(minSet,
                ContextCompat.getColor(this, R.color.green_primary),
                ContextCompat.getColor(this, R.color.green_fill));

        // Max Price Line (red)
        LineDataSet maxSet = new LineDataSet(maxEntries, "সর্বোচ্চ দাম");
        styleDataSet(maxSet,
                ContextCompat.getColor(this, R.color.red_price),
                ContextCompat.getColor(this, R.color.red_fill));

        // X Axis labels
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        LineData lineData = new LineData(minSet, maxSet);
        lineChart.setData(lineData);

        //animation left to right
        lineChart.animateX(1500, Easing.EaseInOutCubic);
        lineChart.invalidate();

        // Stats আপডেট
        tvHighPrice.setText("সর্বোচ্চ: ৳" + (int) maxVal);
        tvLowPrice.setText("সর্বনিম্ন: ৳" + (int) minVal);

        // সর্বশেষ দাম
        if (!history.isEmpty()) {
            Price last = history.get(history.size() - 1);
            tvCurrentPrice.setText("৳" + (int) last.getAvgPrice());
            double change = last.getPriceTrend();
            if (change > 0) {
                tvPriceChange.setText("▲ ৳" + (int) change + " বেড়েছে");
                tvPriceChange.setTextColor(ContextCompat.getColor(this, R.color.red_price));
            } else if (change < 0) {
                tvPriceChange.setText("▼ ৳" + (int) Math.abs(change) + " কমেছে");
                tvPriceChange.setTextColor(ContextCompat.getColor(this, R.color.green_primary));
            } else {
                tvPriceChange.setText("→ অপরিবর্তিত");
                tvPriceChange.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
        }
    }

    private void styleDataSet(LineDataSet dataSet, int lineColor, int fillColor) {
        dataSet.setColor(lineColor);
        dataSet.setCircleColor(lineColor);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(lineColor);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(fillColor);
        dataSet.setFillAlpha(60);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // স্মুথ কার্ভ
        dataSet.setHighlightLineWidth(1.5f);
    }

    private void showDemoChart() {
        // Firebase-এ ডেটা না থাকলে demo দেখানো
        float[] demoPrices = {55f, 58f, 60f, 62f, 60f, 63f, 65f};
        String[] demoLabels = {"৪ মার্চ", "৫ মার্চ", "৬ মার্চ", "৭ মার্চ", "৮ মার্চ", "৯ মার্চ", "আজ"};

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < demoPrices.length; i++) {
            entries.add(new Entry(i, demoPrices[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, productName);
        styleDataSet(dataSet,
                ContextCompat.getColor(this, R.color.green_primary),
                ContextCompat.getColor(this, R.color.green_fill));

        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(demoLabels));
        lineChart.setData(new LineData(dataSet));
        lineChart.animateX(1500, Easing.EaseInOutCubic);
        lineChart.invalidate();

        tvCurrentPrice.setText("৳৬৫");
        tvPriceChange.setText("▲ ৳৮ বেড়েছে");
        tvPriceChange.setTextColor(ContextCompat.getColor(this, R.color.red_price));
        tvHighPrice.setText("সর্বোচ্চ: ৳৬৫");
        tvLowPrice.setText("সর্বনিম্ন: ৳৫৫");
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
