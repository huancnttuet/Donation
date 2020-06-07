package com.example.donate.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.donate.R;
import com.example.donate.api.DonationApi;
import com.example.donate.models.Donation;
import com.example.donate.models.DonationAdapter;

import java.util.List;

public class Report extends Base {
    ListView listView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = (ListView) view.findViewById(R.id.reportList);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.report_swipe_refresh_layout);

        new GetAllTask(getContext()).execute("/donations");

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetAllTask(getContext()).execute("/donations");
            }
        });


//        DonationAdapter adapter = new DonationAdapter(getContext(), app.dbManager.getAll());
//        DonationAdapter adapter = new DonationAdapter(getContext(), app.donations);
//
//        listView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.menuDonate:
                Toast toast2 = Toast.makeText(getContext(), "Donate Selected", Toast.LENGTH_SHORT);
                toast2.show();
                Navigation.findNavController(getActivity(),R.id.nav_host_fragment)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
                break;
            case R.id.menuReset:
//                Toast toast3 = Toast.makeText(getContext(), "Reseted", Toast.LENGTH_SHORT);
//                toast3.show();
//                app.donations.clear();
//                app.totalDonated = 0;
//                DonationAdapter adapter = new DonationAdapter(getContext(), app.donations);
//                listView.setAdapter(adapter);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete ALL Donation?");
                builder.setIcon(android.R.drawable.ic_delete);
                builder.setMessage("Are you sure you want to Delete All the Donations ?");
                builder.setCancelable(false);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        new ResetTask(getContext()).execute("/donations");
                        for(int i=0;i<app.donations.size();i++){
                            Donation donation = app.donations.get(i);
                            new DeleteTask(getContext()).execute("/donations", donation._id);
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return false;
        }
        return false;
    }

    private class GetAllTask extends AsyncTask<String, Void, List<Donation>> {

        protected ProgressDialog dialog;
        protected Context context;

        public GetAllTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donations List");
            this.dialog.show();
        }

        @Override
        protected List<Donation> doInBackground(String... params) {

            try {
                return (List<Donation>) DonationApi.getAll((String) params[0]);
            } catch (Exception e) {
                Log.v("ASYNC", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Donation> result) {
            super.onPostExecute(result);

            app.donations = result;
            DonationAdapter adapter = new DonationAdapter(context, app.donations);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final Donation value = (Donation) parent.getItemAtPosition(position);
                    Toast.makeText(getContext(), "Donation Data [ " + value.upvotes + "]\n " +
                            "With ID of [" + value._id + "]", Toast.LENGTH_LONG).show();
                    view.findViewById(R.id.imgDelete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onDonationDelete(value);
                        }
                    });
                }
            });
            mSwipeRefreshLayout.setRefreshing(false);
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    private class DeleteTask extends AsyncTask<String, Void, String> {

        protected ProgressDialog dialog;
        protected Context context;

        public DeleteTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Deleting Donation");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return (String) DonationApi.delete((String) params[0], (String) params[1]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            String s = result;
            Log.v("donate", "DELETE REQUEST : " + s);

            new GetAllTask(getContext()).execute("/donations");

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    public void onDonationDelete(final Donation donation) {
        String stringId = donation._id;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Donation?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to Delete the \'Donation with ID \' \n [ "
                + stringId + " ] ?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                new DeleteTask(getContext()).execute("/donations", donation._id);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class ResetTask extends AsyncTask<Object, Void, String> {

        protected ProgressDialog         dialog;
        protected Context                 context;

        public ResetTask(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Deleting Donations....");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {

            String res = null;
            try {
                res = DonationApi.deleteAll((String)params[0]);
            }

            catch(Exception e)
            {
                Log.v("donate"," RESET ERROR : " + e);
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            app.totalDonated = 0;

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }
}
