package winterfell.flash.vpn.utils;

import android.content.Context;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
    public static final String FILE_EXTENSION_SEPARATOR = ".";

    private FileUtils() {
        super();
        throw new AssertionError();
    }

//    public static boolean copyFile(String arg3, String arg4) {
//        FileInputStream v0_1;
//        try {
//            v0_1 = new FileInputStream(arg3);
//        }
//        catch(FileNotFoundException v0) {
//            throw new RuntimeException("FileNotFoundException occurred. ", ((Throwable)v0));
//        }
//
//        return FileUtils.writeFile(arg4, ((InputStream)v0_1));
//    }

    public static boolean deleteFile(String arg6) {
        boolean v0 = true;
        int v1 = 0;
        if(!TextUtils.isEmpty(((CharSequence)arg6))) {
            File v2 = new File(arg6);
            if(v2.exists()) {
                if(v2.isFile()) {
                    v0 = v2.delete();
                }
                else if(!v2.isDirectory()) {
                    v0 = false;
                }
                else {
                    File[] v0_1 = v2.listFiles();
                    int v3 = v0_1.length;
                    while(v1 < v3) {
                        File v4 = v0_1[v1];
                        if(v4.isFile()) {
                            v4.delete();
                        }
                        else if(v4.isDirectory()) {
                            FileUtils.deleteFile(v4.getAbsolutePath());
                        }

                        ++v1;
                    }

                    v0 = v2.delete();
                }
            }
        }

        return v0;
    }

    public static String getFileExtension(String arg3) {
        if(!TextUtils.isEmpty(((CharSequence)arg3))) {
            int v0 = arg3.lastIndexOf(".");
            int v1 = arg3.lastIndexOf(File.separator);
            if(v0 == -1) {
                arg3 = "";
            }
            else {
                String v0_1 = v1 >= v0 ? "" : arg3.substring(v0 + 1);
                arg3 = v0_1;
            }
        }

        return arg3;
    }

    public static String getFileName(String arg2) {
        if(!TextUtils.isEmpty(((CharSequence)arg2))) {
            int v0 = arg2.lastIndexOf(File.separator);
            if(v0 != -1) {
                arg2 = arg2.substring(v0 + 1);
            }
        }

        return arg2;
    }

    public static String getFileNameWithoutExtension(String arg3) {
        int v2 = -1;
        if(!TextUtils.isEmpty(((CharSequence)arg3))) {
            int v0 = arg3.lastIndexOf(".");
            int v1 = arg3.lastIndexOf(File.separator);
            if(v1 == v2) {
                if(v0 != v2) {
                    arg3 = arg3.substring(0, v0);
                }
            }
            else if(v0 == v2) {
                arg3 = arg3.substring(v1 + 1);
            }
            else {
                String v0_1 = v1 < v0 ? arg3.substring(v1 + 1, v0) : arg3.substring(v1 + 1);
                arg3 = v0_1;
            }
        }

        return arg3;
    }

    public static long getFileSize(String arg4) {
        long v0 = -1;
        if(!TextUtils.isEmpty(((CharSequence)arg4))) {
            File v2 = new File(arg4);
            if((v2.exists()) && (v2.isFile())) {
                v0 = v2.length();
            }
        }

        return v0;
    }

    public static String getFolderName(String arg2) {
        if(!TextUtils.isEmpty(((CharSequence)arg2))) {
            int v0 = arg2.lastIndexOf(File.separator);
            String v0_1 = v0 == -1 ? "" : arg2.substring(0, v0);
            arg2 = v0_1;
        }

        return arg2;
    }

    public static boolean isFileExist(String arg3) {
        boolean v0 = false;
        if(!TextUtils.isEmpty(((CharSequence)arg3))) {
            File v1 = new File(arg3);
            if((v1.exists()) && (v1.isFile())) {
                v0 = true;
            }
        }

        return v0;
    }

    public static boolean isFolderExist(String arg3) {
        boolean v0 = false;
        if(!TextUtils.isEmpty(((CharSequence)arg3))) {
            File v1 = new File(arg3);
            if((v1.exists()) && (v1.isDirectory())) {
                v0 = true;
            }
        }

        return v0;
    }

    public static boolean makeDirs(String arg2) {
        boolean v0_1;
        String v0 = FileUtils.getFolderName(arg2);
        if(TextUtils.isEmpty(((CharSequence)v0))) {
            v0_1 = false;
        }
        else {
            File v1 = new File(v0);
            if((v1.exists()) && (v1.isDirectory())) {
                return true;
            }

            v0_1 = v1.mkdirs();
        }

        return v0_1;
    }

    public static boolean makeFolders(String arg1) {
        return FileUtils.makeDirs(arg1);
    }

