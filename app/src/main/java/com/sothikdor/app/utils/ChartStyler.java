package com.sothikdor.app.utils;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.sothikdor.R;

/**
 * MPAndroidChart লাইন চার্টের সাধারণ স্টাইল।
 */
public final class ChartStyler {

    private ChartStyler() {}

    /**
     * টাচ/জুম, description, right axis আর x-axis এর সাধারণ কনফিগারেশন।
     */
    public static void applyBaseStyle(LineChart chart) {
        Context context = chart.getContext();

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));

        chart.getAxisLeft().setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
    }

    /**
     * লাইন, বৃত্ত আর fill এর সাধারণ স্টাইল।
     */
    public static void styleDataSet(LineDataSet dataSet, int lineColor, int circleColor,
                                    int fillColor, int fillAlpha, boolean drawValues) {
        dataSet.setColor(lineColor);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(circleColor);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setDrawValues(drawValues);
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(lineColor);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(fillColor);
        dataSet.setFillAlpha(fillAlpha);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setHighlightLineWidth(1.5f);
    }
}
