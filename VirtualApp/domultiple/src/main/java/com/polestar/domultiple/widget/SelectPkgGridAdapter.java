package com.polestar.domultiple.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.domultiple.R;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.MLogs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojia on 2017/7/17.
 */

public class SelectPkgGridAdapter  extends BaseAdapter{

    private List<SelectGridAppItem> appInfos;
    private Context mContext;

    public SelectPkgGridAdapter(Context context, List<SelectGridAppItem> list) {
        super();
        mContext = context;
        appInfos = list;
    }

    @Override
    public int getCount() {
        MLogs.d("get Count: " + appInfos.size());
        return appInfos.size();
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
        MLogs.d("getView: " + i);
        ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
        TextView appName = (TextView) view.findViewById(R.id.app_name);

        SelectGridAppItem appModel = (SelectGridAppItem) getItem(i);
        if (appModel != null) {
            appIcon.setImageDrawable(appModel.icon);
            appName.setText(appModel.name);
        }
        view.setTag(appModel);
        return view;
    }
}
