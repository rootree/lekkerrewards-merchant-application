package com.lekkerrewards.merchant.network.response;

/**
 * Created by Ivan on 29/10/15.
 */
public class LekkerResponse {

    public final String message;
    public final boolean success;
    public final int code;

    public LekkerResponse(String message, boolean success, int code) {
        this.message = message;
        this.success = success;
        this.code = code;
    }

}
