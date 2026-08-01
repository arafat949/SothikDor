package com.sothikdor.app.activities;

import com.sothikdor.R;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothikdor.app.adapters.PriceAdapter;
import com.sothikdor.app.models.Price;
import com.sothikdor.app.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PriceAdapter priceAdapter;
    private List<Price> allPrices = new ArrayList<>();
    private List<Price> filteredPrices = new ArrayList<>();
    private EditText etSearch;
    private ChipGroup chipGroupCategory;
    private TextView tvDate, tvMarketName, tvLiveIndicator;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutEmpty;
    private String selectedCategory = "সব";
    private String currentDistrictKey = "dhaka";
    private String currentDistrictName = "ঢাকা";

    private final String[] DISTRICT_NAMES = {
        "ঢাকা","নারায়ণগঞ্জ","গাজীপুর","মানিকগঞ্জ","মুন্সিগঞ্জ","নরসিংদী",
        "ফরিদপুর","গোপালগঞ্জ","মাদারীপুর","রাজবাড়ী","শরীয়তপুর",
        "চট্টগ্রাম","কক্সবাজার","কুমিল্লা","ফেনী","নোয়াখালী","লক্ষ্মীপুর",
        "চাঁদপুর","ব্রাহ্মণবাড়িয়া","খাগড়াছড়ি","রাঙামাটি","বান্দরবান",
        "সিলেট","মৌলভীবাজার","হবিগঞ্জ","সুনামগঞ্জ",
        "রাজশাহী","চাঁপাইনবাবগঞ্জ","নাটোর","নওগাঁ","বগুড়া","জয়পুরহাট","পাবনা","সিরাজগঞ্জ",
        "খুলনা","যশোর","সাতক্ষীরা","বাগেরহাট","নড়াইল","মাগুরা","ঝিনাইদহ","কুষ্টিয়া","চুয়াডাঙ্গা","মেহেরপুর",
        "বরিশাল","ভোলা","পটুয়াখালী","পিরোজপুর","ঝালকাঠি","বরগুনা",
        "রংপুর","দিনাজপুর","ঠাকুরগাঁও","পঞ্চগড়","নীলফামারী","লালমনিরহাট","কুড়িগ্রাম","গাইবান্ধা",
        "ময়মনসিংহ","জামালপুর","শেরপুর","নেত্রকোণা","কিশোরগঞ্জ","টাঙ্গাইল"
    };

    private final String[] DISTRICT_KEYS = {
        "dhaka","narayanganj","gazipur","manikganj","munshiganj","narsingdi",
        "faridpur","gopalganj","madaripur","rajbari","shariatpur",
        "chittagong","coxsbazar","comilla","feni","noakhali","lakshmipur",
        "chandpur","brahmanbaria","khagrachhari","rangamati","bandarban",
        "sylhet","moulvibazar","habiganj","sunamganj",
        "rajshahi","chapainawabganj","natore","naogaon","bogura","joypurhat","pabna","sirajganj",
        "khulna","jessore","satkhira","bagerhat","narail","magura","jhenaidah","kushtia","chuadanga","meherpur",
        "barisal","bhola","patuakhali","pirojpur","jhalokati","barguna",
        "rangpur","dinajpur","thakurgaon","panchagarh","nilphamari","lalmonirhat","kurigram","gaibandha",
        "mymensingh","jamalpur","sherpur","netrokona","kishoreganj","tangail"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
        setupBottomNavigation();
        setupSwipeRefresh();
        loadPriceData();
        tvDate.setText(DateUtils.getTodayDateBangla());
        tvMarketName.setOnClickListener(v -> showDistrictPicker());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewPrices);
        etSearch = findViewById(R.id.etSearch);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        tvDate = findViewById(R.id.tvDate);
        tvMarketName = findViewById(R.id.tvMarketName);
        tvLiveIndicator = findViewById(R.id.tvLiveIndicator);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        priceAdapter = new PriceAdapter(this, filteredPrices, price -> {
            Intent intent = new Intent(this, ChartActivity.class);
            intent.putExtra("product_id", price.getProductId());
            intent.putExtra("product_name", price.getProductName());
            intent.putExtra("market_id", price.getMarketId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(priceAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 10) etSearch.clearFocus();
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPrices(s.toString(), selectedCategory);
            }
        });
    }

    private void setupCategoryChips() {
        String[] categories = {"সব","সবজি","মাছ","মাংস","চাল-ডাল","তেল-মশলা","মশলা","ডিম"};
        for (String cat : categories) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChecked(cat.equals("সব"));
            chip.setChipBackgroundColorResource(R.color.chip_background_color);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color));
            chipGroupCategory.addView(chip);
            chip.setOnClickListener(v -> {
                selectedCategory = cat;
                filterPrices(etSearch.getText().toString(), cat);
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_compare) {
                startActivity(new Intent(this, CompareActivity.class));
                return true;
            } else if (id == R.id.nav_budget) {
                startActivity(new Intent(this, BudgetActivity.class));
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, MapActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.green_primary, R.color.amber_primary);
        swipeRefresh.setOnRefreshListener(() -> {
            loadPriceData();
            Toast.makeText(this, "ডেটা আপডেট হচ্ছে...", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadPriceData() {
        String today = DateUtils.getTodayDate();
        tvLiveIndicator.setVisibility(View.VISIBLE);
        tvMarketName.setText("📍 " + currentDistrictName);

        FirebaseDatabase.getInstance("https://sothik-dor-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("prices/" + today)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    swipeRefresh.setRefreshing(false);
                    allPrices.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Price price = child.getValue(Price.class);
                        if (price != null) {
                            price.setPriceId(child.getKey());
                            allPrices.add(price);
                        }
                    }
                    filterPrices(etSearch.getText().toString(), selectedCategory);
                    if (allPrices.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    swipeRefresh.setRefreshing(false);
                    tvLiveIndicator.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "ডেটা লোড হয়নি", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showDistrictPicker() {
        new AlertDialog.Builder(this)
            .setTitle("আপনার জেলা বেছে নিন")
            .setItems(DISTRICT_NAMES, (dialog, which) -> {
                currentDistrictKey = DISTRICT_KEYS[which];
                currentDistrictName = DISTRICT_NAMES[which];
                loadPriceData();
                Toast.makeText(this, currentDistrictName + " এর দাম দেখানো হচ্ছে", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private void filterPrices(String query, String category) {
        filteredPrices.clear();
        // প্রতিটি পণ্যের সবচেয়ে কম দামের বাজার রাখি
        java.util.Map<String, Price> bestPriceMap = new java.util.LinkedHashMap<>();
        for (Price price : allPrices) {
            boolean matchesQuery = query.isEmpty() || price.getProductName().contains(query);
            boolean matchesCategory = category.equals("সব") || category.equals(price.getCategory());
            if (matchesQuery && matchesCategory) {
                String pid = price.getProductId();
                if (!bestPriceMap.containsKey(pid)) {
                    bestPriceMap.put(pid, price);
                } else {
                    // সবচেয়ে কম avgPrice রাখি
                    if (price.getAvgPrice() < bestPriceMap.get(pid).getAvgPrice()) {
                        bestPriceMap.put(pid, price);
                    }
                }
            }
        }
        filteredPrices.addAll(bestPriceMap.values());
        priceAdapter.notifyDataSetChanged();
        if (filteredPrices.isEmpty() && !allPrices.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_dark_mode) {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
