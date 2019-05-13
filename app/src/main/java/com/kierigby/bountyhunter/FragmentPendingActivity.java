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

import com.example.bountyhunterapi.Friend;

import java.util.ArrayList;
import java.util.List;

public class FragmentPendingActivity extends Fragment {


    private final List<Friend> friendsList = new ArrayList<>();
    private FragPendingRecyclerView mFragPendingRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_lobby_pending, container, false);

        RecyclerView mFriendRecyclerView = view.findViewById(R.id.pendingRecyclerView);
        mFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFragPendingRecyclerView = new FragPendingRecyclerView(getContext(), friendsList);
        mFriendRecyclerView.setAdapter(mFragPendingRecyclerView);
        return view;
    }
}
