package com.polestar.booster.util;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public final class IOUtil {
    private static final int COPY_BUF_SIZE = 8024;
    private static final int SKIP_BUF_SIZE = 4096;
    private static final byte[] SKIP_BUF = new byte[63];

    private IOUtil() {
    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, 8024);
    }

    public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long count = 0L;

        int read1;
        for(boolean read = false; (read1 = input.read(buffer)) > 0; count += (long)read1) {
            output.write(buffer, 0, read1);
        }

        return count;
    }

    public static long skip(InputStream is, long skip) throws IOException {
        long dest;
        long read;
        for(dest = skip; skip > 0L; skip -= read) {
            read = is.skip(skip);
            if(read == 0L) {
                break;
            }
        }

        while(skip > 0L) {
            int read1 = readFully(is, SKIP_BUF, 0, (int)Math.min(skip, 4096L));
            if(read1 < 1) {
                break;
            }

            skip -= (long)read1;
        }

        return dest - skip;
    }

    public static int readFully(InputStream is, byte[] buffer) throws IOException {
        return readFully(is, buffer, 0, buffer.length);
    }

    public static int readFully(InputStream is, byte[] buffer, int offset, int len) throws IOException {
        if(len >= 0 && offset >= 0 && len + offset <= buffer.length) {
            int count = 0;

            int x1;
            for(boolean x = false; count != len; count += x1) {
                x1 = is.read(buffer, offset + count, len - count);
                if(x1 == -1) {
                    break;
                }
            }

            return count;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public static byte[] toBytes(InputStream is) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(is, output);
        return output.toByteArray();
    }

    public static byte[] toBytes(String string, String encoding) throws IOException {
        return string.getBytes(encoding);
    }

    public static byte[] toBytes(String string) throws IOException {
        return toBytes(string, "utf-8");
    }

    public static byte[] toBytes(File file) throws IOException {
        if(file != null && file.exists() && !file.isDirectory()) {
            FileInputStream fis = new FileInputStream(file);

            byte[] var3;
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream((int)file.length());
                copy(fis, bos);
                bos.close();
                var3 = bos.toByteArray();
            } finally {
                closeQuietly(fis);
            }

            return var3;
        } else {
            throw new IOException("param is error!");
        }
    }

    public static void closeQuietly(Object c) {
        if(c != null) {
            try {
                if(c instanceof Closeable) {
                    ((Closeable)c).close();
                    return;
                }

                if(c instanceof Cursor) {
                    ((Cursor)c).close();
                    return;
                }

                if(c instanceof SQLiteDatabase) {
                    ((SQLiteDatabase)c).close();
                }

                Method e = c.getClass().getDeclaredMethod("close", new Class[0]);
                e.invoke(c, new Object[0]);
            } catch (Throwable var2) {
                var2.printStackTrace();
            }

        }
    }
}
