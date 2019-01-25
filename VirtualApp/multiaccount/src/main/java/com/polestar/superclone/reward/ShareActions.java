package com.polestar.superclone.reward;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.MLogs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

/**
 * Created by guojia on 2019/1/24.
 */

public class ShareActions {
    private static Activity mCurrentActivity;

    public ShareActions(Activity arg2) {
        super();
        ShareActions.mCurrentActivity = arg2;
    }

    public static boolean appInstalledOrNot(Context arg4, String arg5) {
        boolean v2;
        PackageManager v1 = arg4.getPackageManager();
        try {
            v1.getPackageInfo(arg5, PackageManager.GET_ACTIVITIES);
            v2 = true;
        }
        catch(PackageManager.NameNotFoundException v3) {
            v2 = false;
        }

        return v2;
    }

    public static String getMyInviteUrl() {
        //TODO get my invitecode

        return "http://abo.io/12234ssdf";
    }

    public static void shareFacebook() {
//        AccessToken v4 = AccessToken.getCurrentAccessToken();
//        String v5 = ShareActions.getMyInviteUrl();
//        if(v4 == null || (v4.isExpired())) {
//            ShareActions.mCurrentActivity.openFacebookSessionAndShare(v5);
//        }
//        else {
//            ShareActions.mCurrentActivity.shareFb(v5);
//        }
    }

    public static void shareMail() {
//        if(DroidBountyApplication.getAppUser() != null) {
//            String v4 = "Check out AppBounty. You can get free gift cards and other rewards just for trying out free apps.\n" + "If you enter my invite code or follow my invite link you will get 50 free credits to begin.\n\n" + "Invite link: " + ShareActions.getMyInviteUrl() + "\n" + "Invite code: " + DroidBountyApplication.getAppUser().getInviteCode() + "\n\n";
//            Intent v5 = new Intent("android.intent.action.SEND");
//            v5.setType("message/rfc822");
//            v5.putExtra("android.intent.extra.EMAIL", new String[]{""});
//            v5.putExtra("android.intent.extra.SUBJECT", "Earn free Gift Cards for trying free apps");
//            v5.putExtra("android.intent.extra.TEXT", v4);
//            try {
//                ShareActions.mCurrentActivity.startActivity(Intent.createChooser(v5, "Send mail..."));
//            }
//            catch(ActivityNotFoundException v6) {
//                Toast.makeText(ShareActions.mCurrentActivity, "There are no email clients installed.", 0).show();
//            }
//        }
    }

    public static void shareTwitter() {
        Intent shareIntent = new Intent("android.intent.action.SEND");
        shareIntent.putExtra("android.intent.extra.TEXT", "get free #iTunes, #Amazon, #Xbox and other gift cards with @AppBounty. Use my link for a bonus: " + ShareActions.getMyInviteUrl());
        shareIntent.setType("application/twitter");
        PackageManager packageManager = ShareActions.mCurrentActivity.getPackageManager();
        int v7 = 0;
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
        ResolveInfo resolved = null;
        if (resolveInfos != null && resolveInfos.size() > 0) {
            for(ResolveInfo ri : resolveInfos) {
                if (ri.activityInfo.name.endsWith(".SendTweet")) {
                    resolved = ri;
                }
            }
        }
        if (resolved != null) {
            shareIntent.setClassName(resolved.activityInfo.packageName, resolved.activityInfo.name);
        } else {
            shareIntent = new Intent("android.intent.action.VIEW",
                    Uri.parse("https://twitter.com/intent/tweet?text=" + ShareActions.urlEncode("get free #iTunes, #Amazon, #Xbox and other gift cards with @AppBounty. Use my link for a bonus: " + ShareActions.getMyInviteUrl())));
            List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(shareIntent, 0);
            if (resolveInfoList != null && resolveInfoList.size() > 0) {
                for (ResolveInfo ri: resolveInfoList) {
                    if(ri.activityInfo.packageName.toLowerCase(Locale.getDefault()).startsWith("com.twitter")) {
                        shareIntent.setPackage(ri.activityInfo.packageName);
                        break;
                    }
                }
            }
        }
        try {
            ShareActions.mCurrentActivity.startActivity(shareIntent);
            EventReporter.generalEvent("share_twitter_ok");
        }catch (Throwable ex){
            MLogs.e("share Twitter error");
            EventReporter.generalEvent("share_twitter_fail");
        }
    }

    public static void shareWhatsApp() {
//        if(ShareActions.appInstalledOrNot(ShareActions.mCurrentActivity, "com.whatsapp")) {
            String v5 = "Get free Amazon, Xbox, Steam and other gift cards with AppBounty. Use my link for a bonus: " + ShareActions.getMyInviteUrl();
            Intent v6 = new Intent("android.intent.action.SEND");
            v6.setType("text/plain");
            v6.putExtra("android.intent.extra.TEXT", v5);
            v6.setPackage("com.whatsapp");
            try {
                EventReporter.generalEvent("share_whatsapp_ok");
                ShareActions.mCurrentActivity.startActivity(v6);
            }
            catch(ActivityNotFoundException v7) {
                EventReporter.generalEvent("share_whatsapp_fail");
                MLogs.e("WhatsApp not installed!");
            }
//        }
    }

    private static String urlEncode(String arg4) {
        String v0 = "UTF-8";
        try {
            return URLEncoder.encode(arg4, v0);
        }
        catch(UnsupportedEncodingException v3) {
            MLogs.e("InviteFragment", "UTF-8 should always be supported" + v3.getLocalizedMessage());
            throw new RuntimeException("URLEncoder.encode() failed for " + arg4);
        }
    }

}
