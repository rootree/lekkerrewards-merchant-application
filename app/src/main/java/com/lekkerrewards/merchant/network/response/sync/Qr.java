package com.lekkerrewards.merchant.network.response.sync;

/**
 * Created by Ivan on 29/10/15.
 */
public class Qr {

    public final int code;
    public final int status;
    public final int source;

    public Qr(
            int code,
            int status,
            int source
    ) {
        this.code = code;
        this.status = status;
        this.source = source;
    }

}