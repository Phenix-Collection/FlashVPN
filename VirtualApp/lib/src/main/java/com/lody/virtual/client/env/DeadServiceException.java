package com.lody.virtual.client.env;


/**
 * Created by doriscoco on 2017/2/11.
 */

public class DeadServiceException extends RuntimeException {
    public DeadServiceException() {
    }

    public DeadServiceException(String message) {
        super(message);
    }

    public DeadServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeadServiceException(Throwable cause) {
        super(cause);
    }

//    public DeadServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//        super(message, cause, enableSuppression, writableStackTrace);
//    }
}
