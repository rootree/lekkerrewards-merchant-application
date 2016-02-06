package com.lekkerrewards.merchant.network;

import android.util.Log;

import com.google.gson.Gson;
import com.lekkerrewards.merchant.Config;
import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.network.api.LekkerAPI;
import com.lekkerrewards.merchant.network.request.CheckInByQRRequest;
import com.lekkerrewards.merchant.network.request.LekkerRequest;
import com.lekkerrewards.merchant.network.response.LekkerResponse;
import com.splunk.mint.Mint;
import com.splunk.mint.MintLogLevel;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

    public static LekkerResponse send(Call call, Serializable request) throws IOException {

        // Fetch and print a list of the contributors to the library.
        Response response = call.execute();

        LekkerApplication.logTransaction(request, response);

        if (response.code() != 200) {

           Gson gson = new Gson();
            String json = gson.toJson(request);

            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("HTTPCode", response.code() + "");
            map.put("request", json);

            Mint.logEvent(
                    "API Failed " + request.getClass().getSimpleName(),
                    MintLogLevel.Error,
                    map
            );
        }

        LekkerResponse APIResponse = (LekkerResponse) response.body();

        if (!APIResponse.success) {

            Gson gson = new Gson();
            String requestJSON = gson.toJson(request);
            String responseJSON = gson.toJson(APIResponse);

            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("request", requestJSON);
            map.put("response", responseJSON);

            Mint.logEvent(
                    "Business Failed " + request.getClass().getSimpleName() + " " + APIResponse.message,
                    MintLogLevel.Error,
                    map
            );
        }

        return APIResponse;
    }
}
