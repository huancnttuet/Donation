package com.example.donate.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.donate.DonationApp;
import com.example.donate.R;
import com.example.donate.api.DonationApi;
import com.example.donate.models.Donation;

import java.util.ArrayList;
import java.util.List;

public class Base extends Fragment {
    public DonationApp app;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (DonationApp) getActivity().getApplication();
//
//        app.dbManager.open();
//        app.dbManager.setTotalDonated(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        app.dbManager.close();
    }


}
