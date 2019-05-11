package com.kierigby.bountyhunter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.bountyhunterapi.BountyHunterAPI;
import com.example.bountyhunterapi.Friend;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.messaging.RemoteMessage;

import java.net.URISyntaxException;
import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.MyViewHolder> {

    private static List<String> mNotificationList;
    private View.OnClickListener mOnItemClickListener;
    private Socket mSocket;


    public NotificationRecyclerViewAdapter(List<String> notifications, SharedPreferences preferences) {
        mNotificationList = notifications;
        String token = preferences.getString("TOKEN", null).substring(7);
        try {
            IO.Options opts = new IO.Options();
            opts.query = "token=" + token;
            mSocket = IO.socket("https://gs.bountyhunt.me", opts);
        } catch (URISyntaxException ignored) {
        }
        mSocket.connect();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button acceptBtn, declineBtn;

        /**
         * Sets up the view for each data item,
         *
         * @param itemView the view that will be used by the item
         */
        public MyViewHolder(final View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.gameName);
            String joinString = "Do you want to Join this game?";
            textView.setText(joinString);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            declineBtn = itemView.findViewById(R.id.declineBtn);
            itemView.setTag(this);
            acceptBtn.setOnClickListener(mOnItemClickListener);
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
    public void onBindViewHolder(@NonNull final NotificationRecyclerViewAdapter.MyViewHolder myViewHolder, int i) {
        final String gameID = mNotificationList.get(i);

        myViewHolder.declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.emit("declineGame", gameID);
            }
        });

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }
}
