package com.polestar.multiaccount.component.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.BitmapUtils;

import java.util.List;

/**
 * Created by yxx on 2016/8/8.
 */
public class AppHomeVerticalAdapter extends RecyclerView.Adapter<AppHomeVerticalAdapter.ViewHolder> {
    private Context mContext;
    private List<AppModel> appInfos;
    private AdapterView.OnItemClickListener onItemClickListener;

    public AppHomeVerticalAdapter(Context context, List<AppModel> appInfos) {
        this.mContext = context;
        this.appInfos = appInfos;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return appInfos == null ? 0 : appInfos.size();
    }

    @Override
    public AppHomeVerticalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(mContext).inflate(R.layout.item_app, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(AppHomeVerticalAdapter.ViewHolder holder, int position) {
        AppModel appModel = appInfos.get(position);
        if(appModel.getCustomIcon() == null){
            appModel.setCustomIcon(BitmapUtils.createCustomIcon(mContext,appModel.initDrawable(mContext)));
        }

        if(appModel.getCustomIcon() != null){
            holder.appIcon.setImageBitmap(appModel.getCustomIcon());
        }else{
//            holder.appIcon.setImageDrawable(appModel.initDrawable(mContext));
        }
        holder.appName.setText(appModel.getName());
        holder.contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(null,holder.contentView,position,0);
                }
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        View contentView;
        ImageView appIcon;
        TextView appName;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
            appName = (TextView) itemView.findViewById(R.id.app_name);
        }
    }
}
