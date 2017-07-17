package com.polestar.domultiple.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.domultiple.R;
import com.polestar.domultiple.utils.MLogs;

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
        view = new SelectAppCell(mContext);
        MLogs.d("getView: " + i);
        ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
        ImageView cb = (ImageView) view.findViewById(R.id.select_cb_img);
        TextView appName = (TextView) view.findViewById(R.id.app_name);

        SelectGridAppItem appModel = (SelectGridAppItem) getItem(i);
        if (appModel != null) {
            appIcon.setImageDrawable(appModel.icon);
            appName.setText(appModel.name);
            if (appModel.selected ) {
                cb.setImageResource(R.drawable.selectd);
            } else{
                cb.setImageResource(R.drawable.not_select);
            }
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectGridAppItem item = (SelectGridAppItem) view.getTag();
                if (item != null) {
                    ImageView cbox = (ImageView) view.findViewById(R.id.select_cb_img);
                    item.selected = !item.selected;
                    if (item.selected) {
                        cbox.setImageResource(R.drawable.selectd);
                    } else{
                        cbox.setImageResource(R.drawable.not_select);
                    }
                }
            }
        });
        view.setTag(appModel);
        return view;
    }
}
