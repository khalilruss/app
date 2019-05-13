package com.kierigby.bountyhunter;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.bountyhunterapi.Game;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.pusher.pushnotifications.fcm.MessagingService;

import org.json.JSONException;
import org.json.JSONObject;

public class BountyHunterMessagingService extends MessagingService {
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Log.i("mesReceived","We here bro");
        if(remoteMessage.getData().get("type").equals("GAME_INVITE")){
            Game newGame = null;
            try {
                JSONObject gameJSON= new JSONObject(remoteMessage.getData().get("game"));
                Gson gson=new Gson();
                newGame = gson.fromJson(String.valueOf(gameJSON),Game.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(newGame!=null){
                ((GlobalUser) getApplication()).addGame(newGame);
            }
        }


        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                if(remoteMessage.getData().get("type").equals("GAME_INVITE")){
                    showInviteNotification(remoteMessage);
                }else{
                    showReadyNotification(remoteMessage);
                }

                Toast.makeText(getApplicationContext(), "You have a new notification", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showInviteNotification(RemoteMessage remoteMessage){

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Bounty_Hunter")
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId=1;

        notificationManager.notify(notificationId, builder.build());
    }

    public void showReadyNotification(RemoteMessage remoteMessage){

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Bounty_Hunter")
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId=2;

        notificationManager.notify(notificationId, builder.build());
    }
}