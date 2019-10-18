package com.neeraja.wikipedia.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.neeraja.wikipedia.BuildConfig;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

public class Utils {
    protected static RelativeLayout rl;
    private static ProgressBar mProgressBar;
    private static Gson gson = new Gson();


    public static void logE(String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(Constants.LOG_TAG, msg);
        }
    }

    public static void logI(String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(Constants.LOG_TAG, msg);
        }
    }

    public static void logD(String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOG_TAG, msg);
        }
    }

    public static boolean isValidString(String str) {
        if (str != null) {
            str = str.trim();
            if (str.length() > 0)
                return true;
        }
        return false;
    }

    public static boolean isValidArrayList(ArrayList<?> list) {
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    public static ProgressBar getProgressBar(Context context) {
        dismissProgressBar();
        ViewGroup layout = (ViewGroup) ((Activity) context).findViewById(android.R.id.content)
                .getRootView();

        mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
        mProgressBar.setBackground(new ColorDrawable(Color.BLUE));

        RelativeLayout.LayoutParams params = new
                RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        rl = new RelativeLayout(context);

        rl.setGravity(Gravity.CENTER);
        rl.addView(mProgressBar);
        rl.setBackgroundColor(Color.parseColor("#88000000"));

        layout.addView(rl, params);

        return mProgressBar;
    }

    public static void dismissProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.clearAnimation();
            mProgressBar.setVisibility(View.GONE);
        }
        if (rl != null)
            rl.setBackground(new ColorDrawable(Color.TRANSPARENT));

    }
    public static Object parseResp(InputStream is, Class<?> classOfT)
            throws Exception {
        try {
            Reader readr = new InputStreamReader(is);
            return gson.fromJson(readr, classOfT);
        } catch (Exception e) {
            Utils.logE(e.toString());
            throw new CustomException(Constants.ERROR_PARSING,
                    Constants.DATA_INVALID);
            // throw e;
        }
    }

    public static boolean getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnected();
        return isConnected;
    }
}
