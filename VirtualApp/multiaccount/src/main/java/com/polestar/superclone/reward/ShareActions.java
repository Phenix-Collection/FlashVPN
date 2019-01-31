package com.polestar.superclone.reward;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.polestar.superclone.R;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.MLogs;
import com.polestar.task.database.datamodels.ShareTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

/**
 * Created by guojia on 2019/1/24.
 */

public class ShareActions {
    private static Activity mCurrentActivity;
    private String mUrl;
    private String mCode;
    public static final String SOURCE_USER_SHARE = "user_share";

    public ShareActions(Activity arg2) {
        super();
        ShareActions.mCurrentActivity = arg2;
        mUrl = "https://play.google.com/store/apps/details?id=" + arg2.getPackageName() + "&referrer=utm_source%3D"+SOURCE_USER_SHARE;
        mCode = "";
    }

    public ShareActions(Activity arg2, ShareTask task) {
        ShareActions.mCurrentActivity = arg2;
        String source = "";
        if (!task.shareUrl.contains("&referrer=utm_source%3D")){
            source = "&referrer=utm_source%3D" + SOURCE_USER_SHARE;
        }
        mUrl = task.shareUrl + source+"%26utm_content%3D"+AppUser.getInstance().getInviteCode();
        mCode = AppUser.getInstance().getInviteCode();
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

    public String getMyInviteUrl() {

        return mUrl;
    }

    public boolean copy(boolean urlOnly) {
        ClipboardManager v3 = (ClipboardManager) mCurrentActivity.getSystemService(mCurrentActivity.CLIPBOARD_SERVICE);
        ClipData v4 = ClipData.newPlainText("Invite Friend", getCopyText(urlOnly));
        if(v4 != null && v3 != null) {
            ((ClipboardManager) v3).setPrimaryClip(v4);
            return true;
        }
        return false;
    }

    public String getCopyText(boolean urlOnly) {
        return urlOnly? mUrl: "Code: " + mCode + " Download: " + mUrl;
    }

    public void shareFacebook() {
        shareWithFriends("com.facebook.katana");
    }

    public void shareMail() {
        String v4 = getShareContent();
        Uri uri = Uri.parse("mailto:"+"");
        Intent v5 = new Intent(Intent.ACTION_SENDTO, uri);
        v5.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
        v5.putExtra(Intent.EXTRA_SUBJECT, mCurrentActivity.getString(R.string.invite_friends_mail_title, mCurrentActivity.getString(R.string.app_name)));
        v5.putExtra(Intent.EXTRA_TEXT, v4);
        try {
            ShareActions.mCurrentActivity.startActivity(Intent.createChooser(v5, "Send mail..."));
            EventReporter.rewardEvent("share_with_mail");
        }
        catch(ActivityNotFoundException v6) {
            shareWithFriends(null);
            Toast.makeText(ShareActions.mCurrentActivity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void shareWithFriends(String sharePackage) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (!TextUtils.isEmpty(sharePackage)) {
            shareIntent.setPackage(sharePackage);
        }
        shareIntent.setType("text/plain");
        String appName = mCurrentActivity.getResources().getString(R.string.app_name);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName);

        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareContent());
        mCurrentActivity.startActivity(Intent.createChooser(shareIntent, mCurrentActivity.getResources().getText(R.string.share_with_friends)));
        EventReporter.rewardEvent("general_share_"+sharePackage);
    }

    public String getShareContent() {
        String appName = mCurrentActivity.getResources().getString(R.string.app_name);
        String shareContent = mCurrentActivity.getResources().getString(R.string.invite_friends_tip, appName, mCode, mUrl);
        return shareContent;
    }

    public void shareTwitter() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareContent());
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
            shareIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/intent/tweet?text=" + ShareActions.urlEncode(getShareContent())));
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
            EventReporter.rewardEvent("share_twitter_ok");
        }catch (Throwable ex){
            MLogs.e("share Twitter error");
            EventReporter.rewardEvent("share_twitter_fail");
            shareWithFriends(null);
        }
    }

    public void shareWhatsApp() {
        shareWithFriends("com.whatsapp");
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
