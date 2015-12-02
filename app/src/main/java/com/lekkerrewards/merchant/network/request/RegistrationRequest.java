package com.lekkerrewards.merchant.network.request;

import java.io.Serializable;

/**
 * Created by Ivan on 29/10/15.
 */
public class RegistrationRequest  implements Serializable {

    public final String qr;

    public final String email;

    public final long timestamp;

    public RegistrationRequest(String qr, String email, long timestamp) {
        this.qr = qr;
        this.email = email;
        this.timestamp = timestamp;
    }
}
