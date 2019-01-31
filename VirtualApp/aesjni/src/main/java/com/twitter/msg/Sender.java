package com.twitter.msg;

public class Sender {

    static {
        System.loadLibrary("twittermsg");
    }

    public static native String send(Object context, String str);


    public static native String receive(Object context, String str);


    /**
     * @param con
     * @return 1 : pass ， -1 or  -2 : error.
     */
    public static native int check(Object context);

}
