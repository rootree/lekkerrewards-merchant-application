package com.lekkerrewards.merchant;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.ActiveAndroid;
import com.devspark.appmsg.AppMsg;
import com.google.gson.Gson;
import com.lekkerrewards.merchant.activities.CustomerActivity;
import com.lekkerrewards.merchant.activities.GreetingActivity;
import com.lekkerrewards.merchant.activities.RedeemConfirmedActivity;
import com.lekkerrewards.merchant.entities.Customer;
import com.lekkerrewards.merchant.entities.MerchantBranch;
import com.lekkerrewards.merchant.entities.MerchantsCustomers;
import com.lekkerrewards.merchant.entities.Owner;
import com.lekkerrewards.merchant.entities.Qr;
import com.lekkerrewards.merchant.entities.Redeem;
import com.lekkerrewards.merchant.entities.Reward;
import com.lekkerrewards.merchant.entities.RewardHistory;
import com.lekkerrewards.merchant.entities.Visit;
import com.lekkerrewards.merchant.exceptions.CheckInException;
import com.lekkerrewards.merchant.jobs.CheckInByEmailJob;
import com.lekkerrewards.merchant.jobs.CheckInByQRJob;
import com.lekkerrewards.merchant.jobs.RedeemJob;
import com.lekkerrewards.merchant.jobs.RegistrationJob;
import com.lekkerrewards.merchant.jobs.SyncJob;
import com.lekkerrewards.merchant.network.request.CheckInByEmailRequest;
import com.lekkerrewards.merchant.network.request.CheckInByQRRequest;
import com.lekkerrewards.merchant.network.request.RedeemRequest;
import com.lekkerrewards.merchant.network.request.RegistrationRequest;
import com.lekkerrewards.merchant.network.request.SyncRequest;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;
import com.splunk.mint.Mint;
import com.splunk.mint.MintLogLevel;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Call;
import retrofit.Response;

/**
 * Created by Ivan on 22/10/15.
 */
public class LekkerApplication extends com.activeandroid.app.Application {


    public static String lastQR = "";
    public static boolean isSyncInProcess = false;
    private Locale locale = null;

    private static LekkerApplication instance;
    private JobManager jobManager;
private CountDownTimer countDownTimer;
    public LekkerApplication() {
        instance = this;
    }

    public MixpanelAPI mixpanel;

    public final static String TAG = "Lekker";

    public final static int QR_SOURCE_WEB = 1;
    public final static int QR_SOURCE_CARD = 2;

    public final static int QR_STATUS_ACTIVATED = 1;
    public final static int QR_STATUS_DEACTIVATED = 2;


    public static final String MIXPANEL_TOKEN = "7983b9326e9b96046d381386846d8d36";


    private static MerchantBranch merchantBranch;

    public static MerchantsCustomers merchantCustomer;

