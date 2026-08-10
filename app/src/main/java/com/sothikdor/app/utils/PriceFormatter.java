package com.sothikdor.app.utils;

import android.content.Context;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.sothikdor.R;

/**
 * দাম ও দামের পরিবর্তন (trend) দেখানোর সাধারণ ফরম্যাটিং।
 */
public final class PriceFormatter {

    public static final String STABLE_UNCHANGED = "→ অপরিবর্তিত";
    public static final String STABLE_STEADY = "→ স্থিতিশীল";

    private static final String TAKA = "৳";

    private PriceFormatter() {}

    public static String taka(double amount) {
        return TAKA + (long) amount;
    }

    /**
     * দশমিক রাউন্ড করা দাম।
     */
    public static String takaRounded(double amount) {
        return TAKA + Math.round(amount);
    }

    public static String takaRange(double minPrice, double maxPrice) {
        return taka(minPrice) + " - " + taka(maxPrice);
    }

    public static String takaPerUnit(double amount, String unit) {
        return taka(amount) + "/" + unit;
    }

    public static String minLabel(double minPrice) {
        return "সর্বনিম্ন: " + taka(minPrice);
    }

    public static String maxLabel(double maxPrice) {
        return "সর্বোচ্চ: " + taka(maxPrice);
    }

    public static String trendLabel(double trend, String stableLabel) {
        if (trend > 0) return "▲ " + taka(trend) + " বেড়েছে";
        if (trend < 0) return "▼ " + taka(Math.abs(trend)) + " কমেছে";
        return stableLabel;
    }

    /**
     * trend অনুযায়ী লেখা ও রঙ বসানো (বাড়লে লাল, কমলে সবুজ)।
     */
    public static void applyTrend(TextView view, double trend, String stableLabel) {
        Context context = view.getContext();
        int colorRes;
        if (trend > 0) {
            colorRes = R.color.red_price;
        } else if (trend < 0) {
            colorRes = R.color.green_primary;
        } else {
            colorRes = R.color.text_secondary;
        }
        view.setText(trendLabel(trend, stableLabel));
        view.setTextColor(ContextCompat.getColor(context, colorRes));
    }
}
