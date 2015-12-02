package com.lekkerrewards.merchant.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.R;


public class RedeemConfirmedActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redeem_confirmed);
        setFinishOnTouchOutside(false);
        ((LekkerApplication) getApplication()).setCurrentActivity(this);

        int rewardPoints = getIntent().getIntExtra("reward-points", -1);
        String rewardName = getIntent().getStringExtra("reward-name");

        Button btnCheckIn = (Button)findViewById(R.id.return_btn);
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CustomerActivity.class);
                intent.putExtra("dog-years", 210);
                startActivity(intent);

            }
        });

        ((TextView)findViewById(R.id.reward_points)).setText(String.format((getResources().getString(R.string.for_visits)), rewardPoints));
        ((TextView)findViewById(R.id.reward_name)).setText(rewardName);
    }
    @Override public void onBackPressed() {
        // prevent "back" from leaving this activity
    }


}
