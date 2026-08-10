package com.sothikdor.app.activities;

import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * টুলবার, up-navigation আর loading state এর সাধারণ আচরণ।
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupToolbar(String title) {
        setupToolbar(null, title);
    }

    protected void setupToolbar(@Nullable Toolbar toolbar, String title) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(title);
    }

    protected void setToolbarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(title);
    }

    protected void setLoading(ProgressBar progressBar, @Nullable View actionView, boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (actionView != null) actionView.setEnabled(!loading);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
