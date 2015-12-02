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
import android.widget.Toast;

import com.lekkerrewards.merchant.Config;
import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.R;
import com.lekkerrewards.merchant.entities.MerchantBranch;
import com.lekkerrewards.merchant.entities.Reward;
import com.lekkerrewards.merchant.events.SyncEvent;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class CustomerActivity extends Activity {

    private static final int REQUEST_CODE_HISTROY = 103;
    private static final int REQUEST_CODE_REDEEM = 104;

    private List<Reward> rewards = new ArrayList<Reward>();

    private TextView countDown;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ((LekkerApplication) getApplication()).setCurrentActivity(this);

        setContentView(R.layout.customer_activity);

        MerchantBranch merchantBranch = ((LekkerApplication) getApplication()).getMerchantBranch();

        if (merchantBranch == null || LekkerApplication.merchantCustomer == null) {
            finish();
        }

        TextView marchantName =(TextView)findViewById(R.id.marchantName);
        marchantName.setText(merchantBranch.fkMerchant.name);

        ((TextView)findViewById(R.id.customer_name)).setText(LekkerApplication.merchantCustomer.fkCustomer.name);

        ((TextView)findViewById(R.id.redeems_count)).setText(LekkerApplication.merchantCustomer.redeems + "");
        ((TextView)findViewById(R.id.points)).setText(LekkerApplication.merchantCustomer.points + "");
        ((TextView)findViewById(R.id.total_visits)).setText(LekkerApplication.merchantCustomer.visits + "");

        countDown = (TextView)findViewById(R.id.coundDown);

        populateRewardsList();
        populateRewardsListView();
        startCountDown();

        historyBtn();
        logoutBtn();

        String alreadyCheckIn = getIntent().getStringExtra("already_check_in");
        if (alreadyCheckIn != null){
            ((LekkerApplication) getApplication()).showMessage(alreadyCheckIn);
        }

    }



    private void historyBtn() {
        Button btnStart =(Button)findViewById(R.id.history_btn);

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                intent.putExtra("user-age", 30);
                intent.putExtra("user-name", "Roman");

                //startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_HISTROY);

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void startCountDown() {

        countDownTimer = new CountDownTimer(Config.BEFORE_LOGOUT * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                countDown.setText(
                    millisUntilFinished / 1000 + " " + getResources().getString(R.string.before_logout)
                );
            }

            public void onFinish() {
                logout();
            }

        }.start();

    }

    private void logout() {

        finish();

        Intent intent = new Intent(getApplicationContext(), GreetingActivity.class);
        intent.putExtra("user-age", 30);
        intent.putExtra("user-name", "Roman");

        startActivity(intent);
    }

    private void logoutBtn() {
        Button btnStart =(Button)findViewById(R.id.logout_btn);

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                logout();
            }
        });

    }

    private void populateRewardsList() {

        MerchantBranch merchantBranch = ((LekkerApplication) getApplication()).getMerchantBranch();
        rewards = MerchantBranch.getActiveRewards(merchantBranch);

    }



    private void populateRewardsListView() {
        ArrayAdapter<Reward> adapter = new RewardsListAdapter();
        ListView list = (ListView) findViewById(R.id.listView_rewards);
        list.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_greeting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

       /* //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }



    private class RewardsListAdapter extends ArrayAdapter {
        public RewardsListAdapter() {
            super(CustomerActivity.this, R.layout.reward_item, rewards);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rewardItem = convertView;
            if (rewardItem == null) {
                rewardItem = getLayoutInflater().inflate(R.layout.reward_item, parent, false);
            }

            Reward currentReward = rewards.get(position);

            TextView visitText = (TextView) rewardItem.findViewById(R.id.reward_visits);
            visitText.setText("" + currentReward.points);

            TextView nameText = (TextView) rewardItem.findViewById(R.id.reward_name);
            nameText.setText(currentReward.name);

            Button redeemBtn = (Button) rewardItem.findViewById(R.id.redeem_btn);
            redeemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Reward clickedReward = rewards.get(position);

                    try {
                        ((LekkerApplication) getApplication()).redeem(LekkerApplication.merchantCustomer, clickedReward);

                        Intent intent = new Intent(getApplicationContext(), RedeemConfirmedActivity.class);

                        intent.putExtra("reward-name", clickedReward.name);
                        intent.putExtra("reward-points", clickedReward.points);

                        countDownTimer.cancel();

                        startActivityForResult(intent, REQUEST_CODE_REDEEM);

                    } catch (Exception e) {

                        Log.d(LekkerApplication.class.toString(), e.getMessage(), e);

                        ((LekkerApplication) getApplication()).showMessage(e.getMessage());
                    }

                }
            });

            TextView textView = (TextView) rewardItem.findViewById(R.id.textView1);


            double var2 = position %2;
            if(var2 == 1.0) {
                rewardItem.setBackgroundResource( R.color.table_each_first );
            } else {
                rewardItem.setBackgroundResource(R.color.table_each_second);
            }

            if(currentReward.points > LekkerApplication.merchantCustomer.points) {
                textView.setVisibility(View.VISIBLE);
                redeemBtn.setVisibility(View.GONE);
                textView.setText(getResources().getString(R.string.requeur_more) + " " +
                                (currentReward.points - LekkerApplication.merchantCustomer.points)
                );
            } else {
                redeemBtn.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
            }


            return rewardItem;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_HISTROY) {
            if (resultCode == RESULT_OK) {
                int years = data.getIntExtra("dog-years", -1);
                if (-1 != years) {
                    // txtLabel.setText("You are " + years + " dog years old!!");
                }
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }



}
