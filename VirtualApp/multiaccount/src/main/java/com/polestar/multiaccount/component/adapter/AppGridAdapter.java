package com.polestar.multiaccount.component.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.model.AppModel;

import java.util.List;

public class AppGridAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<AppModel> mModels;

    public AppGridAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void setModels(List<AppModel> list) {
        this.mModels = list;
        notifyDataSetChanged();
    }

    public List<AppModel> getModels() {
        return mModels;
    }

    @Override
    public int getCount() {
        return mModels == null ? 0 : mModels.size();
    }

    @Override
    public Object getItem(int position) {
        return mModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppModel model = mModels.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_app_grid, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.iconView.setImageDrawable(model.getIcon());
        viewHolder.nameView.setText(model.getName());
        return convertView;
    }

    private static class ViewHolder {
        private ImageView iconView;
        private TextView nameView;

        public ViewHolder(View itemView) {
            iconView = (ImageView) itemView.findViewById(R.id.grid_item_app_icon);
            nameView = (TextView) itemView.findViewById(R.id.grid_item_app_name);
        }
    }
}
