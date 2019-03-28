package com.polestar.domultiple.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.clone.Build;
import com.polestar.domultiple.BuildConfig;
import com.polestar.domultiple.R;
import com.polestar.domultiple.components.ui.HomeActivity;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.clone.CustomizeAppData;
import com.polestar.domultiple.task.IconAdConfig;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.RemoteConfig;
import com.polestar.task.database.datamodels.AdTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PolestarApp on 2017/7/16.
 */
public class HomeGridAdapter extends BaseAdapter {
    
    private List<CloneModel> appInfos;
    private boolean showLucky;
    private Context mContext;
    private static final int MIN_GRID_SIZE = 6;

    private IconAdConfig adConfig;
    private IAdAdapter iconAd;
    private static final String SLOT_ICON_AD = "slot_app_icon";

    public void setShowLucky(boolean s) {
        showLucky = s;
    }

    private Handler mainHandler;
    private boolean iconAdIgnored;

    public static class HomeItem {
        public final static int TYPE_ADD = 0;
        public final static int TYPE_CLONE = 1;
        public final static int TYPE_LUCKY = 2;
        public final static int TYPE_ICON_AD = 3;
        public int type;
        public Object obj;
        public View inflateView;

        public HomeItem(int type, Object obj) {
            this.type = type;
            this.obj = obj;
        }
    }

    public HomeGridAdapter(Context context) {
        super();
        mContext = context;
        mainHandler = new Handler(Looper.getMainLooper());
        adConfig = new IconAdConfig();
        appInfos = new ArrayList<>(0);
    }


    private boolean needIconAd() {
        if (iconAdIgnored) {
            return false;
        }
        if (appInfos.size() > adConfig.cloneThreshold ) {
            long installTime = CommonUtils.getInstallTime(mContext, mContext.getPackageName());
            if (System.currentTimeMillis() - installTime > adConfig.showAfterInstall) {
                return true;
            }
        }
        return BuildConfig.DEBUG;
    }

    public void ignoreIconAd()  {
        if (iconAd != null ) {
            ((AdTask) iconAd.getAdObject()).ignoreFor(mContext, SLOT_ICON_AD, adConfig.ignoreInterval);
            iconAd = null;
            iconAdIgnored = true;
            notifyDataSetChanged();
        }
    }
    public void notifyDataSetChanged(List<CloneModel> list) {
        appInfos = list;
        if (iconAd == null && needIconAd()) {
            FuseAdLoader.get(SLOT_ICON_AD, mContext).loadAd(mContext, 1, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    iconAd = ad;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onAdClicked(IAdAdapter ad) {

                }

                @Override
                public void onAdClosed(IAdAdapter ad) {

                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {

                }

                @Override
                public void onRewarded(IAdAdapter ad) {

                }
            });
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int size = appInfos == null ? 0 : appInfos.size();
        if (showLucky) {
            size ++;
        }
        if (iconAd != null) {
            size ++;
        }
        if ( size < MIN_GRID_SIZE ) {
            size = MIN_GRID_SIZE;
        } else {
            size = size + 3 - (size % 3);
        }
        return size;
    }

    @Override
    public HomeItem getItem(int position) {
        if (iconAd != null ) {
            if (position == 0) {
                return new HomeItem(HomeItem.TYPE_ICON_AD, iconAd);
            }
        }
        int iconAdOffset = iconAd == null ? 0 : 1;
        if ( appInfos != null && appInfos.size() > position - iconAdOffset ) {
            return  new HomeItem(HomeItem.TYPE_CLONE, appInfos.get(position - iconAdOffset));
        }
        if (showLucky && position == appInfos.size() + iconAdOffset) {
            return new HomeItem(HomeItem.TYPE_LUCKY, null);
        }
        int luckyOffset = showLucky ? 1: 0;
        if (position == appInfos.size() + luckyOffset + iconAdOffset){
            return  new HomeItem(HomeItem.TYPE_ADD, null);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        HomeItem item = getItem(i);
        view = new GridAppCell(mContext);
        ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
        TextView appName = (TextView) view.findViewById(R.id.app_name);
        TextView adFlag = (TextView) view.findViewById(R.id.ad_flag);
        adFlag.setVisibility(View.INVISIBLE);
        ImageView newDot = (ImageView) view.findViewById(R.id.new_dot);
        if (item != null ) {
            switch (item.type) {
                case HomeItem.TYPE_ADD:
                case HomeItem.TYPE_LUCKY:
                case HomeItem.TYPE_CLONE:
                    if (item.type == HomeItem.TYPE_LUCKY) {
                        adFlag.setVisibility(View.VISIBLE);
                        appIcon.setImageResource(R.drawable.icon_feel_lucky);
                        appName.setText(R.string.feel_lucky);
                        appName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        appName.setTextColor(mContext.getResources().getColor(R.color.lucky_red));
                    } else if (item.type == HomeItem.TYPE_ADD) {
                        appIcon.setImageResource(R.drawable.icon_add);
                    } else if (item.type == HomeItem.TYPE_CLONE) {
                        CloneModel appModel = (CloneModel) item.obj;
                        CustomizeAppData data = CustomizeAppData.loadFromPref(appModel.getPackageName(),
                                appModel.getPkgUserId());
                        appModel.setCustomIcon(data.getCustomIcon());
                        if (appModel.getLaunched() == 0) {
                            newDot.setVisibility(View.VISIBLE);
                        } else {
                            newDot.setVisibility(View.INVISIBLE);
                        }
                        if (appModel.getCustomIcon() != null) {
                            appIcon.setImageBitmap(appModel.getCustomIcon());
                        }
                        appName.setText(data.customized ? data.label : appModel.getName());
                    }
                    break;
                case HomeItem.TYPE_ICON_AD:
                    AdViewBinder viewBinder = new AdViewBinder.Builder(R.layout.grid_app_item)
                            .iconImageId(R.id.app_icon)
                            .adFlagId(R.id.ad_flag)
                            .titleId(R.id.app_name).build();
                    view = iconAd.getAdView(mContext, viewBinder);
                    adFlag = (TextView) view.findViewById(R.id.ad_flag);
                    newDot = (ImageView) view.findViewById(R.id.new_dot);
                    newDot.setVisibility(View.VISIBLE);
                    adFlag.setVisibility(View.VISIBLE);
                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return ((HomeActivity)mContext).onItemLongClick(item, v);
                        }
                    });
                    break;
            }
            item.inflateView = view;
        }

        return view;
    }
}
