package com.lekkerrewards.merchant.exceptions;


public class CheckInException extends Exception {
    public CheckInException() {
    }

    public CheckInException(String detailMessage) {
        super(detailMessage);
    }

    public CheckInException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CheckInException(Throwable throwable) {
        super(throwable);
    }
}
