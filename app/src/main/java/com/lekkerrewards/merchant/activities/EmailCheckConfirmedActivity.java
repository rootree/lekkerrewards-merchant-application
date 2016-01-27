package com.lekkerrewards.merchant.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lekkerrewards.merchant.Config;
import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.R;


public class EmailCheckConfirmedActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        ((LekkerApplication) getApplication()).setCurrentActivity(this);

        setContentView(R.layout.email_checking_confirmed);

        int age = getIntent().getIntExtra("user-age", -1);
        String name = getIntent().getStringExtra("user-name");

        Button btnCheckIn = (Button)findViewById(R.id.return_btn);
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GreetingActivity.class);
                intent.putExtra("dog-years", 210);
                startActivity(intent);

            }
        });

        ((TextView)findViewById(R.id.cooldown)).setText(
                String.format(getString(R.string.cooldown_hint), Config.CHECKING_COOL_DOWN)
        );
    }
    @Override public void onBackPressed() {
    // prevent "back" from leaving this activity
    }



}


