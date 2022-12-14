package com.example.cygoapp.constant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.cygoapp.R;

public class Constant {
    public final static long MinuteInMillis = 60000;
    public final static long HourInMillis = 3600000;
    public final static long DayInMillis = 86400000;

    public final static double BoundsInKilometers = 10; // this is the amount of kilometers that route bounds are expanded by

    //hide keyboard when called this
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getCurrentFocus();
        if (v == null)
        {
            v = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    //checks if device is connected to internet
    public static boolean isNetworkConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //loading dialog with transparent background.
    private AlertDialog loadingDialog;
    public void startLoadingDialog(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        builder.setView(inflater.inflate(R.layout.progressbar_loading, null));
        builder.setCancelable(false);

        loadingDialog = builder.create();
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.show();
    }
    //dismiss the loading dialog.
    public void dismissLoadingDialog()
    {
        loadingDialog.dismiss();
    }
}
