package com.kierigby.bountyhunter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bountyhunterapi.BountyHunterAPI;
import com.example.bountyhunterapi.Friend;
import com.example.bountyhunterapi.Game;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.internal.Objects;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.MyViewHolder> {

    private static List<String> mGameList;
    private Socket mSocket;
    private Context context;
    private SharedPreferences preferences;
    private Application application;
    private String acceptID;
    private static final int REQUEST_IMAGE_CAPTURE = 1;


    NotificationRecyclerViewAdapter(List<String> games, SharedPreferences prefs, Context con, Application app) {
        mGameList = games;
        application = app;
        context = con;
        preferences = prefs;
        String token = preferences.getString("TOKEN", null).substring(7);
        try {
            IO.Options opts = new IO.Options();
            opts.query = "token=" + token;
            mSocket = IO.socket("https://gs.bountyhunt.me", opts);
        } catch (URISyntaxException ignored) {
        }
        mSocket.connect();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageButton acceptBtn, declineBtn;

        /**
         * Sets up the view for each data item,
         *
         * @param itemView the view that will be used by the item
         */
        MyViewHolder(final View itemView) {
            super(itemView);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            declineBtn = itemView.findViewById(R.id.declineBtn);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
        }
    }

    @NonNull
    @Override
    public NotificationRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.notification_item, viewGroup, false);
        return new NotificationRecyclerViewAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationRecyclerViewAdapter.MyViewHolder myViewHolder, final int i) {
        final String gameID = mGameList.get(i);
        myViewHolder.declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject declineGameInfo = new JSONObject();
                try {
                    declineGameInfo.put("id", gameID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("declineGame", declineGameInfo);

                Emitter.Listener onGameDecline = new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        ((GlobalUser) application).removeGame(((GlobalUser) application).getGames().get(myViewHolder.getAdapterPosition()));
                        mGameList.remove(myViewHolder.getAdapterPosition());
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                };
                mSocket.on("gameDeclined", onGameDecline);

            }
        });

        myViewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                    acceptID = gameID;
                    ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mGameList.size();
    }

    void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                String imageToSend = encodeImage(imageBitmap);
                String token = preferences.getString("TOKEN", null).substring(7);

                try {
                    IO.Options opts = new IO.Options();
                    opts.query = "token=" + token;
                    mSocket = IO.socket("https://gs.bountyhunt.me", opts);
                } catch (URISyntaxException ignored) {
                }

                mSocket.connect();
                JSONObject createGameInfo = new JSONObject();
                try {
                    createGameInfo.put("photo", imageToSend);
                    createGameInfo.put("id", acceptID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("joinGame", createGameInfo);

            Emitter.Listener onGameJoined = new Emitter.Listener() {
                @Override
                public void call(Object... args) {
//                    for(Iterator<Game> iterator =((GlobalUser) application).getGames().iterator(); iterator.hasNext(); ) {
//                        if(String.valueOf(iterator.next().getId()).equals(acceptID))
//                            ((GlobalUser) application).removeGame(iterator.next());
//                    }
//                    for(Iterator<String> iterator =mGameList.iterator(); iterator.hasNext(); ) {
//                        if(iterator.next().equals(acceptID))
//                            iterator.remove();
//                    }
//
//                    Handler handler = new Handler(Looper.getMainLooper());
//                    handler.post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            notifyDataSetChanged();
//                        }
//                    });
                }
            };
            mSocket.on("gameJoined", onGameJoined);
                Intent lobbyI = new Intent(context, LoggedInActivity.class);
                context.startActivity(lobbyI);
            Toast.makeText(context, "You will receive a notification when the game is ready to start", Toast.LENGTH_SHORT).show();
        }
    }

    private String encodeImage(Bitmap image) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);


    }
}
