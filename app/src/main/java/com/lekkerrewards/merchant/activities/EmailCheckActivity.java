package com.lekkerrewards.merchant.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.R;


public class EmailCheckActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_checking);
        ((LekkerApplication) getApplication()).setCurrentActivity(this);

        Button btnBack = (Button)findViewById(R.id.return_btn);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button btnCheckIn = (Button)findViewById(R.id.check_in_btn);
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText eMailField =(EditText)findViewById(R.id.email_field);
                String eMail = eMailField.getText().toString();

                try {
                    ((LekkerApplication) getApplication()).checkInByEmail(eMail);
                } catch (Exception e) {

                    Log.d(LekkerApplication.class.toString(), e.getMessage(), e);

                    ((LekkerApplication) getApplication()).showMessage(e.getMessage());
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), EmailCheckConfirmedActivity.class);
                intent.putExtra("dog-years", 210);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
    }
}
