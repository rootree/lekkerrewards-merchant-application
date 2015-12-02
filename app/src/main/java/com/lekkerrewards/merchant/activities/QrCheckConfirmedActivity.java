package com.lekkerrewards.merchant.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.R;


public class QrCheckConfirmedActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_checking_confirmed);
        ((LekkerApplication) getApplication()).setCurrentActivity(this);
        setFinishOnTouchOutside(false);

        int age = getIntent().getIntExtra("user-age", -1);
        String name = getIntent().getStringExtra("user-name");

        Button btnCustomer = (Button)findViewById(R.id.customer_btn);
        btnCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CustomerActivity.class);
                intent.putExtra("dog-years", 210);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override public void onBackPressed() {
        // prevent "back" from leaving this activity
    }


}
