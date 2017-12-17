package com.softwarelab.quickshopee;


import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.ProgressBar;

/**
 * Created by GAURANG PC on 31-10-2017.
 */
public class DatabaseConstants {
    public final static String customers = "customers";
    public final static String items = "items";
    public final static String quantity = "quantity";
    public final static String purchases = "purchases";
    public final static String LOG_STATEMENT = "Log_Statement";
    public final static String ACTIVE_STATUS = "active";
    public final static String PASSIVE_STATUS = "passive";
    private static ProgressDialog dialog = null;
    public static void createLoadingDialog(Activity activity){
        ProgressDialog dialog = ProgressDialog.show(activity, "",
                "Loading. Please wait...", true);
    }
    public static void hideLoadingDialog(){
        if (dialog !=null){
            dialog.dismiss();
        }
        dialog = null;
    }
}
