package com.lekkerrewards.merchant;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.devspark.appmsg.AppMsg;
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


import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ivan on 22/10/15.
 */
public class LekkerApplication extends com.activeandroid.app.Application {


    public static boolean isSyncInProcess = false;
    private Locale locale = null;

    private static LekkerApplication instance;
    private JobManager jobManager;

    public LekkerApplication() {
        instance = this;
    }

    public final static String TAG = "Lekker";

    public final static int QR_SOURCE_WEB = 1;
    public final static int QR_SOURCE_CARD = 2;

    public final static int QR_STATUS_ACTIVATED = 1;
    public final static int QR_STATUS_DEACTIVATED = 2;


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

        setLocale();
        configureJobManager();
        startTimer();
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

         timer.schedule (hourlyTask, 0l, 1000*60*Config.SYNC_EVERY_MINS);
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

        //if (!mCurrentActivity.getLocalClassName().equals(GreetingActivity.class.toString())) {
        if (!(mCurrentActivity instanceof GreetingActivity || mCurrentActivity instanceof RedeemConfirmedActivity)) {
            CountDownTimer countDownTimer = new CountDownTimer(Config.BEFORE_HOMESCREEN * 1000, 1000) {
                public void onTick(long millisUntilFinished) {

                }
                public void onFinish() {
                    tCurrentActivity.finish();

                    //Intent intent = new Intent(getApplicationContext(), GreetingActivity.class);
                    //startActivity(intent);
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
            throw new Exception(getStringById(R.string.message_wrong_qr));
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
            throw new Exception(getStringById(R.string.message_wrong_email));
        }

        Qr qrCard = Qr.getQRByCode(qrCode + "");
        //Qr qrCard = Qr.getQRByCode("840849");
        if (qrCard == null) {
            throw new Exception(getStringById(R.string.message_wrong_qr));
        }

        if (qrCard.fkCustomer != null && qrCard.fkCustomer.eMail.equals(eMail)) {
            throw new Exception(getStringById(R.string.message_email_linked));
        }

        if (qrCard.fkCustomer != null) {
            throw new Exception(getStringById(R.string.message_email_linked_to_another));
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
            throw new Exception(getStringById(R.string.message_wrong_email));
        }

        Customer customer = Customer.getCustomerByEmail(eMail);
        if (customer == null) {
            throw new Exception(getStringById(R.string.message_unregistrated));
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

        LekkerApplication.merchantCustomer = merchantCustomer;

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

            LekkerApplication.merchantCustomer = merchantCustomer;

        } else {

            Date d2 = new Date();
            long diff = d2.getTime() - merchantCustomer.updatedAt.toDate().getTime();//as given

            long hours = TimeUnit.MILLISECONDS.toHours(diff);

            if (hours >= 0 && hours < Config.CHECKING_COOL_DOWN) {
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

        return true;
    }

    public boolean redeem(MerchantsCustomers merchantCustomer, Reward reward) throws Exception {

        if (reward.points > merchantCustomer.points) {
            throw new Exception(getStringById(R.string.message_not_visits));
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
                getLastSyncDate()
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

    public JobManager getJobManager() {
        return jobManager;
    }

    public static LekkerApplication getInstance() {
        return instance;
    }

    public static void logError(Object error) {
        Log.e(LekkerApplication.TAG, error.toString());
    }
}
