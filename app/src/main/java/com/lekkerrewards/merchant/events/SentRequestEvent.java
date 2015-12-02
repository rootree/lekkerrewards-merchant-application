package com.lekkerrewards.merchant.events;


import com.lekkerrewards.merchant.network.request.LekkerRequest;
import com.lekkerrewards.merchant.network.response.LekkerResponse;

import retrofit.Call;

public class SentRequestEvent {

    private Call request;

    public SentRequestEvent(Call request) {
        this.request = request;
    }

    public Call getRequest() {
        return request;
    }
}