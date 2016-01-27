package com.lekkerrewards.merchant.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.R;
import com.splunk.mint.Mint;


public class RegistrationActivity extends BaseActivity {
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
        btnRegistered.setText(getResources().getString(R.string.registration));
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

                    //Mint.logException(e);

                    Log.d(LekkerApplication.class.toString(), e.getMessage(), e);
                    ((LekkerApplication) getApplication()).showMessage(e.getMessage());

                }
            }
        });

        EditText eMailField = (EditText) findViewById(R.id.email_field);
        eMailField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    updateRegistrationBtnFromKeyboard();
                }
                return false;
            }
        });
    }

    private void updateRegistrationBtnFromKeyboard() {

        Button btnCheckIn = (Button)findViewById(R.id.registered_btn);
        EditText eMailField =(EditText)findViewById(R.id.email_field);

        if (eMailField.getText().length() == 0) {
            btnCheckIn.setText(getResources().getString(R.string.registration));
            return;
        }
        String eMail = getString(R.string.registration_with_user_email) + " " + eMailField.getText().toString().toUpperCase();

        btnCheckIn.setText(eMail);
    }



    @Override
    public void onBackPressed() {
    }
}
