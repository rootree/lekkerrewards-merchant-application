package com.lekkerrewards.merchant.network.request;

import java.io.Serializable;

/**
 * Created by Ivan on 29/10/15.
 */
public class RedeemRequest implements Serializable {

    public final String code;

    public final String email;

    public final long timestamp;

    public RedeemRequest(String code, String email, long timestamp) {
        this.code = code;
        this.email = email;
        this.timestamp = timestamp;
    }
}