    public String getSomething() {
        return TAG;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        if (locale != null)
        {
            newConfig.locale = locale;
            Locale.setDefault(locale);
            getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    public void onCreate() {

        super.onCreate();

        Mint.initAndStartSession(this.getApplicationContext(), "528f4663");

        setLocale();
        configureJobManager();
        startTimer();

        initMixpanel();

        registerBatteryLevelReceiver();
        checkOffOnScreen();


        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//com.lekkerrewards.merchant//databases//loyalty.db";
                String backupDBPath = "loyalty.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
        }
    }

    private void startTimer(){

        Timer timer = new Timer ();
        TimerTask hourlyTask = new TimerTask () {
            @Override
            public void run () {
                if (isSyncInProcess) {
                    return;
                }
                isSyncInProcess = true;
                sync();
            }
        };

         timer.schedule (hourlyTask, 0l, 1000 * 60 * Config.SYNC_EVERY_MINS);
    }
    private void setLocale(){

       // SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        android.content.res.Configuration config = getBaseContext().getResources().getConfiguration();

        String lang = Config.DEFAULT_LOCALE;// settings.getString(getString(R.string.pref_locale), "");
        if (true || !config.locale.getLanguage().equals(lang))
        {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    private Activity mCurrentActivity = null;
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;

        final Activity  tCurrentActivity = mCurrentActivity;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        //if (!mCurrentActivity.getLocalClassName().equals(GreetingActivity.class.toString())) {
        if (!(
                mCurrentActivity instanceof GreetingActivity ||
                mCurrentActivity instanceof CustomerActivity ||
                        mCurrentActivity instanceof RedeemConfirmedActivity
        )) {
            countDownTimer = new CountDownTimer(Config.BEFORE_HOMESCREEN * 1000, 1000) {
                public void onTick(long millisUntilFinished) {

                }
                public void onFinish() {
                    tCurrentActivity.finish();
                }
            }.start();
        }
    }


    public MerchantBranch getMerchantBranch() {
        if (merchantBranch == null) {
            merchantBranch = MerchantBranch.getByAPIKey(Config.API_KEY);
        }
        return merchantBranch;
    }

    public Owner getOwner() {
        return Owner.getByBranch(getMerchantBranch());
    }


    public void showMessage(String message) {
        AppMsg.cancelAll();
        AppMsg.Style style;
        style = AppMsg.STYLE_CONFIRM;
        AppMsg appMsg = AppMsg.makeText(mCurrentActivity, message, style, R.layout.messages_container);
        appMsg.setLayoutGravity(Gravity.BOTTOM);
        appMsg.setAnimation(android.R.anim.fade_in, android.R.anim.fade_out);
        appMsg.show();
        Log.d(TAG, message);

        mixpanel.track(message);
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * @param QRcode
     * @return
     */
    public Qr getQRByScannedCode(String QRcode) throws Exception {

        String[] splited = QRcode.split("\\|");
        if (splited.length != 2) {
            throw new Exception(getStringById(R.string.message_check_card));
        }

        long code = Integer.parseInt(splited[1]);
        long value = Integer.parseInt(splited[0], 16);
        long QRId = value / code;

        if (QRId % 1 != 0) {
            throw new Exception(getStringById(R.string.message_check_card));
        }

        //code = 840849; // TODO: 27/10/15

        Qr qrCard = Qr.getQRByCode(code + "");
        if (qrCard == null) {
            throw new Exception(getStringById(R.string.message_wrong_qr ) + " ("+code+")");
        }

        return qrCard;
    }

    public void checkInByQr(Qr qrCard) throws Exception {

        qrCard.lastUsed = new DateTime();
        qrCard.save();

        makeCheckIn(qrCard.fkCustomer);

        CheckInByQRRequest request = new CheckInByQRRequest(
                qrCard.code + "",
                LekkerApplication.merchantCustomer.updatedAt.getMillis()
        );

        getJobManager().addJobInBackground(new CheckInByQRJob(request));
    }

    public boolean registration(long qrCode, String eMail) throws Exception {

        if (!LekkerApplication.isEmailValid(eMail)) {
            throw new Exception(getStringById(R.string.message_wrong_email) + " ("+eMail+")");
        }

        Qr qrCard = Qr.getQRByCode(qrCode + "");
        //Qr qrCard = Qr.getQRByCode("840849");
        if (qrCard == null) {
            throw new Exception(getStringById(R.string.message_wrong_qr) + " ("+qrCode+")");
        }

        if (qrCard.fkCustomer != null && qrCard.fkCustomer.eMail.equals(eMail)) {
            throw new Exception(getStringById(R.string.message_email_linked) + " ("+eMail+")");
        }

        if (qrCard.fkCustomer != null) {
            throw new Exception(getStringById(R.string.message_email_linked_to_another) + " ("+qrCode+")");
        }

        ActiveAndroid.beginTransaction();
        Customer customer = Customer.getCustomerByEmail(eMail);

        boolean isNewCustomer = true;

        DateTime now = new DateTime();
        try {

            if (customer != null) {

                Qr currentQRCard = Qr.getQRByCustomer(customer);

                if (currentQRCard != null) {

                    Date d2 = new Date();
                    long diff = d2.getTime() - currentQRCard.updatedAt.toDate().getTime();//as given

                    long hours = TimeUnit.MILLISECONDS.toHours(diff);

                    if (
                        //currentQRCard.code != qrCard.code &&
                        currentQRCard.source == QR_SOURCE_CARD &&
                        hours <= Config.CAN_TAKE_NEW_CARD_AFTER
                    ) {
                        throw new Exception(getStringById(R.string.message_qr_card_cooldown));
                    }

                    Qr.deactivateByCustomer(customer);
                    isNewCustomer = false;
                }

            } else {

                customer = new Customer();
                customer.createdAt = now;
                customer.updatedAt = now;
                customer.eMail = eMail;
                customer.password = "N/A";

                String[] splited = eMail.split("@");

                customer.name = splited[0];
                customer.save();

            }

            qrCard.updatedAt = now;
            qrCard.fkCustomer = customer;
            qrCard.status = QR_STATUS_ACTIVATED;
            qrCard.save();

            ActiveAndroid.setTransactionSuccessful();

        } finally {
            ActiveAndroid.endTransaction();
        }

        checkInByQr(qrCard);

        getJobManager().addJobInBackground(
                new RegistrationJob(
                        new RegistrationRequest(qrCode + "", eMail, now.getMillis())
                )
        );

        return isNewCustomer;
    }

    /**
     * @param eMail
     * @return
     */
    public void checkInByEmail(String eMail) throws Exception {

        if (!LekkerApplication.isEmailValid(eMail)) {
            throw new Exception(getStringById(R.string.message_wrong_email) + " ("+eMail+")");
        }

        Customer customer = Customer.getCustomerByEmail(eMail);
        if (customer == null) {
            throw new Exception(getStringById(R.string.message_unregistrated) + " ("+eMail+")");
        }

        makeCheckIn(customer);

        CheckInByEmailRequest request = new CheckInByEmailRequest(
                eMail,
                LekkerApplication.merchantCustomer.updatedAt.getMillis()
        );

        getJobManager().addJobInBackground(new CheckInByEmailJob(request));
    }

    /**
     * @param customer
     * @return
     */
    private boolean makeCheckIn(Customer customer) throws Exception {

        int obtainedPoints = Config.POINTS_PER_VISIT;

        MerchantsCustomers merchantCustomer = MerchantsCustomers.getMerchantCustomerRelation(merchantBranch, customer);

        DateTime now = new DateTime();

        if (merchantCustomer == null) {

            merchantCustomer = new MerchantsCustomers();
            merchantCustomer.createdAt = now;
            merchantCustomer.updatedAt = now;
            merchantCustomer.firstAt = now;
            merchantCustomer.fkCustomer = customer;
            merchantCustomer.fkMerchantBranch = merchantBranch;
            merchantCustomer.fkMerchant = merchantBranch.fkMerchant;

            obtainedPoints = Config.POINTS_FOR_FIRST_VISIT;

        } else {

            Date d2 = new Date();
            long diff = d2.getTime() - merchantCustomer.updatedAt.toDate().getTime();//as given

            long hours = TimeUnit.MILLISECONDS.toHours(diff);

            if (hours >= 0 && hours < Config.CHECKING_COOL_DOWN) {

                LekkerApplication.merchantCustomer = merchantCustomer;

                throw new CheckInException(getStringById(R.string.message_have_check_in));
            }

        }

        ActiveAndroid.beginTransaction();

        try {

            merchantCustomer.visits = merchantCustomer.visits + 1;
            merchantCustomer.points = merchantCustomer.points + obtainedPoints;
            merchantCustomer.updatedAt = now;
            merchantCustomer.save();

            Visit visit = new Visit();

            visit.createdAt = now;
            visit.updatedAt = now;
            visit.fkCustomer = customer;
            visit.fkMerchantBranch = merchantBranch;
            visit.fkMerchant = merchantBranch.fkMerchant;
            visit.obtainedPoints = obtainedPoints;
            visit.status = 1;

            visit.save();

            ActiveAndroid.setTransactionSuccessful();

        } finally {
            ActiveAndroid.endTransaction();
        }

        LekkerApplication.merchantCustomer = merchantCustomer;

        return true;
    }

    public boolean redeemCheck(MerchantsCustomers merchantCustomer, Reward reward) throws Exception {

        if (reward.points > merchantCustomer.points) {
            throw new Exception(getStringById(R.string.message_not_visits));
        }

        return true;
    }

    public boolean redeem(MerchantsCustomers merchantCustomer, Reward reward) throws Exception {

        if (!redeemCheck(merchantCustomer, reward)) {
            return false;
        }

        ActiveAndroid.beginTransaction();
        DateTime now = new DateTime();

        try {


            Redeem redeem = new Redeem();
            redeem.createdAt = now;
            redeem.updatedAt = now;
            redeem.fkCustomer = merchantCustomer.fkCustomer;
            redeem.fkMerchant = merchantCustomer.fkMerchant;
            redeem.fkMerchantBranch = merchantCustomer.fkMerchantBranch;
            redeem.fkReward = reward;
            redeem.fkHistoryReward = RewardHistory.findByCode(reward.code);
            redeem.status = 1;
            redeem.spent = reward.points;
            redeem.total = merchantCustomer.points;
            redeem.save();
            merchantCustomer.points = merchantCustomer.points - reward.points;
            merchantCustomer.redeems = merchantCustomer.redeems + 1;
            merchantCustomer.updatedAt = now;
            merchantCustomer.save();

            ActiveAndroid.setTransactionSuccessful();

        } finally {
            ActiveAndroid.endTransaction();
        }

        getJobManager().addJobInBackground(new RedeemJob(new RedeemRequest(
                reward.code,
                merchantCustomer.fkCustomer.eMail,
                now.getMillis()
        )));

        return true;
    }

    public static void updateLastSyncDate()
    {
        SharedPreferences mPrefs = getInstance().getSharedPreferences("label", 0);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putLong("LastSync", System.currentTimeMillis()).commit();
    }

    public long getLastSyncDate()
    {
        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        return mPrefs.getLong("LastSync", 0L);
    }

    public boolean sync() {

        getJobManager().addJobInBackground(new SyncJob(new SyncRequest(
                getLastSyncDate(),
                Visit.getCountOfAllVisits(this.getMerchantBranch())
        )));

        return true;
    }

    private String getStringById(int res) {
        return getString(res);
    }


    private void configureJobManager() {
        com.path.android.jobqueue.config.Configuration configuration = new com.path.android.jobqueue.config.Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";
                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .build();
        jobManager = new JobManager(this, configuration);
    }

    private BroadcastReceiver battery_receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            boolean isPresent = intent.getBooleanExtra("present", false);
            String technology = intent.getStringExtra("technology");
            int plugged = intent.getIntExtra("plugged", -1);
            int scale = intent.getIntExtra("scale", -1);
            int health = intent.getIntExtra("health", 0);
            int status = intent.getIntExtra("status", 0);
            int rawlevel = intent.getIntExtra("level", -1);
            int level = 0;
            String temp=null;

            Bundle bundle = intent.getExtras();

            if (rawlevel <= 15 && rawlevel > 14) {
                mixpanel.track("Low battery");
                //Mint.logEvent("Low battery", MintLogLevel.Warning);
            }
        }
    };

    private void registerBatteryLevelReceiver(){
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(battery_receiver, filter);
    }

    private void initMixpanel(){
        // Initialize the library with your
        // Mixpanel project token, MIXPANEL_TOKEN, and a reference
        // to your application context.
        mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), MIXPANEL_TOKEN);

