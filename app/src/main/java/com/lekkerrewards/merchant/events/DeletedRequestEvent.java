package com.lekkerrewards.merchant.events;

import com.lekkerrewards.merchant.network.request.LekkerRequest;
import com.lekkerrewards.merchant.network.response.LekkerResponse;

import retrofit.Call;

public class DeletedRequestEvent {

    private Call request;

    public DeletedRequestEvent(Call request) {
        this.request = request;
    }

    public Call getRequest() {
        return request;
    }
}