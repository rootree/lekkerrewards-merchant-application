package com.lekkerrewards.merchant.jobs;

import android.util.Log;

import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.events.SentRequestEvent;
import com.lekkerrewards.merchant.network.APIService;
import com.lekkerrewards.merchant.network.api.LekkerAPI;
import com.lekkerrewards.merchant.network.request.CheckInByQRRequest;
import com.lekkerrewards.merchant.network.request.RedeemRequest;
import com.lekkerrewards.merchant.network.response.LekkerResponse;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;
import retrofit.Call;

public class RedeemJob extends Job {

    private RedeemRequest redeemRequest;

    public RedeemJob(RedeemRequest redeemRequest) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(
                new Params(Priority.MID).
                        requireNetwork().
                        persist().
                        groupBy("send_request").
                        delayInMs(1000)
        );//order of 1 matter, we don't want to send two in parallel
        //use a negative id so that it cannot collide w/ twitter ids
        //we have to set local id here so it gets serialized into job (to find tweet later on)
        this.redeemRequest = redeemRequest;
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
        Call<LekkerResponse> call = lekker.redeem(redeemRequest);

        LekkerResponse response = APIService.send(call);
        if (response != null && !response.success) {
            Log.e(LekkerApplication.TAG, response.message);
        }

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


}