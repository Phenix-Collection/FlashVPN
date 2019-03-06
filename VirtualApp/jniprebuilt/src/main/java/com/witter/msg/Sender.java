package com.witter.msg;

public class Sender {

    static {
        System.loadLibrary("wittermsg");
    }

    public static native String send(String str);


    public static native String receive(String str);


    /**
     * @param context
     * @return 1 : pass ， -1 or  -2 : error.
     */
    public static native int check(Object context);

    public static native String ssend(Object context, String str);


    public static native String rreceive(Object context, String str);
}
