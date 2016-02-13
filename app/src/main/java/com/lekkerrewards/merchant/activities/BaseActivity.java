package com.lekkerrewards.merchant.activities;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lekkerrewards.merchant.Config;
import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.R;
import com.lekkerrewards.merchant.entities.MerchantBranch;
import com.lekkerrewards.merchant.entities.Reward;
import com.splunk.mint.Mint;
import com.splunk.mint.MintLogLevel;

import java.util.ArrayList;
import java.util.List;


public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setNavVisibility( );
    }

    void setNavVisibility( ) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((LekkerApplication) getApplication()).mixpanel.track("Application stopped");
        ((LekkerApplication) getApplication()).mixpanel.flush();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setNavVisibility( );
    }

    @Override
    protected void onPause() {
        super.onPause();
        //((LekkerApplication) getApplication()).mixpanel.track("Application on pause");
    }

}
