package com.sothikdor.app.activities;

import com.sothikdor.R;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothikdor.app.models.Market;
import com.sothikdor.app.utils.FirebaseHelper;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (getSupportActionBar() != null) {
            if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("নিকটস্থ বাজার");
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Location permission চাওয়া
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }

        // Firebase থেকে বাজারের তালিকা লোড করে Map-এ দেখানো
        loadMarketsOnMap();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng myPos = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos, 13f));
                        }
                    });
        }
    }

    private void loadMarketsOnMap() {
        FirebaseHelper.getInstance().getAllMarkets(new FirebaseHelper.MarketCallback() {
            @Override
            public void onSuccess(List<Market> markets) {
                for (Market market : markets) {
                    LatLng position = new LatLng(market.getLatitude(), market.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(position)
                            .title(market.getName())
                            .snippet(market.getArea())
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_GREEN));
                    mMap.addMarker(markerOptions);
                }

                // ঢাকার কেন্দ্রে জুম করা (যদি location না পাওয়া যায়)
                if (!markets.isEmpty()) {
                    LatLng dhaka = new LatLng(23.7637, 90.3900);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dhaka, 12f));
                }
            }

            @Override
            public void onError(String error) {
                // Demo markers যোগ করা
                addDemoMarkers();
            }
        });

        // Map মার্কার ক্লিক করলে দূরত্ব দেখানো
        mMap.setOnMarkerClickListener(marker -> {
            Toast.makeText(MapActivity.this,
                    marker.getTitle() + "\n" + marker.getSnippet(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    private void addDemoMarkers() {
        double[][] markets = {
            {23.7537, 90.3923, 0}, // কারওয়ান বাজার
            {23.7397, 90.4200, 0}, // শান্তিনগর
            {23.7627, 90.3572, 0}, // মোহাম্মদপুর
            {23.8103, 90.3661, 0}, // মিরপুর
            {23.7558, 90.4338, 0}  // রামপুরা
        };
        String[] names = {"কারওয়ান বাজার", "শান্তিনগর বাজার", "মোহাম্মদপুর কৃষি মার্কেট", "মিরপুর ১০ বাজার", "রামপুরা বাজার"};
        String[] areas = {"তেজগাঁও, ঢাকা", "শান্তিনগর, ঢাকা", "মোহাম্মদপুর, ঢাকা", "মিরপুর, ঢাকা", "রামপুরা, ঢাকা"};

        for (int i = 0; i < names.length; i++) {
            LatLng pos = new LatLng(markets[i][0], markets[i][1]);
            mMap.addMarker(new MarkerOptions().position(pos).title(names[i]).snippet(areas[i])
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.7637, 90.3900), 12f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "লোকেশন পারমিশন দিলে কাছের বাজার দেখা যাবে", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
