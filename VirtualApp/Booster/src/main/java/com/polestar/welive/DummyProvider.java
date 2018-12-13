package com.polestar.welive;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by guojia on 2018/12/12.
 */

public class DummyProvider extends ContentProvider {
        public DummyProvider() {
            super();
        }

        public int delete(Uri arg2, String arg3, String[] arg4) {
            return 0;
        }

        public String getType(Uri arg2) {
            return null;
        }

        public Uri insert(Uri arg2, ContentValues arg3) {
            return null;
        }

        public boolean onCreate() {
            return true;
        }

        public Cursor query(Uri arg2, String[] arg3, String arg4, String[] arg5, String arg6) {
            return null;
        }

        public int update(Uri arg2, ContentValues arg3, String arg4, String[] arg5) {
            return 0;
        }
}
