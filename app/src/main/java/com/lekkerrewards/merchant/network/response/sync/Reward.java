package com.lekkerrewards.merchant.network.response.sync;

/**
 * Created by Ivan on 29/10/15.
 */
public class Reward {

    public final String name;
    public final String code;
    public final int points;
    public final int id;

    public Reward(
            String name,
            String code,
            int points,
            int id
    ) {
        this.id = id;
        this.points = points;
        this.code = code;
        this.name = name;
    }

}
