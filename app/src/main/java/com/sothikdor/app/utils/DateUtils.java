package com.sothikdor.app.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String[] BANGLA_MONTHS = {
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    };
    private static final String[] BANGLA_DAYS = {
        "রোববার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"
    };

    /**
     * আজকের তারিখ (yyyy-MM-dd format - Firebase key হিসেবে)
     */
    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * এখনকার তারিখ ও সময় (yyyy-MM-dd HH:mm)
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * আজকের তারিখ বাংলায়
     */
    public static String getTodayDateBangla() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        return BANGLA_DAYS[dayOfWeek] + ", " +
               toBanglaNumber(day) + " " +
               BANGLA_MONTHS[month] + " " +
               toBanglaNumber(year);
    }

    /**
     * শেষ n দিনের তারিখ লিস্ট
     */
    public static String[] getLastNDates(int n) {
        String[] dates = new String[n];
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        for (int i = n - 1; i >= 0; i--) {
            dates[n - 1 - i] = sdf.format(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        return dates;
    }

    /**
     * তারিখ থেকে সংক্ষিপ্ত লেবেল (Chart-এর জন্য)
     */
    public static String getShortDateLabel(String date) {
        try {
            String[] parts = date.split("-");
            int day = Integer.parseInt(parts[2]);
            int month = Integer.parseInt(parts[1]) - 1;
            return toBanglaNumber(day) + " " + BANGLA_MONTHS[month].substring(0, 3);
        } catch (Exception e) {
            return date;
        }
    }

    /**
     * ইংরেজি সংখ্যাকে বাংলায় রূপান্তর
     */
    public static String toBanglaNumber(int number) {
        String[] banglaDigits = {"০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯"};
        StringBuilder result = new StringBuilder();
        for (char c : String.valueOf(number).toCharArray()) {
            if (Character.isDigit(c)) {
                result.append(banglaDigits[c - '0']);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * মূল্যকে বাংলা ফরম্যাটে দেখানো
     */
    public static String formatPriceBangla(double price) {
        return "৳" + toBanglaNumber((int) price);
    }
}
