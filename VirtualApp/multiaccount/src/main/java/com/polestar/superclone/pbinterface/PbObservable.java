package com.polestar.superclone.pbinterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hxx on 9/8/16.
 */
public class PbObservable implements DataObservable<DataObserver> {

    private List<DataObserver> mObservers = new ArrayList<>();

    @Override
    public void registerObserver(DataObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized (mObservers) {
            if (mObservers.contains(observer)) {
//                throw new IllegalStateException("Observer " + observer + " is already registered.");
            } else {
                mObservers.add(observer);
            }
        }
    }

    @Override
    public void unregisterObserver(DataObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized (mObservers) {
            int index = mObservers.indexOf(observer);
            if (index == -1) {
//                throw new IllegalStateException("Observer " + observer + " was not registered.");
            } else {
                mObservers.remove(index);
            }
        }
    }

    @Override
    public void unregisterAll() {
        synchronized (mObservers) {
            mObservers.clear();
        }
    }

    @Override
    public void notifyChanged() {
        synchronized (mObservers) {
            for (DataObserver observer : mObservers) {
                observer.onChanged();
            }
        }
    }

    @Override
    public void notifyInvalidated() {
        synchronized (mObservers) {
            for (DataObserver observer : mObservers) {
                observer.onInvalidated();
            }
        }
    }
}
