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


public class RegistrationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LekkerApplication) getApplication()).setCurrentActivity(this);


        setContentView(R.layout.registration);


        Button btnReturn = (Button)findViewById(R.id.return_btn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GreetingActivity.class);
                intent.putExtra("dog-years", 210);
                startActivity(intent);

            }
        });

        Button btnRegistered = (Button)findViewById(R.id.registered_btn);
        btnRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {

                    long qrCode = getIntent().getIntExtra("qr-code", 0);

                    EditText eMailField =(EditText)findViewById(R.id.email_field);
                    String eMail = eMailField.getText().toString();

                    boolean isNewCustomer = ((LekkerApplication) getApplication()).registration(qrCode, eMail);

                    if (isNewCustomer) {
                        Intent intent = new Intent(getApplicationContext(), RegisteredActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), CardChangedActivity.class);
                        startActivity(intent);
                    }

                    finish();

                }  catch (Exception e) {

                    Log.d(LekkerApplication.class.toString(), e.getMessage(), e);
                    ((LekkerApplication) getApplication()).showMessage(e.getMessage());

                }
            }
        });
    }



    @Override
    public void onBackPressed() {
    }
}
