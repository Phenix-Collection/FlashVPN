package com.polestar.grey;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by eepaul on 05/07/2018.
 */

public class NormMsg {
    private static final String TAG = "NormMsg";
    private static boolean isFirstCall = true;

    public static long getRawEnvBits() {
        int envbits = 0;
        /*
            bit26 在oppo上为1， qemu也为1

            decode结果
            honor       40732577    0x2000001
                        48566057    0x2000009

            regOnVmBad  116033441   0x6870001
                        103205861   0x6870005
                        107089705   0x6870009
                        116033441   0x6870001
                        107089705   0x6870009

            nexus       2704237     0x000000d
            oppo        74287009    0x4000001

         */
        if (isFirstCall) {
            isFirstCall = false;
            envbits |= (1<<26);

        }
        if (new File("/sys/bus/virtio").exists()) {
            envbits |= (1<<25);
        }

        if (new File("/sys/module/virtio_net").exists()) {
            envbits |= (1<<24);
        }

        if (new File("/sys/module/virtio_pci").exists()) {
            envbits |= (1<<23);
        }

        if (new File("/sys/class/virtio_pt/virtiopt").exists()) {
            envbits |= (1<<22);
        }

        if (new File("/sys/devices/virtual/virtio_pt/virtiopt").exists()) {
            envbits |= (1<<21);
        }

        if (new File("/sys/class/virtio_pt").exists()) {
            envbits |= (1<<20);
        }

        if (new File("/dev/virtiopt").exists()) {
            envbits |= (1<<19);
        }

        if (new File("/sys/bus/pci/drivers/virtio-pci").exists()) {
            envbits |= (1<<18);
        }

        if (new File("/proc/sys/fs/binfmt_misc/arm").exists()) {
            envbits |= (1<<10);
        }

        if (isStrsInFile("/proc/bus/pci/devices", new String[]{"virtio"})) {
            envbits |= (1<<17);
        }

        if (isStrsInFile("/proc/interrupts", new String[]{"virtio"})) {
            envbits |= (1<<16);

        }

        if (isStrsInFile("/proc/diskstats", new String[]{"sda", "sdb"})) {
            envbits |= (1<<15);
        }

        if (isStrsInFile("/proc/filesystems", new String[] {"vboxsf"})) {
            envbits |= (1<<14);
        }

        if (isStrsInFile("/proc/self/maps", new String[]{"/system/lib/arm"})) {
            envbits |= (1<<9);
        }

        Log.d(TAG, String.format("getEnvBits raw bits:0x%x", envbits));
        return envbits;

        /*
        usec:0, env:7178145
usec:1, env:7178145
usec:2, env:993603
usec:3, env:993603
usec:4, env:10603493
usec:5, env:10603493
usec:6, env:4418951
usec:7, env:4418951
usec:8, env:15011625
usec:9, env:15011625
usec:10, env:8888779
usec:11, env:8888779
usec:12, env:2704237
usec:13, env:2704237
usec:14, env:13362447
usec:15, env:13362447

         */
    }

    public static boolean isStrsInFile(String path, String[] needles) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            char [] buf = new char[2048];
            int ret = 0;
            String result = "";
            while (ret != -1) {
                ret = br.read(buf, 0, 2048);
                if (ret > 0) {
                    result += new String(buf, 0, ret);
                }
            }

            for (String n : needles) {
                if (result.contains(n))
                    return true;
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "isStrInFile ex", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {

                }
            }
        }

        return false;
    }

    public static void getCheckSoftType4Info(Context context, String[] result) {
        BufferedReader br = null;
        final int minorbits = 8;
        Set<String> resultCommon = new TreeSet<>();
        Set<String> resultApp = new TreeSet<>();
        try {

            //TODO
            android.system.StructStat stat = android.system.Os.stat(context.getApplicationInfo().sourceDir);
            long stDev = stat.st_dev;
            Log.d(TAG, "android os stat stdev:" + stDev);
//            long stDev = Stat.getStdev(context.getApplicationInfo().sourceDir);
//            Log.d(TAG, "our own stat stdev:" + stDev);

            int major = (int)(stDev >> minorbits);
            int minor = (int)(((1 << minorbits) - 1) & stDev);
            String devStr = String.format("%02x:%02x", major, minor);
            br = new BufferedReader(new FileReader("/proc/self/maps"));
            String line = "";
            while (line != null) {
                line = br.readLine();
                if (line != null) {
                    Log.e(TAG, "maps:" + line);
                    String[] cols = line.split("\\s+");
                    if (cols.length > 5) {
                        if (!cols[5].startsWith("[") &&
                                !cols[5].matches(".*libxposed_.*\\.so") &&
                                cols[1].contains("x")) {
                            if ((cols[3].equals(devStr) || cols[3].equals("00:01")))
                                resultApp.add(cols[5]);
                            else
                                resultCommon.add(cols[5]);
                        }
                    }
                }
            }

            result[0] = set2Str(resultCommon);
            result[1] = set2Str(resultApp);

        } catch (Exception e) {
            Log.e(TAG, "simCheckSoftType4 ex", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public static String set2Str(Set<String> s) {
        String r = "";
        for (String x : s) {
            r += (x + ",");
        }

        if (!TextUtils.isEmpty(r))
            r = r.substring(0, r.length()-1);

        return r;
    }
}
