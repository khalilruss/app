package com.kierigby.bountyhunter;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bountyhunterapi.BountyHunterAPI;
import com.example.bountyhunterapi.Friend;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CreateGameActivity extends AppCompatActivity {
    private final List<Friend> friendsList = Collections.synchronizedList(new ArrayList<Friend>());
    private BountyHunterAPI api;
    private InviteFriendRecyclerViewAdapter mInviteFriendRecyclerViewAdapter;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Socket mSocket;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((GlobalUser) this.getApplication()).tokenCheck();
        setContentView(R.layout.activity_create_game);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        api = new BountyHunterAPI(this);
        addListenerToBackButton();
        addListenerToNexbButton();
        setUpRecyclerView();
        getFriends();
    }

    private void setUpRecyclerView() {

        RecyclerView mFriendRecyclerView = findViewById(R.id.inviteFriendsRecyclerView);
        mFriendRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mInviteFriendRecyclerViewAdapter = new InviteFriendRecyclerViewAdapter(friendsList);
        mFriendRecyclerView.setAdapter(mInviteFriendRecyclerViewAdapter);

    }

    public void getFriends() {

        api.getFriendsFollowing(((GlobalUser) this.getApplication()).getLoggedInUser().getId(), new BountyHunterAPI.FoundFriendsCallBack() {
            @Override
            public void onFriendsFound(List<Friend> friends) {
                synchronized (friendsList) {
                    friends.removeAll(friendsList);
                    friendsList.addAll(friends);
                    mInviteFriendRecyclerViewAdapter.notifyDataSetChanged();
                }
            }
        });

        api.getFriendsFollowers(((GlobalUser) this.getApplication()).getLoggedInUser().getId(), new BountyHunterAPI.FoundFriendsCallBack() {
            @Override
            public void onFriendsFound(List<Friend> friends) {
                synchronized (friendsList) {
                    friends.removeAll(friendsList);
                    friendsList.addAll(friends);
                    mInviteFriendRecyclerViewAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void addListenerToBackButton() {
        ImageButton mBackButton = findViewById(R.id.backFromCreateGame);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(CreateGameActivity.this);
            }
        });
    }

    public void addListenerToNexbButton() {
        Button mNextButton = findViewById(R.id.btCreateGameNext);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInviteFriendRecyclerViewAdapter.inviteFriends() != null) {
                    ArrayList<UUID> friendsToInvite = mInviteFriendRecyclerViewAdapter.inviteFriends();
                    if (friendsToInvite.size() != 0) {
                        TakePictureIntent();
                    } else {
                        Toast.makeText(CreateGameActivity.this, "Please choose some friends to invite", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
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
                createGameInfo.put("players", mInviteFriendRecyclerViewAdapter.inviteFriends());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("createGame", createGameInfo);
            Intent lobbyI = new Intent(CreateGameActivity.this,LobbyActivity.class);
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
