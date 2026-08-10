package com.sothikdor.app.activities;

import com.sothikdor.R;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sothikdor.app.models.Price;
import com.sothikdor.app.utils.ChartStyler;
import com.sothikdor.app.utils.DateUtils;
import com.sothikdor.app.utils.FirebaseHelper;
import com.sothikdor.app.utils.PriceFormatter;

import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends BaseActivity {

    private LineChart lineChart;
    private TextView tvProductName, tvCurrentPrice, tvPriceChange, tvHighPrice, tvLowPrice;
    private String productId, productName, marketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

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
        setupToolbar(findViewById(R.id.toolbar), productName + " - দামের ইতিহাস");

        setupChart();
        loadChartData();
    }

    private void setupChart() {
        ChartStyler.applyBaseStyle(lineChart);

        lineChart.setBackgroundColor(ContextCompat.getColor(this, R.color.card_background));
        lineChart.setGridBackgroundColor(Color.TRANSPARENT);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.getLegend().setEnabled(false);

        // X Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);

        // Left Y Axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ContextCompat.getColor(this, R.color.divider));
        leftAxis.setTextSize(10f);

        // clik for price
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                tvCurrentPrice.setText(PriceFormatter.taka(e.getY()));
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
        tvHighPrice.setText(PriceFormatter.maxLabel(maxVal));
        tvLowPrice.setText(PriceFormatter.minLabel(minVal));

        // সর্বশেষ দাম
        if (!history.isEmpty()) {
            Price last = history.get(history.size() - 1);
            tvCurrentPrice.setText(PriceFormatter.taka(last.getAvgPrice()));
            PriceFormatter.applyTrend(tvPriceChange, last.getPriceTrend(),
                    PriceFormatter.STABLE_UNCHANGED);
        }
    }

    private void styleDataSet(LineDataSet dataSet, int lineColor, int fillColor) {
        ChartStyler.styleDataSet(dataSet, lineColor, lineColor, fillColor, 60, true);
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
}
