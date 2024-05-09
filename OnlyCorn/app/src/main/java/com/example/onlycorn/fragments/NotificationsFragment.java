package com.example.onlycorn.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.onlycorn.R;

public class NotificationsFragment extends Fragment {

    private RecyclerView notificationRecView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        notificationRecView = view.findViewById(R.id.notificationRecView);

    }
}