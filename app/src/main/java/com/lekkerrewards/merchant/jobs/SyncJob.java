package com.lekkerrewards.merchant.jobs;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.entities.Redeem;
import com.lekkerrewards.merchant.events.SentRequestEvent;
import com.lekkerrewards.merchant.events.SyncEvent;
import com.lekkerrewards.merchant.network.APIService;
import com.lekkerrewards.merchant.network.api.LekkerAPI;
import com.lekkerrewards.merchant.network.request.RedeemRequest;
import com.lekkerrewards.merchant.network.request.SyncRequest;
import com.lekkerrewards.merchant.network.response.LekkerResponse;
import com.lekkerrewards.merchant.network.response.SyncResponse;
import com.lekkerrewards.merchant.network.response.sync.Reward;
import com.lekkerrewards.merchant.network.response.sync.RewardHistory;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit.Call;

public class SyncJob extends Job {

    private SyncRequest syncRequest;

    public SyncJob(SyncRequest syncRequest) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(
                new Params(Priority.HIGH).
                        requireNetwork().
                        persist().
                        groupBy("send_sync")
        );//order of 1 matter, we don't want to send two in parallel
        //use a negative id so that it cannot collide w/ twitter ids
        //we have to set local id here so it gets serialized into job (to find tweet later on)
        this.syncRequest = syncRequest;
    }

    @Override
    public void onAdded() {
        //job has been secured to disk, add item to database
        // call = APIService.checkInByQR(request);
        //EventBus.getDefault().post(new SendingRequestEvent(qr));
    }

    @Override
    public void onRun() throws Throwable {

        // Create an instance of our API interface.
        LekkerAPI lekker = APIService.get();

        // Create a call instance
        Call<SyncResponse> call = lekker.sync(syncRequest);

        SyncResponse response = (SyncResponse) APIService.send(call, syncRequest);


        updateDB(response);

        EventBus.getDefault().post(new SentRequestEvent(call));
    }

    @Override
    protected void onCancel() {
        //EventBus.getDefault().post(new DeletedRequestEvent(call));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return true;
    }

    private void updateDB(SyncResponse response) {
        if (response.isUpdated) {

            ActiveAndroid.beginTransaction();
            DateTime now = new DateTime();

            try {


                com.lekkerrewards.merchant.entities.Reward rewardForAction;

                for (Map.Entry<String, RewardHistory> entry : response.rewardsHistory.entrySet()) {

                    String rewardCode = entry.getKey();
                    RewardHistory rewardHistory = entry.getValue();

                    rewardForAction = getReward(rewardCode);

                    if (rewardForAction == null) {

                        rewardForAction = getReward(rewardHistory.parentCode);

                        if (rewardForAction != null) {

                            com.lekkerrewards.merchant.entities.RewardHistory rewardHistoryInDB =
                                    new com.lekkerrewards.merchant.entities.RewardHistory();

                            rewardHistoryInDB.createdAt = now;
                            rewardHistoryInDB.fkReward = rewardForAction;
                            rewardHistoryInDB.code = rewardHistory.code;
                            rewardHistoryInDB.name = rewardHistory.name;
                            rewardHistoryInDB.points = rewardHistory.points;

                            rewardHistoryInDB.save();
                            rewardForAction = null;
                        }
                    }

                }


                for (Map.Entry<String, Reward> entry : response.rewardsForUpdate.entrySet()) {

                    String rewardCode = entry.getKey();
                    Reward reward = entry.getValue();

                    rewardForAction = getReward(rewardCode);

                    if (rewardForAction != null) {

                        rewardForAction.createdAt = now;
                        rewardForAction.code = reward.code;
                        rewardForAction.name = reward.name;
                        rewardForAction.points = reward.points;
                        rewardForAction.save();

                    } else {

                        com.lekkerrewards.merchant.entities.Reward newReward =
                                new com.lekkerrewards.merchant.entities.Reward();

                        newReward.createdAt = now;
                        newReward.isActive = true;
                        newReward.updatedAt = now;
                        newReward.code = reward.code;
                        newReward.name = reward.name;
                        newReward.points = reward.points;
                        newReward.fkMerchant = LekkerApplication.getInstance().getMerchantBranch().fkMerchant;
                        newReward.fkMerchantBranch = LekkerApplication.getInstance().getMerchantBranch();
                        newReward.fkOwner = LekkerApplication.getInstance().getOwner();
                        newReward.save();

                        com.lekkerrewards.merchant.entities.RewardHistory rewardHistoryInDB =
                                new com.lekkerrewards.merchant.entities.RewardHistory();

                        rewardHistoryInDB.createdAt = now;
                        rewardHistoryInDB.fkReward = newReward;
                        rewardHistoryInDB.code = reward.code;
                        rewardHistoryInDB.name = reward.name;
                        rewardHistoryInDB.points = reward.points;
                        rewardHistoryInDB.save();

                    }
                    rewardForAction = null;
                    // do what you have to do here
                    // In your case, an other loop.
                }


                for (String codeForDelete : response.rewardsForDelete) {


                    rewardForAction =
                            com.lekkerrewards.merchant.entities.Reward.getByCode(codeForDelete);

                    if (rewardForAction != null) {

                        rewardForAction.isActive = false;
                        rewardForAction.updatedAt = now;
                        rewardForAction.save();
                        rewardForAction = null;
                    }
                }

                ActiveAndroid.setTransactionSuccessful();

                EventBus.getDefault().post(new SyncEvent());

            } finally {
                ActiveAndroid.endTransaction();
                LekkerApplication.isSyncInProcess = false;
                LekkerApplication.updateLastSyncDate();

            }

        }
    }

    private com.lekkerrewards.merchant.entities.Reward getReward(String code) {

        com.lekkerrewards.merchant.entities.RewardHistory rewardHistory =
                com.lekkerrewards.merchant.entities.RewardHistory.findByCode(code);

        return (rewardHistory != null) ? rewardHistory.fkReward : null;
    }
}