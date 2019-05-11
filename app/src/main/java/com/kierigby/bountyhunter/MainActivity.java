package com.kierigby.bountyhunter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bountyhunterapi.BountyHunterAPI;
import com.example.bountyhunterapi.Friend;
import com.example.bountyhunterapi.User;
import com.google.firebase.messaging.RemoteMessage;
import com.pusher.pushnotifications.BeamsCallback;
import com.pusher.pushnotifications.PushNotifications;
import com.pusher.pushnotifications.PusherCallbackError;
import com.pusher.pushnotifications.auth.AuthData;
import com.pusher.pushnotifications.auth.AuthDataGetter;
import com.pusher.pushnotifications.auth.BeamsTokenProvider;
import com.pusher.pushnotifications.fcm.MessagingService;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText mPasswordInput;
    private EditText mUsernameInput;
    private BountyHunterAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = new BountyHunterAPI(this);
        addListenerToLoginButton();
        addListenerToRegisterButton();
        addListenerToForgotPasswordTextView();

    }

    public void addListenerToLoginButton() {
        Button mLoginButton = findViewById(R.id.loginBtn);
        mUsernameInput = findViewById(R.id.loginUsernameEditText);
        mPasswordInput = findViewById(R.id.loginPasswordEditText);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsernameInput.getText().toString().isEmpty() || mPasswordInput.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter both your username and password", Toast.LENGTH_LONG).show();
                } else {
                    String username = mUsernameInput.getText().toString();
                    String password = mPasswordInput.getText().toString();
                    api.loginUser(username, password, new BountyHunterAPI.FoundUserCallBack() {
                        @Override
                        public void onUserReturned(User user) {
                            ((GlobalUser) getApplication()).setLoggedInUser(user);
                            setUpPusher(user.getId().toString());
                            Intent loggedInI = new Intent(MainActivity.this, LoggedInActivity.class);
                            startActivity(loggedInI);
                        }
                    });
                }

            }
        });
    }

    public void addListenerToRegisterButton() {
        Button mRegisterButton = findViewById(R.id.registerBtn);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerI = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(registerI);
            }
        });
    }

    public void addListenerToForgotPasswordTextView() {
        TextView mForgotPasswordTextView = findViewById(R.id.forgotPasswordtextView);

        mForgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgotPasswordI = new Intent(MainActivity.this, ForgottenPassActivity.class);
                startActivity(forgotPasswordI);
            }
        });
    }

    public void setUpPusher(String userID){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String sessionToken = preferences.getString("TOKEN", null);
//        Toast.makeText(getApplicationContext(),sessionToken.toString(),Toast.LENGTH_LONG).show();
        BeamsTokenProvider tokenProvider = new BeamsTokenProvider(
                "https://api.bountyhunt.me/users/pusher/beams-auth",
                new AuthDataGetter() {
                    @Override
                    public AuthData getAuthData() {
                        // Headers and URL query params your auth endpoint needs to
                        // request a Beams Token for a given user
                        HashMap<String, String> headers = new HashMap<>();
                        // for example:
                        headers.put("Authorization", sessionToken);
                        HashMap<String, String> queryParams = new HashMap<>();
                        return new AuthData(
                                headers,
                                queryParams
                        );
                    }
                }
        );
        PushNotifications.start(getApplicationContext(), "8ad9b796-b0fe-4936-83bd-cde6d460f800");
        PushNotifications.setUserId(userID, tokenProvider, new BeamsCallback<Void, PusherCallbackError>(){
            @Override
            public void onSuccess(Void... values) {
                Log.i("PusherBeams", "Successfully authenticated with Pusher Beams");
            }

            @Override
            public void onFailure(PusherCallbackError error) {
                Log.i("PusherBeams", "Pusher Beams authentication failed: " + error.getMessage());
            }
        });

    }

}