package com.kierigby.bountyhunter;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;

public class NotificationsActivity extends AppCompatActivity {
    private NotificationRecyclerViewAdapter mNotificationsRecyclerViewAdapter;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Socket mSocket;
    private View.OnClickListener mItemClickListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        mItemClickListener = createClickListener();

    }

    private View.OnClickListener createClickListener() {
       return new View.OnClickListener() {
           @Override
           public void onClick(View v) {
              TakePictureIntent();
           }
       };
    }

    private void setUpRecyclerView() {
        RecyclerView mNotificationRecyclerView = findViewById(R.id.notifications_recycler_view);
        mNotificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mNotificationsRecyclerViewAdapter = new NotificationRecyclerViewAdapter(((GlobalUser) this.getApplication()).getNotifications(),preferences);
        mNotificationRecyclerView.setAdapter(mNotificationsRecyclerViewAdapter);
        mNotificationsRecyclerViewAdapter.setOnItemClickListener(mItemClickListener);
    }

    private void TakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            String imageToSend = encodeImage(imageBitmap);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String token = preferences.getString("TOKEN", null).substring(7);

            try {
                IO.Options opts = new IO.Options();
                opts.query = "token=" + token;
                mSocket = IO.socket("https://gs.bountyhunt.me", opts);
            } catch (URISyntaxException ignored) {}

            mSocket.connect();
            JSONObject createGameInfo = new JSONObject();
            try {
                createGameInfo.put("photo", imageToSend);
//                createGameInfo.put("id", mInviteFriendRecyclerViewAdapter.inviteFriends());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("joinGame", createGameInfo);
            Intent lobbyI = new Intent(NotificationsActivity.this,LobbyActivity.class);
            startActivity(lobbyI);
        }
    }

    public String encodeImage(Bitmap image) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);


    }
}
