package com.kierigby.bountyhunter;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;

public class BountyHunterActivity extends AppCompatActivity implements OnMapReadyCallback {

    private DrawerLayout drawer;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean requestingLocationUpdates = true;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private Location playerLoc;
    private int locationRequestCode = 1000;
    private ArrayList<Circle> circle = new ArrayList<>();
    private ArrayList<Polyline> arrows = new ArrayList<>();
    double mag1 = 0.07;
    double mag2 = 0.01;
    double degree1 = 90;
    double degree2 = 180;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounty_hunter);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.bMap);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest = new LocationRequest().setInterval(1000).setFastestInterval(500).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {


            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(BountyHunterActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
        makeDraw();

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLocation();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        }

    }

    public void updateLocation() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    playerLoc = location;
                    for (Polyline arrow:arrows){
                        arrow.remove();
                    }
                    arrows.clear();
                    LatLng userLocation= new LatLng(location.getLatitude(),location.getLongitude());

                    addArrows(userLocation, 0.4, 90, Color.BLUE);
                    addArrows(userLocation, 0.8, 180, Color.RED);                }
            }


        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.i("In map", "it works");
        mMap = googleMap;
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            playerLoc = locationManager.getLastKnownLocation(locationManager
                    .getBestProvider(criteria, false));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        }
        LatLng userLocation = new LatLng(playerLoc.getLatitude(), playerLoc.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        try {
            drawCircle(userLocation, 500);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mag1 += 0.3;
        mag2 -= 0.3;
        degree1 += 10;
        degree2 -= 10;
        if(mag1<1&& mag2<1&& mag1 > 0 && mag2 > 0 && degree1 < 360 && degree1 > 0 && degree2 < 360 && degree2 > 0) {
            addArrows(userLocation, mag1, degree1, Color.BLUE);
            addArrows(userLocation, mag2, degree2, Color.RED);
        }

    }

    public void makeDraw() {
        drawer = findViewById(R.id.drawer);

        findViewById(R.id.ivHam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(Gravity.END)) {
                    drawer.closeDrawer(Gravity.END);
                } else {
                    drawer.openDrawer(Gravity.END);
                }
            }
        });

        findViewById(R.id.ivCross).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(Gravity.END)) {
                    drawer.closeDrawer(Gravity.END);
                }
            }
        });

    }

    public void drawCircle(LatLng centre, int radius) throws IOException {

        for (Circle circle : circle) {
            circle.remove();
        }
        circle.clear();

        CircleOptions circleOptions = new CircleOptions()
                .center(centre)
                .radius(radius)
                .fillColor(0x220000DD);
        Circle myCircle = mMap.addCircle(circleOptions);
        circle.add(myCircle);
    }

    public void addArrows(LatLng userLocation, double mag, double degree, int col) {

        Cap cap = new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.arrowhead), 60);

        double Lat = userLocation.latitude + (0.005 * mag * Math.cos(degree));
        double Lng = userLocation.longitude + (0.005 * mag * Math.sin(degree));

        Polyline newPolyline = mMap.addPolyline(
                new PolylineOptions()
                        .add(userLocation, new LatLng(Lat, Lng))
                        .color(col)
                        .width(2f)
                        .endCap(cap)
        );

        arrows.add(newPolyline);
    }
}
