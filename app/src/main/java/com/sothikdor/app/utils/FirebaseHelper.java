package com.sothikdor.app.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothikdor.app.models.Market;
import com.sothikdor.app.models.Price;
import com.sothikdor.app.models.Product;
import com.sothikdor.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";

    private static FirebaseHelper instance;
    private final DatabaseReference mDatabase;
    private final FirebaseAuth mAuth;

    // Firebase Node names
    private static final String NODE_PRODUCTS = "products";
    private static final String NODE_PRICES = "prices";
    private static final String NODE_MARKETS = "markets";
    private static final String NODE_USERS = "users";

    private FirebaseHelper() {
        // Firebase Offline Persistence (cache) - অফলাইনে কাজ করবে
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://sothik-dor-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDatabase = database.getReference();

        // Prices node cache রাখা
        mDatabase.child(NODE_PRICES).keepSynced(true);
        mDatabase.child(NODE_PRODUCTS).keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    // ==================== AUTH ====================

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void signOut() {
        mAuth.signOut();
    }

    // ==================== PRODUCTS ====================

    /**
     * সব পণ্যের তালিকা পড়া (Real-time listener)
     */
    public void getAllProducts(final ProductCallback callback) {
        mDatabase.child(NODE_PRODUCTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Product> products = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Product product = child.getValue(Product.class);
                            if (product != null && product.isActive()) {
                                product.setProductId(child.getKey());
                                products.add(product);
                            }
                        }
                        callback.onSuccess(products);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(logAndDescribe("getAllProducts", error));
                    }
                });
    }

    // ==================== PRICES ====================

    /**
     * নির্দিষ্ট তারিখের সব দাম পড়া (Real-time - দাম আপডেট হলে সাথে সাথে দেখাবে)
     */
    public void getPricesByDate(String date, String marketId, final PriceCallback callback) {
        mDatabase.child(NODE_PRICES)
                .child(date)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Price> prices = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Price price = child.getValue(Price.class);
                            if (price != null) {
                                price.setPriceId(child.getKey());
                                // নির্দিষ্ট বাজার ফিল্টার
                                if (marketId == null || marketId.isEmpty()
                                        || marketId.equals(price.getMarketId())) {
                                    prices.add(price);
                                }
                            }
                        }
                        callback.onSuccess(prices);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(logAndDescribe("getPricesByDate(" + date + ")", error));
                    }
                });
    }

    /**
     * একটি পণ্যের বিভিন্ন বাজারের দাম তুলনা
     */
    public void getPricesByProduct(String productId, String date, final PriceCallback callback) {
        mDatabase.child(NODE_PRICES)
                .child(date)
                .orderByChild("productId")
                .equalTo(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Price> prices = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Price price = child.getValue(Price.class);
                            if (price != null) {
                                price.setPriceId(child.getKey());
                                prices.add(price);
                            }
                        }
                        callback.onSuccess(prices);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(logAndDescribe("getPricesByProduct(" + productId + ")", error));
                    }
                });
    }

    /**
     * একটি পণ্যের শেষ ৭ দিনের দাম (Chart-এর জন্য)
     */
    public void getLast7DaysPrices(String productId, String marketId, final PriceHistoryCallback callback) {
        mDatabase.child(NODE_PRICES)
                .limitToLast(7)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Price> history = new ArrayList<>();
                        for (DataSnapshot dateSnap : snapshot.getChildren()) {
                            for (DataSnapshot priceSnap : dateSnap.getChildren()) {
                                Price price = priceSnap.getValue(Price.class);
                                if (price != null
                                        && productId.equals(price.getProductId())
                                        && (marketId == null || marketId.equals(price.getMarketId()))) {
                                    price.setDate(dateSnap.getKey());
                                    history.add(price);
                                }
                            }
                        }
                        callback.onSuccess(history);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(logAndDescribe("getLast7DaysPrices(" + productId + ")", error));
                    }
                });
    }

    /**
     * নতুন দাম যোগ করা (Admin only)
     */
    public void addPrice(String date, Price price, final SimpleCallback callback) {
        String key = price.getProductId() + "_" + price.getMarketId();
        mDatabase.child(NODE_PRICES)
                .child(date)
                .child(key)
                .setValue(price)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "addPrice failed for " + key + " on " + date, e);
                    callback.onError(describe(e));
                });
    }

    // ==================== MARKETS ====================

    /**
     * সব বাজারের তালিকা
     */
    public void getAllMarkets(final MarketCallback callback) {
        mDatabase.child(NODE_MARKETS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Market> markets = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Market market = child.getValue(Market.class);
                            if (market != null) {
                                market.setMarketId(child.getKey());
                                markets.add(market);
                            }
                        }
                        callback.onSuccess(markets);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(logAndDescribe("getAllMarkets", error));
                    }
                });
    }

    // ==================== USER ====================

    public void saveUser(User user, final SimpleCallback callback) {
        mDatabase.child(NODE_USERS)
                .child(user.getUserId())
                .setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "saveUser failed for " + user.getUserId(), e);
                    callback.onError(describe(e));
                });
    }

    public void getUser(String userId, final UserCallback callback) {
        mDatabase.child(NODE_USERS)
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            callback.onSuccess(user);
                        } else {
                            Log.w(TAG, "User " + userId + " not found");
                            callback.onError("User not found");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(logAndDescribe("getUser(" + userId + ")", error));
                    }
                });
    }

    // ==================== SAMPLE DATA ====================

    /**
     * Firebase-এ প্রথমবার sample data যোগ করার জন্য
     */
    public void insertSampleData() {
        // Products
        String[][] products = {
            {"p001", "আলু (দেশি)", "সবজি", "কেজি", "🥔"},
            {"p002", "পেঁয়াজ (দেশি)", "সবজি", "কেজি", "🧅"},
            {"p003", "রসুন (দেশি)", "মশলা", "কেজি", "🧄"},
            {"p004", "মিনিকেট চাল", "চাল-ডাল", "কেজি", "🌾"},
            {"p005", "নাজিরশাইল চাল", "চাল-ডাল", "কেজি", "🌾"},
            {"p006", "মসুর ডাল", "চাল-ডাল", "কেজি", "🫘"},
            {"p007", "সয়াবিন তেল", "তেল-মশলা", "লিটার", "🛢️"},
            {"p008", "রুই মাছ", "মাছ", "কেজি", "🐟"},
            {"p009", "ইলিশ মাছ", "মাছ", "কেজি", "🐟"},
            {"p010", "ব্রয়লার মুরগি", "মাংস", "কেজি", "🍗"},
            {"p011", "গরুর মাংস", "মাংস", "কেজি", "🥩"},
            {"p012", "ডিম (ফার্ম)", "ডিম", "হালি", "🥚"},
            {"p013", "টমেটো", "সবজি", "কেজি", "🍅"},
            {"p014", "বেগুন", "সবজি", "কেজি", "🍆"},
            {"p015", "পটল", "সবজি", "কেজি", "🥒"}
        };

        for (String[] p : products) {
            Product product = new Product(p[0], p[1], p[2], p[3], p[4]);
            mDatabase.child(NODE_PRODUCTS).child(p[0]).setValue(product)
                    .addOnFailureListener(e -> Log.e(TAG, "Sample product write failed: " + p[0], e));
        }

        // Markets
        String[][] markets = {
            {"m001", "কারওয়ান বাজার", "তেজগাঁও, ঢাকা", "23.7537", "90.3923"},
            {"m002", "শান্তিনগর বাজার", "শান্তিনগর, ঢাকা", "23.7397", "90.4200"},
            {"m003", "মোহাম্মদপুর কৃষি মার্কেট", "মোহাম্মদপুর, ঢাকা", "23.7627", "90.3572"},
            {"m004", "মিরপুর ১০ বাজার", "মিরপুর, ঢাকা", "23.8103", "90.3661"},
            {"m005", "রামপুরা বাজার", "রামপুরা, ঢাকা", "23.7558", "90.4338"}
        };

        for (String[] m : markets) {
            Market market = new Market(m[0], m[1], m[2],
                    Double.parseDouble(m[3]), Double.parseDouble(m[4]));
            mDatabase.child(NODE_MARKETS).child(m[0]).setValue(market)
                    .addOnFailureListener(e -> Log.e(TAG, "Sample market write failed: " + m[0], e));
        }

        // Sample Prices for today
        String today = DateUtils.getTodayDate();
        insertSamplePricesForDate(today);
    }

    private void insertSamplePricesForDate(String date) {
        // productId, marketId, minPrice, maxPrice
        double[][] priceData = {
            // আলু - ৫ বাজার
            // কারওয়ান, শান্তিনগর, মোহাম্মদপুর, মিরপুর, রামপুরা
        };

        // কারওয়ান বাজার - আজকের দাম
        addSamplePrice(date, "p001", "m001", "আলু (দেশি)", "কারওয়ান বাজার", "সবজি", "কেজি", "🥔", 30, 40);
        addSamplePrice(date, "p002", "m001", "পেঁয়াজ (দেশি)", "কারওয়ান বাজার", "সবজি", "কেজি", "🧅", 60, 75);
        addSamplePrice(date, "p004", "m001", "মিনিকেট চাল", "কারওয়ান বাজার", "চাল-ডাল", "কেজি", "🌾", 68, 78);
        addSamplePrice(date, "p006", "m001", "মসুর ডাল", "কারওয়ান বাজার", "চাল-ডাল", "কেজি", "🫘", 95, 115);
        addSamplePrice(date, "p007", "m001", "সয়াবিন তেল", "কারওয়ান বাজার", "তেল-মশলা", "লিটার", "🛢️", 165, 185);
        addSamplePrice(date, "p008", "m001", "রুই মাছ", "কারওয়ান বাজার", "মাছ", "কেজি", "🐟", 280, 360);
        addSamplePrice(date, "p010", "m001", "ব্রয়লার মুরগি", "কারওয়ান বাজার", "মাংস", "কেজি", "🍗", 180, 210);
        addSamplePrice(date, "p011", "m001", "গরুর মাংস", "কারওয়ান বাজার", "মাংস", "কেজি", "🥩", 700, 780);
        addSamplePrice(date, "p012", "m001", "ডিম (ফার্ম)", "কারওয়ান বাজার", "ডিম", "হালি", "🥚", 42, 48);
        addSamplePrice(date, "p013", "m001", "টমেটো", "কারওয়ান বাজার", "সবজি", "কেজি", "🍅", 40, 60);

        // শান্তিনগর বাজার
        addSamplePrice(date, "p001", "m002", "আলু (দেশি)", "শান্তিনগর বাজার", "সবজি", "কেজি", "🥔", 32, 42);
        addSamplePrice(date, "p002", "m002", "পেঁয়াজ (দেশি)", "শান্তিনগর বাজার", "সবজি", "কেজি", "🧅", 62, 78);
        addSamplePrice(date, "p007", "m002", "সয়াবিন তেল", "শান্তিনগর বাজার", "তেল-মশলা", "লিটার", "🛢️", 168, 188);
        addSamplePrice(date, "p010", "m002", "ব্রয়লার মুরগি", "শান্তিনগর বাজার", "মাংস", "কেজি", "🍗", 185, 215);
    }

    private void addSamplePrice(String date, String productId, String marketId,
                                 String productName, String marketName, String category,
                                 String unit, String emoji, double min, double max) {
        Price price = new Price(productId, marketId, min, max, date);
        price.setProductName(productName);
        price.setMarketName(marketName);
        price.setCategory(category);
        price.setUnit(unit);
        price.setProductEmoji(emoji);

        String key = productId + "_" + marketId;
        mDatabase.child(NODE_PRICES).child(date).child(key).setValue(price)
                .addOnFailureListener(e -> Log.e(TAG, "Sample price write failed: " + key, e));
    }

    private static String logAndDescribe(String operation, DatabaseError error) {
        Log.e(TAG, operation + " was cancelled: " + error.getMessage(), error.toException());
        return error.getMessage() != null ? error.getMessage() : error.getDetails();
    }

    private static String describe(Exception e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }

    // ==================== CALLBACKS ====================

    public interface ProductCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public interface PriceCallback {
        void onSuccess(List<Price> prices);
        void onError(String error);
    }

    public interface PriceHistoryCallback {
        void onSuccess(List<Price> history);
        void onError(String error);
    }

    public interface MarketCallback {
        void onSuccess(List<Market> markets);
        void onError(String error);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}
