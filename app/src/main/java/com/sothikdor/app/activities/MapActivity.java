package com.sothikdor.app.activities;

import com.sothikdor.R;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
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

public class MapActivity extends BaseActivity {

    private MapView mapView;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid config
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map);

        setupToolbar("বাজারের মানচিত্র");

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
                addDefaultMarkets();
            }
        });
    }

    private void addMarker(Market market) {
        addMarker(market.getLatitude(), market.getLongitude(), market.getName(), market.getArea());
        mapView.invalidate();
    }

    private void addMarker(double latitude, double longitude, String title, String snippet) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(latitude, longitude));
        marker.setTitle(title);
        marker.setSnippet(snippet);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
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
            addMarker(markets[i][0], markets[i][1], names[i], "ঢাকা");
        }
        mapView.invalidate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            addMyLocation();
        }
    }

    @Override
    protected void onResume() { super.onResume(); mapView.onResume(); }

    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }
}
