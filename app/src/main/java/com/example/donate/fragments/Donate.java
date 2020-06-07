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
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.donate.R;
import com.example.donate.api.DonationApi;
import com.example.donate.models.Donation;
import com.webianks.library.scroll_choice.ScrollChoice;

import java.util.ArrayList;
import java.util.List;

public class Donate extends Base {

    private Button donateButton;
    private RadioGroup paymentMethod;
    private ProgressBar progressBar;
    private NumberPicker amountPicker;
    private EditText amountText;
    private TextView amountTotal;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        setHasOptionsMenu(true);
        new GetAllTask(getContext()).execute("/donations");
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        paymentMethod = (RadioGroup)   view.findViewById(R.id.paymentMethod);
        progressBar   = (ProgressBar)  view.findViewById(R.id.progressBar);
        amountPicker  = (NumberPicker) view.findViewById(R.id.amountPicker);
        amountText    = (EditText)     view.findViewById(R.id.paymentAmount);
        amountTotal   = (TextView)     view.findViewById(R.id.totalSoFar);
        amountPicker.setMinValue(0);
        amountPicker.setMaxValue(1000);
        progressBar.setMax(app.target);
        progressBar.setProgress(app.totalDonated);
        String totalDonatedStr = "$" + app.totalDonated;
        amountTotal.setText(totalDonatedStr);

        donateButton = (Button) view.findViewById(R.id.donateButton);
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donateButtonPressed(v);
            }
        });


    }
    public void donateButtonPressed (View view)
    {
        String method = paymentMethod.getCheckedRadioButtonId() == R.id.paypal_btn ? "PayPal" : "Direct";
        int donatedAmount =  amountPicker.getValue();
        if (donatedAmount == 0)
        {
            String text = amountText.getText().toString();
            if (!text.equals(""))
                donatedAmount = Integer.parseInt(text);
        }
        if (donatedAmount > 0) {
            Donation donation = new Donation(donatedAmount, method, 69,96);
            app.newDonation(donation);
            new InsertTask(getContext()).execute("/donations",donation);
            progressBar.setProgress(app.totalDonated);
            String totalDonatedStr = "$" + app.totalDonated;
            amountTotal.setText(totalDonatedStr);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuReport:
                Toast toast = Toast.makeText(getContext(), "Report Selected", Toast.LENGTH_SHORT);
                toast.show();
                if(app.donations == null){
                    Toast toast3 = Toast.makeText(getContext(), "Report Empty", Toast.LENGTH_SHORT);
                    toast3.show();
                    return true;
                }
                Navigation.findNavController(getActivity(),R.id.nav_host_fragment)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                break;

            case R.id.menuReset:
//                app.dbManager.reset();
//                Toast toast2 = Toast.makeText(getContext(), "Reseted", Toast.LENGTH_SHORT);
//                toast2.show();
//                app.donations.clear();
//                app.totalDonated = 0;
//                progressBar.setProgress(app.totalDonated);
//                String totalDonatedStr = "$" + app.totalDonated;
//                amountTotal.setText(totalDonatedStr);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete ALL Donation?");
                builder.setIcon(android.R.drawable.ic_delete);
                builder.setMessage("Are you sure you want to Delete All the Donations ?");
                builder.setCancelable(false);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        new ResetTask(getContext()).execute("/donations");
                        Log.v("XXXXXX", String.valueOf(app.donations.size()));
                        for(int i=0;i<app.donations.size();i++){
                            Donation donation = app.donations.get(i);
                            new Donate.DeleteTask(getContext()).execute("/donations", donation._id);
                        }
                        new GetAllTask(getContext()).execute("/donations");
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

                return true;
        }
        return false;
    }

    private class GetAllTask extends AsyncTask<String, Void, List<Donation>> {

        protected ProgressDialog         dialog;
        protected Context                 context;

        public GetAllTask(Context context)
        {
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
                Log.v("donate", "Donation App Getting All Donations");
                return (List<Donation>) DonationApi.getAll((String) params[0]);
            }
            catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Donation> result) {
            super.onPostExecute(result);
            app.donations = result;
            int sum = 0;
            //use result to calculate the totalDonated amount here
            for(int i = 0; i < result.size(); i++){
                sum += result.get(i).amount;
            }
//            Log.v("respone", String.valueOf(result.size()));
            app.totalDonated = sum;
            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }
    private class InsertTask extends AsyncTask<Object, Void, String> {

        protected ProgressDialog dialog;
        protected Context context;

        public InsertTask(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Saving Donation....");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {

            String res = null;
            try {
                Log.v("donate", "Donation App Inserting");
                return (String) DonationApi.insert((String) params[0],(Donation) params[1]);
            }

            catch(Exception e)
            {
                Log.v("donate","ERROR : " + e);
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new GetAllTask(getContext()).execute("/donations");
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


            if (dialog.isShowing())
                dialog.dismiss();
        }
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
            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }
}
