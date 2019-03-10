package com.polestar.ad;

/**
 * Created by guojia on 2019/3/9.
 */

public interface IAdEventLogger {
    void trackEvent(String slot, String event);
}
