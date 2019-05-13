package com.kierigby.bountyhunter;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Iterator;

public class LobbyActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private int locationRequestCode = 1000;
    private boolean requestingLocationUpdates = true;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Socket mSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest = new LocationRequest().setInterval(500).setFastestInterval(250).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));
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
                        resolvable.startResolutionForResult(LobbyActivity.this,
                                REQUEST_CHECK_SETTINGS);
                        Log.i("interval", "Didn't work");
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
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
                    mSocket.emit("joinLobby", locationInfo);


                    Emitter.Listener onJoinedLobby = new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Game newGame = null;
                            try {
                                JSONObject gameJSON = (JSONObject) ((JSONObject) args[0]).get("game");
                                Gson gson = new Gson();
                                newGame = gson.fromJson(String.valueOf(gameJSON), Game.class);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mSocket.on("joinedLobby", onJoinedLobby);


                    Emitter.Listener onGameStart = new Emitter.Listener() {
                        @Override
                        public void call(final Object... args) {
                            Log.i("startGame", "we here2");
                            Game newGame = null;
                            try {
                                JSONObject gameJSON = (JSONObject) ((JSONObject) args[0]).get("game");
                                Gson gson = new Gson();
                                newGame = gson.fromJson(String.valueOf(gameJSON), Game.class);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (newGame != null) {
                                for (Player player : newGame.getJoined()) {
                                    if (player.getUser().getId().equals(((GlobalUser) getApplication()).getLoggedInUser().getId())) {
                                        if (player.getType().equals("Bounty Hunter")) {
                                            Intent startI = new Intent(getApplicationContext(), BountyHunterActivity.class);
                                            startActivity(startI);
                                        } else {
                                            Intent startI = new Intent(getApplicationContext(), FugitiveActivity.class);
                                            startActivity(startI);
                                        }
                                    }
                                }
                            }
                        }
                    };
                    mSocket.on("gameStarted", onGameStart);
                }
            }


        };
    }
}