//    public static void moveFile(File arg2, File arg3) {
//        if(!arg2.renameTo(arg3)) {
//            FileUtils.copyFile(arg2.getAbsolutePath(), arg3.getAbsolutePath());
//            FileUtils.deleteFile(arg2.getAbsolutePath());
//        }
//    }

//    public static void moveFile(String arg2, String arg3) {
//        if(!TextUtils.isEmpty(((CharSequence)arg2)) && !TextUtils.isEmpty(((CharSequence)arg3))) {
//            FileUtils.moveFile(new File(arg2), new File(arg3));
//            return;
//        }
//
//        throw new RuntimeException("Both sourceFilePath and destFilePath cannot be null.");
//    }

    public static StringBuilder readFile(String fileName, String arg6) {
        StringBuilder res = new StringBuilder("");
        try {
            FileInputStream fis = new FileInputStream(fileName);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                res.append(line);
                res.append("\r\n");
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return res;
    }

//    public static List readFileToList(String arg5, String arg6) {
//        BufferedReader v2_1;
//        BufferedReader v1 = null;
//        File v2 = new File(arg5);
//        ArrayList v0 = new ArrayList();
//        if(v2 == null || !v2.isFile()) {
//            List v0_3 = ((List)v1);
//        }
//        else {
//            try {
//                v2_1 = new BufferedReader(new InputStreamReader(new FileInputStream(v2), arg6));
//                goto label_16;
//            }
//            catch(Throwable v0_1) {
//            }
//            catch(IOException v0_2) {
//                goto label_22;
//                try {
//                    while(true) {
//                        label_16:
//                        String v1_1 = v2_1.readLine();
//                        if(v1_1 == null) {
//                            break;
//                        }
//
//                        ((List)v0).add(v1_1);
//                    }
//
//                    v2_1.close();
//                    if(v2_1 == null) {
//                        goto label_9;
//                    }
//                }
//                catch(Throwable v0_1) {
//                    v1 = v2_1;
//                    goto label_27;
//                }
//                catch(IOException v0_2) {
//                    v1 = v2_1;
//                    goto label_22;
//                }
//
//                try {
//                    v2_1.close();
//                    goto label_9;
//                }
//                catch(IOException v0_2) {
//                    throw new RuntimeException("IOException occurred. ", ((Throwable)v0_2));
//                }
//
//                try {
//                    label_22:
//                    throw new RuntimeException("IOException occurred. ", ((Throwable)v0_2));
//                }
//                catch(Throwable v0_1) {
//                }
//            }
//
//            label_27:
//            if(v1 != null) {
//                try {
//                    v1.close();
//                }
//                catch(IOException v0_2) {
//                    throw new RuntimeException("IOException occurred. ", ((Throwable)v0_2));
//                }
//            }
//
//            throw v0_1;
//        }
//
//        label_9:
//        return ((List)v0);
//    }
//
    public static String readRawFile(Context arg5, @RawRes int arg6) {
        InputStream v2;
        String v0 = null;
        try {
            v2 = arg5.getResources().openRawResource(arg6);
        }
        catch(Exception v1) {
            v1.printStackTrace();
            v2 = null;
        }

        if(v2 == null) {
            return v0;
        }

        try {
            BufferedReader v1_2 = new BufferedReader(new InputStreamReader(v2));
            StringBuilder v3 = new StringBuilder();
            while(true) {
                String v4 = v1_2.readLine();
                if(v4 == null) {
                    break;
                }

                v3.append(v4);
            }

            v0 = v3.toString();
        }
        catch(Throwable ex) {
          ex.printStackTrace();
        }

        try {
            v2.close();
        }
        catch(IOException v1_1) {
            v1_1.printStackTrace();
        }

        return v0;
    }
//
//    public static boolean writeFile(String arg4, String arg5, boolean arg6) {
//        FileWriter v1;
//        boolean v0;
//        if(TextUtils.isEmpty(((CharSequence)arg5))) {
//            v0 = false;
//        }
//        else {
//            FileWriter v2 = null;
//            try {
//                FileUtils.makeDirs(arg4);
//                v1 = new FileWriter(arg4, arg6);
//            }
//            catch(Throwable v0_1) {
//                v1 = v2;
//                goto label_26;
//            }
//            catch(IOException v0_2) {
//                v1 = v2;
//                goto label_21;
//            }
//
//            try {
//                v1.write(arg5);
//                v1.close();
//                v0 = true;
//                if(v1 == null) {
//                    return v0;
//                }
//
//                goto label_12;
//            }
//            catch(Throwable v0_1) {
//            }
//            catch(IOException v0_2) {
//                try {
//                    label_21:
//                    throw new RuntimeException("IOException occurred. ", ((Throwable)v0_2));
//                }
//                catch(Throwable v0_1) {
//                }
//            }
//
//            label_26:
//            if(v1 != null) {
//                try {
//                    v1.close();
//                }
//                catch(IOException v0_2) {
//                    throw new RuntimeException("IOException occurred. ", ((Throwable)v0_2));
//                }
//            }
//
//            throw v0_1;
//            try {
//                label_12:
//                v1.close();
//            }
//            catch(IOException v0_2) {
//                throw new RuntimeException("IOException occurred. ", ((Throwable)v0_2));
//            }
//        }
//
//        return v0;
//    }
//
//    public static boolean writeFile(String arg1, InputStream arg2) {
//        return FileUtils.writeFile(arg1, arg2, false);
//    }
//
//    public static boolean writeFile(File arg1, InputStream arg2) {
//        return FileUtils.writeFile(arg1, arg2, false);
//    }
//
//    public static boolean writeFile(File arg4, InputStream arg5, boolean arg6) {
//        int v0_3;
//        FileOutputStream v1;
//        FileOutputStream v2 = null;
//        try {
//            FileUtils.makeDirs(arg4.getAbsolutePath());
//            v1 = new FileOutputStream(arg4, arg6);
//            v0_3 = 1024;
//        }
//        catch(Throwable v0) {
//            v1 = v2;
//            goto label_19;
//        }
//        catch(IOException v0_1) {
//            v1 = v2;
//            goto label_36;
//        }
//        catch(FileNotFoundException v0_2) {
//            v1 = v2;
//            goto label_14;
//        }
//
//        try {
//            byte[] v0_4 = new byte[v0_3];
//            while(true) {
//                int v2_1 = arg5.read(v0_4);
//                if(v2_1 == -1) {
//                    break;
//                }
//
//                ((OutputStream)v1).write(v0_4, 0, v2_1);
//            }
//
//            ((OutputStream)v1).flush();
//            if(v1 != null) {
//                goto label_26;
//            }
//
//            return 1;
//        }
//        catch(IOException v0_1) {
//            goto label_49;
//        }
//        catch(Throwable v0) {
//            goto label_19;
//        }
//        catch(FileNotFoundException v0_2) {
//            goto label_14;
//        }
//
//        try {
//            label_26:
//            ((OutputStream)v1).close();
//            arg5.close();
//        }
//        catch(IOException v0_1) {
//            throw new RuntimeException("IOException occurred. ", ((Throwable)v0_1));
//        }
//
//        return 1;
//        try {
//            label_14:
//            throw new RuntimeException("FileNotFoundException occurred. ", ((Throwable)v0_2));
//        }
//        catch(Throwable v0) {
//            goto label_19;
//        }
//
//        label_49:
//        try {
//            label_36:
//            throw new RuntimeException("IOException occurred. ", ((Throwable)v0_1));
//        }
//        catch(Throwable v0) {
//        }
//
//        label_19:
//        if(v1 != null) {
//            try {
//                ((OutputStream)v1).close();
//                arg5.close();
//            }
//            catch(IOException v0_1) {
//                throw new RuntimeException("IOException occurred. ", ((Throwable)v0_1));
//            }
//        }
//
//        throw v0;
//    }
//
//    public static boolean writeFile(String arg1, InputStream arg2, boolean arg3) {
//        File v0 = arg1 != null ? new File(arg1) : null;
//        return FileUtils.writeFile(v0, arg2, arg3);
//    }
//
//    public static boolean writeFile(String arg1, String arg2) {
//        return FileUtils.writeFile(arg1, arg2, false);
//    }
}