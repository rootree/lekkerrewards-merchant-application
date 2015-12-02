package com.lekkerrewards.merchant.network.request;

import java.io.Serializable;

/**
 * Created by Ivan on 29/10/15.
 */
public class CheckInByEmailRequest  implements Serializable {

    public final String email;
    public final long timestamp;

    public CheckInByEmailRequest(String email, long timestamp) {
        this.email = email;
        this.timestamp = timestamp;
    }

}
