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
import com.example.bountyhunterapi.User;
import com.google.firebase.messaging.RemoteMessage;
import com.pusher.pushnotifications.PushNotificationReceivedListener;
import com.pusher.pushnotifications.PushNotifications;

import java.util.ArrayList;
import java.util.HashMap;

public class GlobalUser extends Application {
    User loggedInUser;
    ArrayList<String> notifications= new ArrayList();
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

    public void addNotifications(String newNotification){
        notifications.add(newNotification);
    }

    public ArrayList<String> getNotifications(){
        return notifications;
    }


}
