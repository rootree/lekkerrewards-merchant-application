package com.lekkerrewards.merchant.network;

import android.util.Log;

import com.lekkerrewards.merchant.Config;
import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.network.api.LekkerAPI;
import com.lekkerrewards.merchant.network.request.CheckInByQRRequest;
import com.lekkerrewards.merchant.network.request.LekkerRequest;
import com.lekkerrewards.merchant.network.response.LekkerResponse;

import java.io.IOException;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by Ivan on 28/10/15.
 */
public class APIService {

    public static LekkerAPI get() {

        // Create a very simple REST adapter which points the GitHub API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create an instance of our API interface.
        return retrofit.create(LekkerAPI.class);
    }

    public static LekkerResponse send(Call call) throws IOException {

        // Fetch and print a list of the contributors to the library.
        Response response = call.execute();

        if (response.code() >= 500) {
            LekkerApplication.logError(response.raw());
        }

        LekkerResponse APIResponse = (LekkerResponse) response.body();

        return APIResponse;
    }
}
