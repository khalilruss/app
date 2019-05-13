package com.kierigby.bountyhunter;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.example.bountyhunterapi.Game;
import com.example.bountyhunterapi.Player;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class FugitiveActivity extends AppCompatActivity implements OnMapReadyCallback {

    private DrawerLayout drawer;
    private Socket mSocket;
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
    private ArrayList<Marker> challenge = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fugitive2);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fMap);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest = new LocationRequest().setInterval(10000).setFastestInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));
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
                        resolvable.startResolutionForResult(FugitiveActivity.this,
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
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String token = preferences.getString("TOKEN", null).substring(7);
                    try {
                        IO.Options opts = new IO.Options();
                        opts.query = "token=" + token;
                        mSocket = IO.socket("https://gs.bountyhunt.me", opts);
                    } catch (URISyntaxException ignored) {
                    }

                    mSocket.connect();
                    JSONObject locationInfo = new JSONObject();

                    try {
                        locationInfo.put("lat", location.getLatitude());
                        locationInfo.put("long", location.getLongitude());
                        locationInfo.put("alt", location.getAltitude());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("locationUpdate", locationInfo);

                    Emitter.Listener onGameUpdate = new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Game currentGame = null;
                            LatLng centre = null;
                            try {
                                final JSONObject gameJSON = (JSONObject) ((JSONObject) args[0]).get("game");
                                Gson gson = new Gson();
                                final JSONObject locationJSON = (JSONObject) gameJSON.get("center");
                                currentGame = gson.fromJson(String.valueOf(gameJSON), Game.class);
                                centre = new LatLng(((Double) locationJSON.get("lat")), (Double) locationJSON.get("long"));

                                for(Player player:currentGame.getJoined()){
                                    if(player.getType().equals("Fugitive")){
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        handler.post(new Runnable() {

                                            @Override
                                            public void run() {
                                                LatLng fugitiveLocation = null;
                                                try {
                                                    fugitiveLocation = new LatLng(((Double) locationJSON.get("lat")), (Double) locationJSON.get("long"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                addChallenge(fugitiveLocation);
                                            }
                                        });

                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            Handler handler = new Handler(Looper.getMainLooper());
                            final Game finalCurrentGame = currentGame;
                            final LatLng finalCentre = centre;
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    if (finalCurrentGame != null) {
                                        try {
                                            drawCircle(finalCentre, finalCurrentGame.getRadius());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                    }
                                }
                            });
                        }
                    };
                    mSocket.on("gameUpdate", onGameUpdate);
                }
            }


        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


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
//        LatLng userLocation = new LatLng(playerLoc.getLatitude(),playerLoc.getLongitude());
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
//        try {
//            drawCircle(userLocation,50);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    public void drawCircle(LatLng centre, double radius) throws IOException {

        for (Circle circle : circle) {
            circle.remove();
        }
        circle.clear();

        CircleOptions circleOptions = new CircleOptions()
                .center(centre)
                .radius(radius)
                .fillColor(0x220000DD);

        if (mMap != null) {
            Circle myCircle = mMap.addCircle(circleOptions);
            circle.add(myCircle);
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

    public void addChallenge(LatLng challengeLocation) {
        for (Marker challenge : challenge) {
            challenge.remove();
        }
        challenge.clear();
        if (mMap != null) {
            Marker newChallenge = mMap.addMarker(new MarkerOptions().position(challengeLocation).title("New Challenge").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
            challenge.add(newChallenge);
        }

    }


}