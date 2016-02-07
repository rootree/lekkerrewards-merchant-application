package com.lekkerrewards.merchant.network.response.sync;

/**
 * Created by Ivan on 29/10/15.
 */
public class Customer {

    public final String name;
    public final String eMail;
    public final int qr;

    public Customer(
            String name,
            String eMail,
            int qr
    ) {
        this.name = name;
        this.eMail = eMail;
        this.qr = qr;
    }

}