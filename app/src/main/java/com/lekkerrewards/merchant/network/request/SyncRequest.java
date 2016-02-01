package com.lekkerrewards.merchant.network.request;

import java.io.Serializable;

/**
 * Created by Ivan on 29/10/15.
 */
public class SyncRequest implements Serializable {

    public final long timestamp;

    public final int visits;

    public SyncRequest(long timestamp, int visits) {
        this.timestamp = timestamp;
        this.visits = visits;
    }
}
