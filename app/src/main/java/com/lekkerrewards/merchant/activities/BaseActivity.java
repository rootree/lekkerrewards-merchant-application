package com.lekkerrewards.merchant.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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


public class BaseActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //((LekkerApplication) getApplication()).mixpanel.track("Application resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //((LekkerApplication) getApplication()).mixpanel.track("Application on pause");
    }

}
