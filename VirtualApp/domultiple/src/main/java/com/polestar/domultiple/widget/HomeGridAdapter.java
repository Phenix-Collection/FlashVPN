package com.polestar.domultiple.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.domultiple.R;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.CustomizeAppData;
import com.polestar.domultiple.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojia on 2017/7/16.
 */
public class HomeGridAdapter extends BaseAdapter {
    
    private List<CloneModel> appInfos;
    private boolean showLucky;
    private Context mContext;
    private static final int MIN_GRID_SIZE = 6;

    public void setShowLucky(boolean s) {
        showLucky = s;
    }

    public HomeGridAdapter(Context context) {
        super();
        mContext = context;
        appInfos = new ArrayList<>(0);
    }

    public void notifyDataSetChanged(List<CloneModel> list) {
        appInfos = list;
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int size = appInfos == null ? 0 : appInfos.size();
        if (showLucky) {
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
    public Object getItem(int position) {
        if ( appInfos == null) {
            return  null;
        }
        if (position < appInfos.size() && position >= 0) {
            return  appInfos.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = new GridAppCell(mContext);

        ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
        TextView appName = (TextView) view.findViewById(R.id.app_name);
        ImageView newDot = (ImageView) view.findViewById(R.id.new_dot);

        CloneModel appModel = (CloneModel) getItem(i);
        if (appModel != null) {
            CustomizeAppData data = CustomizeAppData.loadFromPref(appModel.getPackageName());
//            if (appModel.getCustomIcon() == null) {
//            Bitmap bmp =
//            appIcon.setImageBitmap(bmp);
            appModel.setCustomIcon(data.getCustomIcon());

//                appModel.setCustomIcon(CommonUtils.createCustomIcon(mContext, appModel.getIconDrawable(mContext)));
//            }
            if (appModel.getLaunched() == 0) {
                newDot.setVisibility(View.VISIBLE);
            } else {
                newDot.setVisibility(View.INVISIBLE);
            }
            if (appModel.getCustomIcon() != null) {
                appIcon.setImageBitmap(appModel.getCustomIcon());
            }
            appName.setText(data.customized? data.label: appModel.getName());
        } else {
            int luckyIdx = appInfos.size();
            int addIdx = showLucky? luckyIdx + 1 : luckyIdx;
            if (showLucky && i == luckyIdx) {
                appIcon.setImageResource(R.drawable.icon_feel_lucky);
                appName.setText(R.string.feel_lucky);
                appName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                appName.setTextColor(mContext.getResources().getColor(R.color.lucky_red));
            }
            if (i == addIdx) {
                appIcon.setImageResource(R.drawable.icon_add);
            }
        }

        return view;
    }
}