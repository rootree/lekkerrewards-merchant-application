package com.lekkerrewards.merchant;

/**
 * Created by Ivan on 28/10/15.
 */
public class Config {

    //public static final String API_URL = "http://192.168.56.1:8888/api/";
   // public static final String API_URL = "http://lekkerrewards.local:8888/api/";
    public static final String API_URL = "http://lekkerrewards.nl/api/";

    public final static String API_KEY =  "sdfsdsdfsdsdfs4444sdsdfsdsdfsdsdfsd"; // "test-api"
    public final static String MERCHANT_NAME =  "Mankind"; // "test-api"

    public final static String DATETIME_FORMAT = "dd-MM-yyyy HH:mm";

    public final static int LIMIT_VISITS = 100;
    public final static int LIMIT_REDEEMS = 100;

    public final static int CAN_TAKE_NEW_CARD_AFTER = 23;
    public final static int POINTS_PER_VISIT = 1;
    public final static int POINTS_FOR_FIRST_VISIT = 1;
    public final static int CHECKING_COOL_DOWN = 6;//6; @todo
    public final static int SYNC_EVERY_MINS = 15;
    public final static int BEFORE_LOGOUT = 10; // 10 sec.
    public final static int DELAY_BEFORE_LOGOUT = 59; // 59 sec.
    public final static int BEFORE_HOMESCREEN = 60; // 60 sec.
    public final static String DEFAULT_LOCALE = "en"; // nl

}
