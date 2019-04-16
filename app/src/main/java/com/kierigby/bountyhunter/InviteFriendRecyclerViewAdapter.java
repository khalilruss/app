package com.kierigby.bountyhunter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.bountyhunterapi.BountyHunterAPI;
import com.example.bountyhunterapi.Friend;
import com.example.bountyhunterapi.User;

import java.util.ArrayList;
import java.util.List;

public class InviteFriendRecyclerViewAdapter extends RecyclerView.Adapter<InviteFriendRecyclerViewAdapter.MyViewHolder> {
    private static List<Friend> mFriendList;
    private static BountyHunterAPI api;
    static SparseBooleanArray friendsToInvite = new SparseBooleanArray();

    InviteFriendRecyclerViewAdapter(List<Friend> users) {
        mFriendList = users;
    }

    /**
     * Provide a reference to the views for each data item
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        CheckBox inviteCheckBox;

        /**
         * Sets up the view for each data item,
         *
         * @param itemView the view that will be used by the item
         */
        MyViewHolder(final View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.inviteFriendName);
            api = new BountyHunterAPI(itemView.getContext());
            inviteCheckBox = itemView.findViewById(R.id.inviteFriendCheckBox);
        }
    }

    @NonNull
    @Override
    public InviteFriendRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.creategame_item, viewGroup, false);
        return new InviteFriendRecyclerViewAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final InviteFriendRecyclerViewAdapter.MyViewHolder myViewHolder, final int i) {

        Friend friendObj = mFriendList.get(i);
        String username = friendObj.getFriend().getUsername();
        myViewHolder.textView.setText(username);
        if (!friendsToInvite.get(myViewHolder.getAdapterPosition(), false)) {
            myViewHolder.inviteCheckBox.setChecked(false);
        } else {
            myViewHolder.inviteCheckBox.setChecked(true);
        }
        myViewHolder.inviteCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!friendsToInvite.get(myViewHolder.getAdapterPosition(), false)) {
                    myViewHolder.inviteCheckBox.setChecked(true);
                    friendsToInvite.put(myViewHolder.getAdapterPosition(),true);
                } else {
                    myViewHolder.inviteCheckBox.setChecked(false);
                    friendsToInvite.put(myViewHolder.getAdapterPosition(),false);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }

    public ArrayList<User> inviteFriends() {
        ArrayList<User> sendInvitesTo = new ArrayList<>();
        for (int i = 0; i < friendsToInvite.size(); i++) {
            boolean invite = friendsToInvite.valueAt(i);
            if (invite) {
                sendInvitesTo.add(mFriendList.get(i).getFriend());
            }
        }

        return sendInvitesTo;

    }

}
