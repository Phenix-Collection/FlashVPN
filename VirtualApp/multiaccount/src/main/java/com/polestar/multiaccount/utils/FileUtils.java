package com.polestar.multiaccount.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by yxx on 2016/7/22.
 */
public class FileUtils {

    /**
     * 获取可用的文件目录 优先/sdcard/Android/data/mypacketname/file
     * 不可用时返回/data/data/mypacketname/file
     */
    public static String getFileDir(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = context.getExternalFilesDir(null);
            if (file != null) {
                return file.getAbsolutePath();
            }
        }
        return context.getFilesDir().getAbsolutePath();
    }

    public static String readFromFile(String path) {
        StringBuffer sb = new StringBuffer();
        String line;
        BufferedReader reader = null;

        try {
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void writeToFile(String path, String str) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(str.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

}
