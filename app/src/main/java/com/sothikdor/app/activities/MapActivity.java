package com.sothikdor.app.activities;

import com.sothikdor.R;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import com.sothikdor.app.models.Market;
import com.sothikdor.app.utils.FirebaseHelper;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid config
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("বাজারের মানচিত্র");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Default zoom to Dhaka
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(new GeoPoint(23.8103, 90.4125));

        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            addMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
        }

        loadMarkets();
    }

    private void addMyLocation() {
        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(
            new GpsMyLocationProvider(this), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);
    }

    private void loadMarkets() {
        FirebaseHelper.getInstance().getAllMarkets(new FirebaseHelper.MarketCallback() {
            @Override
            public void onSuccess(List<Market> markets) {
                for (Market market : markets) {
                    addMarker(market);
                }
            }
            @Override
            public void onError(String error) {
                // fallback: add default Dhaka markets
                Log.e(TAG, "Market list load failed: " + error);
                Toast.makeText(MapActivity.this,
                        "বাজারের তালিকা লোড হয়নি, ডিফল্ট বাজার দেখানো হচ্ছে", Toast.LENGTH_SHORT).show();
                addDefaultMarkets();
            }
        });
    }

    private void addMarker(Market market) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(market.getLatitude(), market.getLongitude()));
        marker.setTitle(market.getName());
        marker.setSnippet(market.getArea());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    private void addDefaultMarkets() {
        double[][] markets = {
            {23.7515, 90.3930, 0}, // কারওয়ান বাজার
            {23.7334, 90.4190, 1}, // শান্তিনগর
            {23.7647, 90.3572, 2}, // মোহাম্মদপুর
            {23.8223, 90.3654, 3}, // মিরপুর ১০
            {23.7549, 90.4299, 4}, // রামপুরা
            {23.7098, 90.4320, 5}, // যাত্রাবাড়ী
        };
        String[] names = {
            "কারওয়ান বাজার","শান্তিনগর বাজার",
            "মোহাম্মদপুর বাজার","মিরপুর ১০ বাজার",
            "রামপুরা বাজার","যাত্রাবাড়ী বাজার"
        };
        for (int i = 0; i < markets.length; i++) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(markets[i][0], markets[i][1]));
            marker.setTitle(names[i]);
            marker.setSnippet("ঢাকা");
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode != LOCATION_PERMISSION_REQUEST) return;
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            addMyLocation();
        } else {
            Log.w(TAG, "Location permission denied; my-location overlay disabled");
            Toast.makeText(this,
                "লোকেশন অনুমতি না দেওয়ায় আপনার অবস্থান দেখানো যাবে না", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() { super.onResume(); mapView.onResume(); }

    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
