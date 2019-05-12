package com.kierigby.bountyhunter;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.bountyhunterapi.BountyHunterAPI;
import com.example.bountyhunterapi.Game;
import com.example.bountyhunterapi.User;
import com.google.firebase.messaging.RemoteMessage;
import com.pusher.pushnotifications.PushNotificationReceivedListener;
import com.pusher.pushnotifications.PushNotifications;

import java.util.ArrayList;
import java.util.HashMap;

public class GlobalUser extends Application {
    User loggedInUser;
    ArrayList<Game> games= new ArrayList();
    BountyHunterAPI api;

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public User getLoggedInUser() {
        return this.loggedInUser;
    }

    public void logoutUser() {
        this.loggedInUser = null;
        api = new BountyHunterAPI(this);
        api.clearToken();
        Intent logoutI = new Intent(getApplicationContext(), MainActivity.class);
        logoutI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logoutI);
    }

    public void tokenCheck() {
        api = new BountyHunterAPI(this);
        api.getUser(loggedInUser.getId(), new BountyHunterAPI.TokenCheckCallBack() {
            @Override
            public void tokenCheck(int code) {
                if (code != 200) {
                    logoutUser();
                    Toast.makeText(getApplicationContext(), "Your session has expired please login again", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public void addGame(Game newGame){
        games.add(newGame);
    }

    public void removeGame(Game declinedGame){
        games.remove(declinedGame);
    }
    public ArrayList<Game> getGames(){
        return games;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Bounty_Hunter", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
}
