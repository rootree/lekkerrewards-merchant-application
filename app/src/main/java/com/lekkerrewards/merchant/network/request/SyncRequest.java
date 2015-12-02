package com.lekkerrewards.merchant.network.request;

import java.io.Serializable;

/**
 * Created by Ivan on 29/10/15.
 */
public class SyncRequest implements Serializable {

    public final long timestamp;

    public SyncRequest(long timestamp) {
        this.timestamp = timestamp;
    }
}
