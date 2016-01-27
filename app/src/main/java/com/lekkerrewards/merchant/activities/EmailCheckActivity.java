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


public class EmailCheckActivity extends BaseActivity {
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
        btnCheckIn.setText(getResources().getString(R.string.check_in));
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText eMailField = (EditText) findViewById(R.id.email_field);
                String eMail = eMailField.getText().toString();

                try {
                    ((LekkerApplication) getApplication()).checkInByEmail(eMail);
                } catch (Exception e) {

                    Log.d(LekkerApplication.class.toString(), e.getMessage(), e);

                   // Mint.logException(e);

                    ((LekkerApplication) getApplication()).showMessage(e.getMessage());
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), EmailCheckConfirmedActivity.class);
                intent.putExtra("dog-years", 210);
                startActivity(intent);
            }
        });

        EditText eMailField = (EditText) findViewById(R.id.email_field);
        eMailField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    updateCheckInBtnFromKeyboard();
                }
                return false;
            }
        });

    }

    private void updateCheckInBtnFromKeyboard() {
        Button btnCheckIn = (Button)findViewById(R.id.check_in_btn);
        EditText eMailField =(EditText)findViewById(R.id.email_field);
        if (eMailField.getText().length() == 0) {
            btnCheckIn.setText(getResources().getString(R.string.check_in));
            return;
        }

        String eMail = getString(R.string.check_in_with_user_email) + " " + eMailField.getText().toString().toUpperCase();


        btnCheckIn.setText(eMail);
    }



    @Override
    public void onBackPressed() {
    }
}
