package com.witter.msg;

public class Sender {

    static {
        System.loadLibrary("wittermsg");
    }

    private static native String send(String str);
    public static String Send(String str) {
        synchronized (Sender.class) {
            return send(str);
        }
    }


    private static native String receive(String str);
    public static String Receive(String str) {
        synchronized (Sender.class) {
            return receive(str);
        }
    }


    /**
     * @param context
     * @return 1 : pass ， -1 or  -2 : error.
     */
    public static native int check(Object context);

    private static native String ssend(Object context, String str);
    public static String Ssend(Object context, String str) {
        synchronized (Sender.class) {
            return ssend(context, str);
        }
    }


    private static native String rreceive(Object context, String str);
    public static String Rreceive(Object context, String str) {
        synchronized (Sender.class) {
            return rreceive(context, str);
        }
    }
}
