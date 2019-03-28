package com.polestar.ad.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mopub.common.UrlAction;
import com.mopub.common.UrlHandler;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.AdViewBinder;
import com.polestar.imageloader.widget.BasicLazyLoadImageView;
import com.polestar.task.database.DatabaseApi;
import com.polestar.task.database.DatabaseImplFactory;
import com.polestar.task.database.datamodels.AdTask;
import com.polestar.task.network.datamodels.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by guojia on 2017/5/31.
 */

public class PoleNativeAdapter extends AdAdapter {
    private AdTask adTask;
    private DatabaseApi databaseApi;
    private static Handler sWorkHandler;
    private Handler mainHandler;
    private static final String GPID_MACRO = "{gpid}";
    private static final String USERID_MACRO = "{userid}";
    private static final String TASK_ID = "{taskid}";
    private static final String AD_ID = "{adid}";
    private Context appContext;
    private String TAG = PoleNativeAdapter.class.getSimpleName();
    private static String gpid;

    public PoleNativeAdapter(Context context, String adUnit) {
       init(context, adUnit);
    }

    private boolean hasTrackImpression;

    private void init(Context context, String adUnit) {
        mKey = adUnit;
        databaseApi = DatabaseImplFactory.getDatabaseApi(context);
        AdLog.d("databaseApi: " + databaseApi);
        if (sWorkHandler == null) {
            HandlerThread thread = new HandlerThread("pole_ad_loader");
            thread.start();
            sWorkHandler = new Handler(thread.getLooper());
        }
        mainHandler = new Handler(Looper.getMainLooper());
        appContext = context.getApplicationContext();
        hasTrackImpression = false;
    }

    private PoleNativeAdapter(Context context, String adUnit, AdTask task) {
        init(context, adUnit);
        adTask = task;
    }

    @Override
    public String getBody() {
        return adTask!= null? adTask.adDesc: "";
    }

    @Override
    public String getCoverImageUrl() {
        return adTask!= null? adTask.imageUrl: "";
    }

    @Override
    public String getIconImageUrl() {
        return adTask!= null? adTask.iconUrl: "";
    }

    //Steps to fulfill the task
    @Override
    public String getSubtitle() {
        return adTask!= null? adTask.mDescription: "";
    }

    @Override
    public String getTitle() {
        return adTask!= null? adTask.mTitle: "";
    }

    @Override
    public String getCallToActionText() {
        return adTask!= null? adTask.ctaText: "";
    }

    @Override
    public String getId() {
        return adTask!= null? adTask.adid: "";
    }

