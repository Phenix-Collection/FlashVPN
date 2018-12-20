package com.polestar.clone.client.badger;

import android.content.Intent;

import com.polestar.clone.remote.BadgerInfo;

/**
 * @author Lody
 */
public interface IBadger {

    String getAction();

    BadgerInfo handleBadger(Intent intent);

}
