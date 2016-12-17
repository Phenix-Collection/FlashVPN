package com.polestar.multiaccount.component.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.BitmapUtils;

import java.util.List;


/**
 *
 * Created by yxx on 2016/7/19.
 */
public class AppHomeAdapter extends BaseAdapter {

    private Context mContext;
    private List<AppModel> appInfos;
    private int pageSize;
    private boolean isAdLoaded = false;
    private Bitmap adBitmap;
    private int pageCount;
    private boolean onDelete;

    public AppHomeAdapter(Context context, List<AppModel> appInfos, int pageSize) {
        this.mContext = context;
        this.appInfos = appInfos;
        this.pageSize = pageSize;
        loadAd();
    }

    public void onDelete(){
        onDelete = true;
    }

    public void deleteComplete(){
        onDelete = false;
    }

    @Override
    public int getCount() {
        if (appInfos != null && appInfos.size() > 0 && isAdLoaded) {
            pageCount = appInfos.size() / pageSize + 1;
            return appInfos.size() + pageCount;
        }
        return appInfos == null ? 0 : appInfos.size();

    }

    @Override
    public Object getItem(int i) {
        if (isEnabled(i)) {
            return appInfos == null ? null : appInfos.get(getNatureIndex(i));
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
//        if (view != null) {
//            holder = (ViewHolder) view.getTag();
//        }
//        if (view == null || holder == null) {
        view = LayoutInflater.from(mContext).inflate(R.layout.item_app, null);
        holder = new ViewHolder();
        holder.appIcon = (ImageView) view.findViewById(R.id.app_icon);
        holder.appName = (TextView) view.findViewById(R.id.app_name);
        view.setTag(holder);
//        }
        if (isAppEnabled(i)) {
            AppModel appModel = appInfos.get(getNatureIndex(i));
            if (appModel.getCustomIcon() == null) {
                appModel.setCustomIcon(BitmapUtils.createCustomIcon(mContext, appModel.initDrawable(mContext)));
            }

            if (appModel.getCustomIcon() != null) {
                holder.appIcon.setImageBitmap(appModel.getCustomIcon());
            } else {
//            holder.appIcon.setImageDrawable(appModel.initDrawable(mContext));
            }
            holder.appName.setText(appModel.getName());
        } else {
           // return new AdView(mContext, nativeAd).contentView;
        }
        return view;
    }

    private boolean isAppEnabled(int position){
        if (!isAdLoaded){
            int index = getNatureIndex(position);
            if(appInfos != null && index >= 0 && index < appInfos.size()){
                return !appInfos.get(index).isUnEnable();
            }
        }else if( position % pageSize != 0){
            int index = getNatureIndex(position);
            if(appInfos != null && index >= 0 && index < appInfos.size()){
                return !appInfos.get(index).isUnEnable();
            }
        }
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if(onDelete)
            return false;
        return isAppEnabled(position);
    }

    public int getNatureIndex(int position) {
        if (isAdLoaded && position > 0) {
            return position - position / pageSize - 1;
        }
        return position;
    }

    public int getFirstAppIndex() {
        if (isAdLoaded) {
            return 1;
        }
        return 0;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView appName;
    }

//    class AdView {
//        View contentView;
//        ImageView adIcon;
//        ImageView privacyInformationIcon;
//        TextView titleTv;
//
//        public AdView(Context context, IAd nativeAd) {
//            contentView = LayoutInflater.from(context).inflate(R.layout.item_ad, null);
//            adIcon = (ImageView) contentView.findViewById(R.id.ad_icon);
//            privacyInformationIcon = (ImageView) contentView.findViewById(R.id.ad_other_icon);
//            titleTv = (TextView) contentView.findViewById(R.id.ad_title);
//
//            if (nativeAd == null) {
//                return;
//            }
//            NativeAdData data = nativeAd.getNativeAd();
//
//            if (adBitmap != null) {
//                adIcon.setImageBitmap(adBitmap);
//            } else {
//                ImageLoader.getInstance().displayImage(data.getIconImageUrl(), adIcon);
//            }
//            ImageLoader.getInstance().displayImage(data.getPrivacyInformationIconUrl(), privacyInformationIcon);
//            titleTv.setText(data.getTitle());
//            data.handlePrivacyIconClick(mContext, privacyInformationIcon);
//            nativeAd.registerViewForInteraction(contentView);
//        }
//    }

    /**
     * 如果加载到广告，每页第一个icon显示广告
     */
    private void loadAd() {
//        LocalAdUtils.showAd(mContext.getApplicationContext(), null, LocalAdUtils.AD_STYLE[9], true, new OnAdLoadListener() {
//            @Override
//            public void onLoad(IAd iAd) {
//                MLogs.e("onLoad");
//                if (iAd.getNativeAd() != null) {
//                    isAdLoaded = true;
//                    nativeAd = iAd;
//                    MyLog.i(MyLog.TAG, "addAd--应用自定义样式和定义样式的View没有填充父容器，直接使用数据");
//                    ImageLoader.getInstance().loadImage(nativeAd.getIconUrl(), new ImageLoadingListener() {
//                        @Override
//                        public void onLoadingStarted(String imageUri, View view) {
//
//                        }
//
//                        @Override
//                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                            isAdLoaded = false;
//                            nativeAd = null;
//                        }
//
//                        @Override
//                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                            notifyDataSetChanged();
//                            adBitmap = loadedImage;
//                        }
//
//                        @Override
//                        public void onLoadingCancelled(String imageUri, View view) {
//                            isAdLoaded = false;
//                            nativeAd = null;
//                        }
//                    });
//                    notifyDataSetChanged();
//                } else if (iAd.getAdView() != null) {
//                    MLogs.e("addAd--传统的和banner，返回View");
//                } else {
//                    MLogs.e("addAd--有父类容器，banner和自定义样式View直接添加到父类容器");
//                }
//
//                iAd.setOnAdClickListener(new OnAdClickListener() {
//                    @Override
//                    public void onAdClicked() {
//                        MLogs.e("addAd--OnAdClickListener");
//                    }
//                });
//                iAd.setOnCancelAdListener(new OnCancelAdListener() {
//                    @Override
//                    public void cancelAd() {
//                        MLogs.e("addAd--setOnCancelAdListener");
//                    }
//                });
//                iAd.setOnPrivacyIconClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        MLogs.e("addAd--setOnPrivacyIconClickListener");
//                    }
//                });
//
//            }
//
//            @Override
//            public void onLoadFailed(AdError adError) {
//                MLogs.e("iconAd LoadFailed --" + adError.toString());
//                MyLog.i(MyLog.TAG, "addAd--应用自定义样式和定义样式的View没有填充父容器，直接使用数据");
//                notifyDataSetChanged();
//            }
//
//            @Override
//            public void onLoadInterstitialAd(WrapInterstitialAd wrapInterstitialAd) {
//                MLogs.e("onLoadInterstitialAd");
//            }
//        });
    }
}
