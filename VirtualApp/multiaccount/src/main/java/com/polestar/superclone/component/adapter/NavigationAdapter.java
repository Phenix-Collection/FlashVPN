package com.polestar.superclone.component.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.superclone.R;


/**
 * Created by yxx on 2016/7/28.
 */
public class NavigationAdapter extends BaseAdapter {

    private Context mContext;
    private int[] iconArray = new int[]{R.drawable.lock_icon_small, R.mipmap.notification_settings,R.mipmap.icon_faq,R.mipmap.feedback,
            R.mipmap.icon_rate_us,R.mipmap.icon_share,R.mipmap.icon_setting};
    private String[] itemArray;
    private Resources mResources;
    public NavigationAdapter(Context context){
        this.mContext = context;
        mResources = context.getResources();
        itemArray = mResources.getStringArray(R.array.navigationItem);
    }
    @Override
    public int getCount() {
        return itemArray == null ? 0 : itemArray.length;
    }

    @Override
    public Object getItem(int i) {
        return itemArray == null ? i : itemArray[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = new ViewHolder();
        view = LayoutInflater.from(mContext).inflate(R.layout.item_navigation,null);
        holder.icon = (ImageView) view.findViewById(R.id.icon);
        holder.itemTv = (TextView) view.findViewById(R.id.item_name);

        holder.icon.setImageResource(iconArray[i]);
        holder.itemTv.setText(itemArray[i]);
        return view;
    }

    class ViewHolder{
        ImageView icon;
        TextView itemTv;
    }
}
