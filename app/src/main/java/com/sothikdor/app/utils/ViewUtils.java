package com.sothikdor.app.utils;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public final class ViewUtils {

    private ViewUtils() {}

    public static void bindSpinner(Spinner spinner, String[] items) {
        Context context = spinner.getContext();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
