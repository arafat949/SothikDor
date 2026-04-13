package com.sothikdor.app.activities;

import com.sothikdor.R;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.sothikdor.app.adapters.BudgetAdapter;
import com.sothikdor.app.models.BudgetItem;
import com.sothikdor.app.models.Price;
import com.sothikdor.app.utils.DateUtils;
import com.sothikdor.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class BudgetActivity extends AppCompatActivity implements BudgetAdapter.OnQuantityChangedListener {

    private RecyclerView recyclerBudget;
    private BudgetAdapter budgetAdapter;
    private TextView tvTotalCost, tvItemCount, tvMarketInfo;
    private MaterialButton btnClearAll, btnShare;

    private List<BudgetItem> budgetItems = new ArrayList<>();
    private double totalCost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        if (getSupportActionBar() != null) {
            if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("বাজার বাজেট হিসাব");
        }

        recyclerBudget = findViewById(R.id.recyclerBudget);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvMarketInfo = findViewById(R.id.tvMarketInfo);
        btnClearAll = findViewById(R.id.btnClearAll);
        btnShare = findViewById(R.id.btnShare);

        budgetAdapter = new BudgetAdapter(this, budgetItems, this);
        recyclerBudget.setLayoutManager(new LinearLayoutManager(this));
        recyclerBudget.setAdapter(budgetAdapter);

        tvMarketInfo.setText("কারওয়ান বাজার • " + DateUtils.getTodayDateBangla());

        btnClearAll.setOnClickListener(v -> {
            for (BudgetItem item : budgetItems) {
                item.setQuantity(0);
            }
            budgetAdapter.notifyDataSetChanged();
            updateTotal();
        });

        btnShare.setOnClickListener(v -> shareList());

        loadProductsForBudget();
    }

    private void loadProductsForBudget() {
        String today = DateUtils.getTodayDate();
        FirebaseHelper.getInstance().getPricesByDate(today, "m001",
                new FirebaseHelper.PriceCallback() {
                    @Override
                    public void onSuccess(List<Price> prices) {
                        budgetItems.clear();
                        for (Price price : prices) {
                            BudgetItem item = new BudgetItem(
                                    price.getProductId(),
                                    price.getProductName(),
                                    price.getProductEmoji(),
                                    price.getUnit(),
                                    price.getAvgPrice()
                            );
                            item.setQuantity(0);
                            budgetItems.add(item);
                        }
                        budgetAdapter.notifyDataSetChanged();
                        updateTotal();
                    }

                    @Override
                    public void onError(String error) {
                        // Demo items দেখানো
                        loadDemoItems();
                    }
                });
    }

    private void loadDemoItems() {
        String[][] demoData = {
            {"p001", "আলু (দেশি)", "🥔", "কেজি", "35"},
            {"p002", "পেঁয়াজ (দেশি)", "🧅", "কেজি", "65"},
            {"p003", "রসুন (দেশি)", "🧄", "কেজি", "120"},
            {"p004", "মিনিকেট চাল", "🌾", "কেজি", "72"},
            {"p006", "মসুর ডাল", "🫘", "কেজি", "105"},
            {"p007", "সয়াবিন তেল", "🛢️", "লিটার", "175"},
            {"p008", "রুই মাছ", "🐟", "কেজি", "320"},
            {"p010", "ব্রয়লার মুরগি", "🍗", "কেজি", "195"},
            {"p012", "ডিম (ফার্ম)", "🥚", "হালি", "45"},
            {"p013", "টমেটো", "🍅", "কেজি", "50"}
        };

        budgetItems.clear();
        for (String[] d : demoData) {
            BudgetItem item = new BudgetItem(d[0], d[1], d[2], d[3], Double.parseDouble(d[4]));
            item.setQuantity(0);
            budgetItems.add(item);
        }
        budgetAdapter.notifyDataSetChanged();
        updateTotal();
    }

    @Override
    public void onQuantityChanged() {
        updateTotal();
    }

    private void updateTotal() {
        totalCost = 0;
        int itemCount = 0;
        for (BudgetItem item : budgetItems) {
            if (item.getQuantity() > 0) {
                totalCost += item.getTotalCost();
                itemCount++;
            }
        }
        tvTotalCost.setText("৳" + String.format("%.0f", totalCost));
        tvItemCount.setText(itemCount + "টি পণ্য");
    }

    private void shareList() {
        StringBuilder sb = new StringBuilder();
        sb.append("🛒 সঠিক দর - বাজার লিস্ট\n");
        sb.append("তারিখ: ").append(DateUtils.getTodayDateBangla()).append("\n");
        sb.append("বাজার: কারওয়ান বাজার\n\n");
        for (BudgetItem item : budgetItems) {
            if (item.getQuantity() > 0) {
                sb.append(item.getEmoji()).append(" ").append(item.getProductName())
                  .append(": ").append(item.getQuantity()).append(" ").append(item.getUnit())
                  .append(" = ৳").append(String.format("%.0f", item.getTotalCost())).append("\n");
            }
        }
        sb.append("\n💰 মোট: ৳").append(String.format("%.0f", totalCost));

        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
        startActivity(android.content.Intent.createChooser(shareIntent, "শেয়ার করুন"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
