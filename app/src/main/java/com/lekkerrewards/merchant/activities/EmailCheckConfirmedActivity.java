package com.lekkerrewards.merchant.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.R;


public class EmailCheckConfirmedActivity extends Activity {
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
    }
    @Override public void onBackPressed() {
    // prevent "back" from leaving this activity
    }



}


