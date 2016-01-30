package com.lekkerrewards.merchant.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.lekkerrewards.merchant.Config;
import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.R;
import com.lekkerrewards.merchant.entities.MerchantBranch;
import com.lekkerrewards.merchant.entities.Reward;
import com.lekkerrewards.merchant.events.DeletedRequestEvent;
import com.lekkerrewards.merchant.events.SendingRequestEvent;
import com.lekkerrewards.merchant.events.SentRequestEvent;
import com.lekkerrewards.merchant.events.SyncEvent;
import com.lekkerrewards.merchant.network.APIService;
import com.lekkerrewards.merchant.network.api.LekkerAPI;
import com.lekkerrewards.merchant.network.request.SyncRequest;
import com.lekkerrewards.merchant.network.response.SyncResponse;

import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import retrofit.Call;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class GreetingActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = ScanningActivity.class.getSimpleName();

    private static final int REQUEST_CODE_FUNCTONE = 100;
    private static final int REQUEST_RETURN_FROM_CHECK_IN = 101;


    boolean checkStep2 = false;
    boolean checkStep3 = false;

    private List<Reward> rewards = new ArrayList<Reward>();
    MerchantBranch merchantBranch;

    public void onClick(View v) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // mAvailableCountText.setText(Html.fromHtml(getResources().getQuantityString(R.plurals.hotels_count_available, event.getCount(), event.getCount())));
        super.onCreate(savedInstanceState);

        LekkerApplication.merchantCustomer = null;

        setContentView(R.layout.activity_greeting);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("OpenSans_Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );


        merchantBranch = ((LekkerApplication) getApplication()).getMerchantBranch();
        ((LekkerApplication) getApplication()).setCurrentActivity(this);

        if (merchantBranch == null) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.title_bad_settings));
            alertDialogBuilder
                    .setMessage(getResources().getString(R.string.message_bad_settings))
                    .setCancelable(false)
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    moveTaskToBack(true);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(1);
                                }
                            });

                   /* .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });*/

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;

        }
        //MerchantBranch test = MerchantBranch.getActiveRewards(merchantBranch);

        EventBus.getDefault().register(this);


        startBtn();
        checkInWithEmailBtn();
        localeBtn();
        hiddenExit();

        populateRewardsList();
        populateRewardsListView();

        TextView marchantName = (TextView) findViewById(R.id.marchantName);
        marchantName.setText(merchantBranch.fkMerchant.name);

        int isUpdate = getIntent().getIntExtra("is_update", 0);
        if (isUpdate == 1) {
            ((LekkerApplication) getApplication()).showMessage(getString(R.string.update_rewards));
        }

    }


    private void startBtn() {
        Button btnStart = (Button) findViewById(R.id.main_btn);

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {


                // ((LekkerApplication) getApplication()).sync();


                Intent intent = new Intent(getApplicationContext(), ScanningActivity.class);
                intent.putExtra("user-age", 30);
                intent.putExtra("user-name", "Roman");

                //startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_FUNCTONE);

            }
        });

    }

    private void checkInWithEmailBtn() {


        Button btnStart = (Button) findViewById(R.id.checkIn_btn);

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(getApplicationContext(), EmailCheckActivity.class);
                startActivityForResult(intent, REQUEST_RETURN_FROM_CHECK_IN);

            }
        });


    }

    private void hiddenExit() {


        TextView step1DescText = (TextView) findViewById(R.id.step1_desc);

        step1DescText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                checkStep2 = false;
                checkStep3 = false;

                Timer timer = new Timer();
                TimerTask hourlyTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (checkStep2 && checkStep3) {
                            moveTaskToBack(true);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        } else {
                            checkStep2 = false;
                            checkStep3 = false;
                        }
                    }
                };

                timer.schedule(hourlyTask, 0l, 3000);
            }
        });

        TextView step2DescText = (TextView) findViewById(R.id.step2_desc);
        TextView step3DescText = (TextView) findViewById(R.id.step3_desc);

        step2DescText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                checkStep2 = true;
            }
        });
        step3DescText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                checkStep3 = true;
            }
        });
    }

    private void localeBtn() {

        ImageButton nlStart = (ImageButton) findViewById(R.id.nl_locale);

        nlStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Locale locale = new Locale("nl");
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

                finish();
                Intent myIntent = new Intent(getBaseContext(), GreetingActivity.class);
                startActivity(myIntent);

            }
        });


        ImageButton enStart = (ImageButton) findViewById(R.id.en_locale);

        enStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Locale locale = new Locale("en");
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

                finish();
                Intent myIntent = new Intent(getBaseContext(), GreetingActivity.class);
                startActivity(myIntent);

            }
        });

        String currentLocale = getBaseContext().getResources().getConfiguration().locale.toString();

        if (!currentLocale.equals(Config.DEFAULT_LOCALE)) {
            nlStart.setVisibility(View.GONE);
            enStart.setVisibility(View.VISIBLE);
        } else {
            nlStart.setVisibility(View.VISIBLE);
            enStart.setVisibility(View.GONE);
        }
    }


    private void populateRewardsList() {

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


    private class RewardsListAdapter extends ArrayAdapter {
        public RewardsListAdapter() {
            super(GreetingActivity.this, R.layout.reward_item, rewards);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
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
            TextView textView = (TextView) rewardItem.findViewById(R.id.textView1);
            redeemBtn.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);

            double var2 = position % 2;
            if (var2 == 1.0) {
                rewardItem.setBackgroundResource(R.color.table_each_first);
            } else {
                rewardItem.setBackgroundResource(R.color.table_each_second);
            }

            return rewardItem;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Toast.makeText(this, "result ", Toast.LENGTH_SHORT).show();

        if (requestCode == REQUEST_CODE_FUNCTONE) {
            if (resultCode == RESULT_OK) {
                int years = data.getIntExtra("dog-years", -1);
                if (-1 != years) {
                    // txtLabel.setText("You are " + years + " dog years old!!");
                }
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LekkerApplication.merchantCustomer = null;
    }

    @Override
    public void onBackPressed() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(SentRequestEvent ignored) {
        //Toast.makeText(this, "SentRequestEvent", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(SendingRequestEvent ignored) {
        //Toast.makeText(this, "SendingRequestEvent", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DeletedRequestEvent ignored) {
        //Toast.makeText(this, "DeletedRequestEvent", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(SyncEvent ignored) {
        //Toast.makeText(this, "DeletedRequestEvent", Toast.LENGTH_SHORT).show();

        String activeClass = ((LekkerApplication) getApplication()).getCurrentActivity().getClass().getName();
        if (
                activeClass.equals(GreetingActivity.class.getName()) ||
                        activeClass.equals(CustomerActivity.class.getName())
                ) {

            finish();
            Intent myIntent = new Intent(getBaseContext(), GreetingActivity.class);
            myIntent.putExtra("is_update", 1);

            startActivity(myIntent);

        }


    }
}