        mixpanel.identify(Config.MERCHANT_NAME);
        mixpanel.getPeople().identify(Config.MERCHANT_NAME);

        mixpanel.track("Application started");
    }

    private void checkOffOnScreen(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    mixpanel.track("Screen is off");
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    mixpanel.track("Screen is on");
                }
            }
        }, intentFilter);
    }


    public JobManager getJobManager() {
        return jobManager;
    }

    public static LekkerApplication getInstance() {
        return instance;
    }

    public static void logError(Object error) {
        Log.e(LekkerApplication.TAG, error.toString());
    }

    public static void logTransaction(Serializable request, Response response) {

        String date = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date());

        Gson gson = new Gson();
        String json = gson.toJson(request);
        String jsonResponse = gson.toJson(response.body());

        String lineForLog = String.format(
                "%s | %d | %s | %s | %s",
                date,
                response.code(),
                request.getClass().getSimpleName(),
                json,
                jsonResponse
        );

        getInstance().appendLog(
                lineForLog,
                "transactions"
        );
        //Log.e(LekkerApplication.TAG, call.toString());
    }

    private File getLogFile(String type)
    {
        //File backupPath = Environment.getExternalStorageDirectory();

       // File backupPath = new File(getApplicationInfo().dataDir + "/files");

        File backupPath = new File(Environment.getExternalStorageDirectory().getPath() + "/logs");

        if(!backupPath.exists()){
            backupPath.mkdirs();
        }


        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        File logFile = new File(backupPath + "/" + type + "_" + date + ".txt");

        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                Mint.logException(e);
            }
        }
        return  logFile;
    }

    public void appendLog(String text, String type)
    {
        File logFile = getLogFile(type);
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));

            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            Mint.logException(e);
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }
}
