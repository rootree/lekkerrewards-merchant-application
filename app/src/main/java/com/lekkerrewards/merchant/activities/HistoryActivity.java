package com.lekkerrewards.merchant.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.lekkerrewards.merchant.entities.MerchantsCustomers;
import com.lekkerrewards.merchant.entities.Redeem;
import com.lekkerrewards.merchant.entities.Visit;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;


public class HistoryActivity extends BaseActivity {

    private static final int REQUEST_CODE_HISTROY = 103;
    private static final int REQUEST_CODE_REDEEM = 104;

    private List<Redeem> redeems = new ArrayList<Redeem>();
    private List<Visit> visits = new ArrayList<Visit>();

    private DateTimeFormatter df = DateTimeFormat.forPattern(Config.DATETIME_FORMAT);

    private MerchantBranch merchantBranch;
    private MerchantsCustomers merchantCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LekkerApplication) getApplication()).setCurrentActivity(this);
        setContentView(R.layout.history_activity);

        int age = getIntent().getIntExtra("user-age", -1);
        String name = getIntent().getStringExtra("user-name");

        merchantBranch = ((LekkerApplication) getApplication()).getMerchantBranch();
        merchantCustomer = LekkerApplication.merchantCustomer;

        if (merchantBranch == null || merchantCustomer == null) {
            finish();
        }

        ((TextView)findViewById(R.id.customer_name)).setText(merchantCustomer.fkCustomer.name);


        populateRedeemsList();
        populateRedeemsListView();

        populateVisitsList();
        populateVisitsListView();

        Button btnCheckIn = (Button)findViewById(R.id.return_btn);
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CustomerActivity.class);
                intent.putExtra("dog-years", 210);
                startActivity(intent);

            }
        });


        Button btnStart =(Button)findViewById(R.id.logout_btn);

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getApplicationContext(), GreetingActivity.class);
                intent.putExtra("user-age", 30);
                intent.putExtra("user-name", "Roman");

                startActivity(intent);
                finish();
            }
        });

    }

    private void populateRedeemsList() {
        redeems = Redeem.getRedeems(merchantBranch, merchantCustomer.fkCustomer);
    }

    private void populateVisitsList() {
        visits = Visit.getVisits(merchantBranch, merchantCustomer.fkCustomer);
    }

    private void populateRedeemsListView() {
        ArrayAdapter<Redeem> adapter = new RedeemListAdapter();
        ListView list = (ListView) findViewById(R.id.listView_redeems);

        //ListView lv = (ListView)findViewById(android.R.id.list);
        TextView emptyText = (TextView)findViewById(android.R.id.empty);
        list.setEmptyView(emptyText);

        list.setAdapter(adapter);
    }

    private void populateVisitsListView() {
        ArrayAdapter<Visit> adapter = new VisitListAdapter();
        ListView list = (ListView) findViewById(R.id.listView_visits);
        list.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }

    private class RedeemListAdapter extends ArrayAdapter {
        public RedeemListAdapter() {
            super(HistoryActivity.this, R.layout.redeem_item, redeems);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View redeemItem = convertView;
            if (redeemItem == null) {
                redeemItem = getLayoutInflater().inflate(R.layout.redeem_item, parent, false);
            }

            Redeem currentRedeem = redeems.get(position);

            TextView visitText = (TextView) redeemItem.findViewById(R.id.redeem_date);
            visitText.setText(df.print(currentRedeem.createdAt));

            TextView nameText = (TextView) redeemItem.findViewById(R.id.reward_name);
            nameText.setText(currentRedeem.fkHistoryReward.name);

            double var2 = position %2;
            if(var2 == 1.0) {
                redeemItem.setBackgroundResource( R.color.table_each_first );
            } else {
                redeemItem.setBackgroundResource(R.color.table_each_second);
            }

            return redeemItem;
        }
    }

    private class VisitListAdapter extends ArrayAdapter {
        public VisitListAdapter() {
            super(HistoryActivity.this, R.layout.visit_item, visits);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View visitItem = convertView;
            if (visitItem == null) {
                visitItem = getLayoutInflater().inflate(R.layout.visit_item, parent, false);
            }

            Visit currentVisit = visits.get(position);

            TextView visitText = (TextView) visitItem.findViewById(R.id.visit_date);
            visitText.setText(df.print(currentVisit.createdAt));

            double var2 = position %2;
            if(var2 == 1.0) {
                visitItem.setBackgroundResource( R.color.table_each_first );
            } else {
                visitItem.setBackgroundResource(R.color.table_each_second);
            }

            return visitItem;
        }
    }



    @Override
    public void onBackPressed() {
    }
}
