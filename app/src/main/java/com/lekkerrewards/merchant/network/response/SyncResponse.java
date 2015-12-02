package com.lekkerrewards.merchant.network.response;

import com.lekkerrewards.merchant.network.response.sync.Reward;
import com.lekkerrewards.merchant.network.response.sync.RewardHistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ivan on 29/10/15.
 */
public class SyncResponse extends LekkerResponse{

    public final boolean isUpdated;
    public final ArrayList<String> rewardsForDelete;
    public final HashMap<String, Reward> rewardsForUpdate;
    public final HashMap<String, RewardHistory> rewardsHistory;
    public final long dateTime;

    public SyncResponse(
            String message,
            boolean success,
            boolean isUpdated,
            ArrayList<String> rewardsForDelete,
            HashMap<String, Reward> rewardsForUpdate,
            HashMap<String, RewardHistory> rewardsHistory,
            long dateTime,
            int code
    ) {
        super(message, success, code);
        this.isUpdated = isUpdated;
        this.rewardsForDelete = rewardsForDelete;
        this.rewardsForUpdate = rewardsForUpdate;
        this.rewardsHistory = rewardsHistory;
        this.dateTime = dateTime;
    }

}
