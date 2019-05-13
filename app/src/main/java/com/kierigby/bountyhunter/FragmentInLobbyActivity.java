package com.kierigby.bountyhunter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bountyhunterapi.BountyHunterAPI;
import com.example.bountyhunterapi.Friend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FragmentInLobbyActivity extends Fragment {

    private final List<Friend> friendsList = new ArrayList<>();
    private FragInLobbyRecyclerView mFragInLobbyRecyclerView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_lobby_in_lobby, container, false);

        RecyclerView mFriendRecyclerView = view.findViewById(R.id.inLobbyRecyclerView);
        mFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFragInLobbyRecyclerView = new FragInLobbyRecyclerView(getContext(), friendsList);
        mFriendRecyclerView.setAdapter(mFragInLobbyRecyclerView);
        return view;
    }


}
