package com.polestar.clone;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import com.polestar.clone.helper.compat.ContentProviderCompat;
import com.polestar.clone.helper.utils.VLog;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by guojia on 2017/7/1.
 */

public class ProxyCP extends ContentProvider {
    private static final String TAG = "ProxyCP";
    @Override
    public boolean onCreate() {
        VLog.d(TAG, "onCreate");
        return true;
    }

    private Uri getOriginUri(Uri uri) {
        if (uri != null) {
            List<String> segs = uri.getPathSegments();
            Uri.Builder builder = new Uri.Builder().scheme("content");
            if (segs.size() > 1) {
                builder.authority(segs.get(0));
            }
            for(int i = 1; i < segs.size(); i ++) {
                builder.appendPath(segs.get(i));
            }
            Uri newUri = builder.build();
            VLog.d(TAG, "new uri: " + newUri);
            return newUri;
        }
        return null;
    }
    
    @Override
    public Cursor query( Uri uri, String[] strings, String s, String[] strings1, String s1) {
        Uri origin = getOriginUri(uri);
        ContentProviderClient client = ContentProviderCompat.crazyAcquireContentProvider(getContext(), origin);
        try {
            return client.query(uri,strings, s, strings1, s1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Cursor() {
            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public int getPosition() {
                return 0;
            }

            @Override
            public boolean move(int i) {
                return false;
            }

            @Override
            public boolean moveToPosition(int i) {
                return false;
            }

            @Override
            public boolean moveToFirst() {
                return false;
            }

            @Override
            public boolean moveToLast() {
                return false;
            }

            @Override
            public boolean moveToNext() {
                return false;
            }

            @Override
            public boolean moveToPrevious() {
                return false;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean isBeforeFirst() {
                return false;
            }

            @Override
            public boolean isAfterLast() {
                return false;
            }

            @Override
            public int getColumnIndex(String s) {
                return 0;
            }

            @Override
            public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
                return 0;
            }

            @Override
            public String getColumnName(int i) {
                return null;
            }

            @Override
            public String[] getColumnNames() {
                return new String[0];
            }

            @Override
            public int getColumnCount() {
                return 0;
            }

            @Override
            public byte[] getBlob(int i) {
                return new byte[0];
            }

            @Override
            public String getString(int i) {
                return null;
            }

            @Override
            public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {

            }

            @Override
            public short getShort(int i) {
                return 0;
            }

            @Override
            public int getInt(int i) {
                return 0;
            }

            @Override
            public long getLong(int i) {
                return 0;
            }

            @Override
            public float getFloat(int i) {
                return 0;
            }

            @Override
            public double getDouble(int i) {
                return 0;
            }

            @Override
            public int getType(int i) {
                return 0;
            }

            @Override
            public boolean isNull(int i) {
                return false;
            }

            @Override
            public void deactivate() {

            }

            @Override
            public boolean requery() {
                return false;
            }

            @Override
            public void close() {

            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void registerContentObserver(ContentObserver contentObserver) {

            }

            @Override
            public void unregisterContentObserver(ContentObserver contentObserver) {

            }

            @Override
            public void registerDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public void setNotificationUri(ContentResolver contentResolver, Uri uri) {

            }

            @Override
            public Uri getNotificationUri() {
                return null;
            }

            @Override
            public boolean getWantsAllOnMoveCalls() {
                return false;
            }

            @Override
            public void setExtras(Bundle bundle) {

            }

            @Override
            public Bundle getExtras() {
                return null;
            }

            @Override
            public Bundle respond(Bundle bundle) {
                return null;
            }
        };
    }

    @Override
    public String getType( Uri uri) {
        Uri origin = getOriginUri(uri);
        ContentProviderClient client = ContentProviderCompat.crazyAcquireContentProvider(getContext(), origin);
        try {
            return client.getType(origin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Uri insert( Uri uri, ContentValues contentValues) {
        Uri origin = getOriginUri(uri);
        ContentProviderClient client = ContentProviderCompat.crazyAcquireContentProvider(getContext(), origin);
        try {
            return client.insert(origin,contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int delete( Uri uri, String s, String[] strings) {
        Uri origin = getOriginUri(uri);
        ContentProviderClient client = ContentProviderCompat.crazyAcquireContentProvider(getContext(), origin);
        try {
            return client.delete(origin,s,strings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int update( Uri uri, ContentValues contentValues, String s, String[] strings) {
        Uri origin = getOriginUri(uri);
        ContentProviderClient client = ContentProviderCompat.crazyAcquireContentProvider(getContext(), origin);
        try {
            return client.update(origin, contentValues,s,strings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AssetFileDescriptor openAssetFile( Uri uri,  String mode) throws FileNotFoundException {
        Uri origin = getOriginUri(uri);
        ContentProviderClient client = ContentProviderCompat.crazyAcquireContentProvider(getContext(), origin);
        try {
            return client.openAssetFile(origin, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public AssetFileDescriptor openAssetFile( Uri uri,  String mode, CancellationSignal signal) throws FileNotFoundException {
        Uri origin = getOriginUri(uri);
        ContentProviderClient client = ContentProviderCompat.crazyAcquireContentProvider(getContext(), origin);
        try {
            return client.openAssetFile(origin, mode,signal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ParcelFileDescriptor openFile( Uri uri,  String mode) throws FileNotFoundException {
        Uri origin = getOriginUri(uri);
        ContentProviderClient client = ContentProviderCompat.crazyAcquireContentProvider(getContext(), origin);
        try {
            return client.openFile(origin, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    
    @Override
    public ParcelFileDescriptor openFile( Uri uri,  String mode, CancellationSignal signal) throws FileNotFoundException {
        Uri origin = getOriginUri(uri);
        ContentProviderClient client = ContentProviderCompat.crazyAcquireContentProvider(getContext(), origin);
        try {
            return client.openFile(origin, mode, signal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        VLog.d(TAG, "method: " + method + " arg: " + arg);
        return super.call(method, arg, extras);
    }
}
