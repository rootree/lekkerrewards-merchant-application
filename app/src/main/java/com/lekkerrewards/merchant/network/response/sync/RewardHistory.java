package com.lekkerrewards.merchant.network.response.sync;

/**
 * Created by Ivan on 29/10/15.
 */
public class RewardHistory {

    public final String name;
    public final String code;
    public final String parentCode;
    public final int points;
    public final int id;
    public final int rewardId;

    public RewardHistory(
            String name,
            String code,
            String parentCode,
            int points,
            int rewardId,
            int id
    ) {
        this.id = id;
        this.rewardId = rewardId;
        this.points = points;
        this.parentCode = parentCode;
        this.code = code;
        this.name = name;
    }

}