    @Override
    public void registerViewForInteraction(View view) {
        super.registerViewForInteraction(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adListener != null ) {
                    adListener.onAdClicked(PoleNativeAdapter.this);
                }
                if (!TextUtils.isEmpty(adTask.clickUrl)){
                    goUrl(appContext, replaceMacro(adTask.clickUrl), true);
                }
                FuseAdLoader.notifyAdTaskClicked(adTask);
                trackClick();
            }
        });
        if (!hasTrackImpression) {
            if (!TextUtils.isEmpty(adTask.impUrl)) {
                goUrl(appContext, replaceMacro(adTask.impUrl), false);
        //            TrackingRequest.makeTrackingHttpRequest(replaceMacro(adTask.impUrl), appContext, new TrackingRequest.Listener() {
        //                @Override
        //                public void onResponse(@NonNull String url) {
        //                    AdLog.d(TAG, "Imp onResponse " +url);
        //                }
        //
        //                @Override
        //                public void onErrorResponse(VolleyError volleyError) {
        //                    AdLog.d(TAG, "Imp onErrorResponse " +volleyError.toString());
        //                }
        //            });
            }

            trackImpression();
            hasTrackImpression = true;
        }
        adTask.updateShowTime(appContext);
    }

    private void jumpToUrl(Context context, String url){
        if (!TextUtils.isEmpty(url)) {
            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(viewIntent);
        }
    }

    public static void goUrl(Context context, String url, boolean userInteraction) {
        AdLog.d("usr: " + userInteraction + " goUrl: " + url);
        UrlHandler.Builder builder = new UrlHandler.Builder();

        builder.withSupportedUrlActions(
                UrlAction.OPEN_NATIVE_BROWSER,
                UrlAction.OPEN_IN_APP_BROWSER,
                UrlAction.OPEN_APP_MARKET)
                .withResultActions(new UrlHandler.ResultActions() {
                    @Override
                    public void urlHandlingSucceeded(@NonNull String url,
                                                     @NonNull UrlAction urlAction) {
                        AdLog.d("urlHandlingSucceeded " + urlAction.name());
                    }

                    @Override
                    public void urlHandlingFailed(@NonNull String url,
                                                  @NonNull UrlAction lastFailedUrlAction) {
                        AdLog.d("urlHandlingFailed " + lastFailedUrlAction.name());

                    }
                })
                .build().handleUrl(context, url, userInteraction);
    }

    @Override
    public void registerPrivacyIconView(View view) {

    }

    private String replaceMacro(String input) {
        String ret = new String(input.toCharArray());
        ret.replace(GPID_MACRO, gpid == null? "":gpid);
        if (databaseApi == null) {
            AdLog.d("null database api");
        }
        ret.replace(USERID_MACRO, databaseApi.getMyUserInfo().mDeviceId);
        ret.replace(TASK_ID, ""+adTask.mId);
        ret.replace(AD_ID, adTask.adid);
        return ret;
    }

    private List<AdTask> sortAndFilterAdTask(List<Task> taskList, int num) {
        List<AdTask> retList = new ArrayList<>();
        if (taskList == null || taskList.size() == 0) {
            return retList;
        }
        for (Task adTask: taskList) {
            AdTask ad = adTask.getAdTask();
            if (ad != null && ad.canFillToSlot(appContext, mKey)) {
                retList.add(ad);
            }
        }

        if (retList.size() > 1) {
            Collections.sort(retList, new Comparator<AdTask>() {
                @Override
                public int compare(AdTask o1, AdTask o2) {
                    if (o1.getShowTime(appContext) == o2.getShowTime(appContext)) {
                        if (o1.priority == o2.priority) {
                            return  0;
                        } else {
                            return o1.priority > o2.priority ? -1 : 1;
                        }
                    } else {
                       return o1.getShowTime(appContext) < o2.getShowTime(appContext)? -1 : 1;
                    }
                }
            });
        }

        return retList;
    }

    @Override
    public void loadAd(final Context context, final int num, IAdLoadListener listener) {
        adListener = listener;
        AdLog.d("Pole loadAd " + listener);
        if (databaseApi == null || !databaseApi.isDataAvailable()) {
            if (adListener != null) {
                adListener.onError("No Data");
            }
            return;
        }
        sWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(gpid)) {
                    gpid = AdUtils.getGoogleAdvertisingId(appContext);
                }
                List<Task> taskList = databaseApi.getActiveTasksByType(Task.TASK_TYPE_AD_TASK);
                if (taskList == null || taskList.size() == 0 ){
                    if (adListener != null) {
                        adListener.onError("No Fill");
                    }
                }
                final List<AdTask> adTaskList = sortAndFilterAdTask(taskList, num);
                if (adTaskList == null || adTaskList.size() == 0) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (adListener != null) {
                                adListener.onError("No Fill");
                            }
                        }
                    });
                } else {
                    adTask = adTaskList.get(0);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mLoadedTime = System.currentTimeMillis();
                            if (adListener != null) {
                                if (num <= 1) {
                                    adListener.onAdLoaded(PoleNativeAdapter.this);
                                } else  {
                                    ArrayList<IAdAdapter> list = new ArrayList<>(Math.min(num, adTaskList.size()));
                                    list.add(PoleNativeAdapter.this);
                                    for (int i = 1; i < num && i < adTaskList.size(); i ++) {
                                        list.add(new PoleNativeAdapter(appContext, mKey, adTaskList.get(i)));
                                    }
                                    adListener.onAdListLoaded(list);
                                }
                            }
                        }
                    });
                }
                stopMonitor();
            }
        });
        startMonitor();
    }


    @Override
    public String getAdType() {
        return AdConstants.AdType.AD_SOURCE_POLE_NATIVE;
    }

    @Override
    public Object getAdObject() {
        return adTask;
    }

    @Override
    public View getAdView(Context context, AdViewBinder viewBinder) {
        View inflateView = LayoutInflater.from(context).inflate(viewBinder.layoutId, null);
        TextView cta = inflateView.findViewById(viewBinder.callToActionId);
        if (cta != null) {
            cta.setText(getCallToActionText());
        }

        TextView title = inflateView.findViewById(viewBinder.titleId);
        if (title != null) {
            title.setText(getTitle());
        }

        TextView body = inflateView.findViewById(viewBinder.textId);
        if (body != null) {
            body.setText(getBody());
        }

        BasicLazyLoadImageView iconView = inflateView.findViewById(viewBinder.iconImageId);
        if (iconView instanceof BasicLazyLoadImageView) {
            iconView.setDefaultResource(0);
            iconView.requestDisplayURL(getIconImageUrl());
        }
        BasicLazyLoadImageView coverView = inflateView.findViewById(viewBinder.mainMediaId);
        if (coverView instanceof BasicLazyLoadImageView) {
            coverView.setDefaultResource(0);
            coverView.requestDisplayURL(getCoverImageUrl());
        }
        registerViewForInteraction(inflateView);
        return inflateView;
    }

    @Override
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }
}
