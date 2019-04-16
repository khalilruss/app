package com.kierigby.bountyhunter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bountyhunterapi.BountyHunterAPI;
import com.example.bountyhunterapi.Friend;
import com.example.bountyhunterapi.User;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateGameActivity extends AppCompatActivity {
    private final List<Friend> friendsList = Collections.synchronizedList(new ArrayList<Friend>());
    private BountyHunterAPI api;
    private InviteFriendRecyclerViewAdapter mInviteFriendRecyclerViewAdapter;
    private static final int REQUEST_IMAGE_CAPTURE = 1;


    private Socket mSocket;

    {
        try {
//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//            String token = preferences.getString("TOKEN", null).substring(6);
            IO.Options opts = new IO.Options();
//            opts.query = "token=" + token;
            mSocket = IO.socket("http://gs.bountyhunt.me", opts);
        } catch (URISyntaxException e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((GlobalUser) this.getApplication()).tokenCheck();
        setContentView(R.layout.activity_create_game);
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
                    ArrayList<User> friendsToInvite = mInviteFriendRecyclerViewAdapter.inviteFriends();
                    if (friendsToInvite.size() != 0) {
                        TakePictureIntent();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            String imageToSend = encodeImage(imageBitmap);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String token = preferences.getString("TOKEN", null).substring(7);

            if (!token.isEmpty()) {
                Toast.makeText(getApplicationContext(), token, Toast.LENGTH_LONG).show();

            }

//            mSocket.connect();
        }
    }

    public String encodeImage(Bitmap image) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encImage;


    }
}
