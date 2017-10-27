package com.iotledcontroll;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;

/**
 * Created by Cyno-SuperAndroid on 11/18/2016.
 */

public class Progress_Dialog {

    private ProgressDialog progressDialog;
    Context context;
    public Progress_Dialog(Context context)
    {
        this.context=context;
    }

    public void setProgressDialog()
    {
        progressDialog=new ProgressDialog(context);
        try
        {
            progressDialog.show();
        }catch (Exception e)
        {
            Log.e("exception",e.toString());
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.progress_bar);
        progressDialog.setCancelable(false);

    }

    public void hideProgressDialog()
    {

        if(progressDialog != null){
            progressDialog.hide();
        }

    }
}
