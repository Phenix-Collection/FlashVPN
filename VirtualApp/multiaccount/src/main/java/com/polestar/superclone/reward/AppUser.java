package com.polestar.superclone.reward;

/**
 * Created by guojia on 2019/1/24.
 */

public class AppUser {

    private String mInviteCode;
    private float mBalance;
    private String mId;
    private static AppUser sAppUser = null;

    private AppUser() {

    }

    private void init() {

    }

    synchronized public static AppUser getInstance() {
        if (sAppUser == null) {
            sAppUser = new AppUser();
            sAppUser.init();
        }
        return sAppUser;
    }

    public String getInviteCode( ) {
        return mInviteCode;
    }

    public float getMyBalance() {
        return mBalance;
    }

    public String getMyId() {
        return mId;
    }
}
