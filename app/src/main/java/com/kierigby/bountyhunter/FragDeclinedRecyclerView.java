package com.kierigby.bountyhunter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.bountyhunterapi.BountyHunterAPI;
import com.example.bountyhunterapi.Friend;

import java.util.List;

public class FragDeclinedRecyclerView extends RecyclerView.Adapter<FragDeclinedRecyclerView.myViewHolder> {

    Context context;
    List<Friend> mFriendList;
    private static BountyHunterAPI api;

    public FragDeclinedRecyclerView(Context context, List<Friend> mFriendList) {
        this.context = context;
        this.mFriendList = mFriendList;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v;
        v = LayoutInflater.from(context).inflate(R.layout.lobby_friend_item, viewGroup, false);
        myViewHolder vHolder = new myViewHolder(v);
        return vHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder myViewHolder, int i) {
        Friend friendObj = mFriendList.get(i);
        String username = friendObj.getFriend().getUsername();
        myViewHolder.friendName.setText(username);
    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        TextView friendName;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.lobbyFriendName);
            api = new BountyHunterAPI(itemView.getContext());
        }
    }
}
