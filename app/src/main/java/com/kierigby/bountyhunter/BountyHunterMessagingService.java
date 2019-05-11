package com.kierigby.bountyhunter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;
import com.pusher.pushnotifications.fcm.MessagingService;

public class BountyHunterMessagingService extends MessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i("NotificationsService", "Got a remote message: 🎉"+ remoteMessage.getNotification().getBody());
//        Toast.makeText(getApplicationContext(), remoteMessage.getMessageId(), Toast.LENGTH_LONG).show();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "You have a new notification", Toast.LENGTH_LONG).show();
            }
        });
    }
}