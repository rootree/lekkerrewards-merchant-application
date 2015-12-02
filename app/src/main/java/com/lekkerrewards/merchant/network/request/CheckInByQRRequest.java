package com.lekkerrewards.merchant.network.request;

import java.io.Serializable;

/**
 * Created by Ivan on 29/10/15.
 */
public class CheckInByQRRequest implements Serializable {

    public final String qr;
    public final long timestamp;

    public CheckInByQRRequest(String qr, long timestamp) {
        this.qr = qr;
        this.timestamp = timestamp;
    }

}
