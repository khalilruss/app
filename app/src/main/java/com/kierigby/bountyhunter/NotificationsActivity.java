package com.kierigby.bountyhunter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.bountyhunterapi.Game;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {
    private NotificationRecyclerViewAdapter mNotificationsRecyclerViewAdapter;
    private RecyclerView mNotificationsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        mNotificationsRecyclerView = findViewById(R.id.notifications_recycler_view);
        mNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ArrayList<String> gameIds= new ArrayList<>();
        for(Game game: ((GlobalUser) this.getApplication()).getGames()){
            gameIds.add(String.valueOf(game.getId()));
        }

        mNotificationsRecyclerViewAdapter= new NotificationRecyclerViewAdapter(gameIds,preferences,this,this.getApplication());
        mNotificationsRecyclerView.setAdapter(mNotificationsRecyclerViewAdapter);
        mNotificationsRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mNotificationsRecyclerViewAdapter.onActivityResult(requestCode, resultCode, data);
    }
}
