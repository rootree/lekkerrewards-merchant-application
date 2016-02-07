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
import com.lekkerrewards.merchant.network.response.sync.Customer;
import com.lekkerrewards.merchant.network.response.sync.Qr;
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

            ActiveAndroid.beginTransaction();

            try {

                updateRewards(response);

                // First QRs then Customers
                addNewQrs(response.qrsForAdd);
                updateCustomers(response.customersForUpdate);

                ActiveAndroid.setTransactionSuccessful();

                if (response.isUpdated) {
                    EventBus.getDefault().post(new SyncEvent());
                }

            } finally {
                ActiveAndroid.endTransaction();
                LekkerApplication.isSyncInProcess = false;
                LekkerApplication.updateLastSyncDate();

            }


    }

    protected void updateRewards(SyncResponse response) {

        DateTime now = new DateTime();

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

    }

    protected void updateCustomers(Map<String, Customer> customersForUpdate) {

        com.lekkerrewards.merchant.entities.Customer customerForAction;
        com.lekkerrewards.merchant.entities.Qr customerQrNew;
        com.lekkerrewards.merchant.entities.Qr customerQrExists;

        DateTime now = new DateTime();

        for (Map.Entry<String, Customer> entry : customersForUpdate.entrySet()) {

            Customer customer = entry.getValue();

            customerForAction = com.lekkerrewards.merchant.entities.Customer.getCustomerByEmail(
                    customer.eMail
            );

            customerQrNew = com.lekkerrewards.merchant.entities.Qr.getQRByCode(
                    customer.qr
            );

            // Если клиент сушествует
            if (customerForAction != null) {

                customerQrExists =
                        com.lekkerrewards.merchant.entities.Qr.getQRByCustomer(customerForAction);

                // Клиент мог зарегеститровать на себя другую карту
                if (customerQrExists.code != customer.qr) {
                    com.lekkerrewards.merchant.entities.Qr.deactivateByCustomer(customerForAction);
                    customerQrNew.fkCustomer = customerForAction;
                    customerQrNew.status = LekkerApplication.QR_STATUS_ACTIVATED;
                    customerQrNew.save();
                }

                customerForAction.name = customer.name;
                customerForAction.save();

            } else {


                // Клиент не найден в базе, но его qr есть, значит он поменял свои e-mail
                if (customerQrNew.fkCustomer != null) {

                    customerQrNew.fkCustomer.name = customer.name;
                    customerQrNew.fkCustomer.eMail = customer.eMail;
                    customerQrNew.fkCustomer.updatedAt = now;
                    customerQrNew.fkCustomer.save();
                    customerQrNew.status = LekkerApplication.QR_STATUS_ACTIVATED;
                    customerQrNew.save();

                } else {

                    // QR карта свободна и e-mail клиента не был найден в БД
                    com.lekkerrewards.merchant.entities.Customer newCustomer =
                            new com.lekkerrewards.merchant.entities.Customer();

                    newCustomer.createdAt = now;
                    newCustomer.updatedAt = now;
                    newCustomer.name = customer.name;
                    newCustomer.eMail = customer.eMail;
                    newCustomer.save();

                    customerQrNew.fkCustomer = newCustomer;
                    customerQrNew.status = LekkerApplication.QR_STATUS_ACTIVATED;
                    customerQrNew.save();
                }
            }
        }
    }

    protected void addNewQrs(Map<String, Qr> qrsForAdd) {

        com.lekkerrewards.merchant.entities.Qr QrForAction;

        DateTime now = new DateTime();

        for (Map.Entry<String, Qr> entry : qrsForAdd.entrySet()) {

            Qr qr = entry.getValue();

            QrForAction =
                    com.lekkerrewards.merchant.entities.Qr.getQRByCode(
                            qr.code
                    );

            if (QrForAction != null) {

                QrForAction.source = qr.source;
                QrForAction.status = qr.status;
                QrForAction.save();

            } else {

                com.lekkerrewards.merchant.entities.Qr newQr =
                        new com.lekkerrewards.merchant.entities.Qr();

                newQr.createdAt = now;
                newQr.updatedAt = now;
                newQr.code = qr.code;
                newQr.status = qr.status;
                newQr.source = qr.source;
                newQr.save();

            }
        }
    }

    private com.lekkerrewards.merchant.entities.Reward getReward(String code) {

        com.lekkerrewards.merchant.entities.RewardHistory rewardHistory =
                com.lekkerrewards.merchant.entities.RewardHistory.findByCode(code);

        return (rewardHistory != null) ? rewardHistory.fkReward : null;
    }

}