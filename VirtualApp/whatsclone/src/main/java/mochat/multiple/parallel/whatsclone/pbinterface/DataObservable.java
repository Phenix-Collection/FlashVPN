package mochat.multiple.parallel.whatsclone.pbinterface;

/**
 * Created by hxx on 9/7/16.
 */
public interface DataObservable<T> {
    void registerObserver(T observer);

    void unregisterObserver(T observer);

    void unregisterAll();

    void notifyChanged();

    void notifyInvalidated();
}
