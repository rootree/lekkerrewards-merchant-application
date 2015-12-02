package com.lekkerrewards.merchant.network.api;

import com.lekkerrewards.merchant.Config;
import com.lekkerrewards.merchant.network.request.CheckInByEmailRequest;
import com.lekkerrewards.merchant.network.request.CheckInByQRRequest;
import com.lekkerrewards.merchant.network.request.LekkerRequest;
import com.lekkerrewards.merchant.network.request.RedeemRequest;
import com.lekkerrewards.merchant.network.request.RegistrationRequest;
import com.lekkerrewards.merchant.network.request.SyncRequest;
import com.lekkerrewards.merchant.network.response.LekkerResponse;
import com.lekkerrewards.merchant.network.response.SyncResponse;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by Ivan on 29/10/15.
 */
public interface LekkerAPI {

    @POST("check-in-by-qr/"+ Config.API_KEY+"/")
    Call<LekkerResponse> checkInByQR(@Body CheckInByQRRequest body);

    @POST("check-in-by-email/"+ Config.API_KEY+"/")
    Call<LekkerResponse> checkInByEmail(@Body CheckInByEmailRequest body);

    @POST("registration/"+ Config.API_KEY+"/")
    Call<LekkerResponse> registration(@Body RegistrationRequest body);

    @POST("redeem/"+ Config.API_KEY+"/")
    Call<LekkerResponse> redeem(@Body RedeemRequest body);

    @POST("sync/"+ Config.API_KEY+"/")
    Call<SyncResponse> sync(@Body SyncRequest body);
}
