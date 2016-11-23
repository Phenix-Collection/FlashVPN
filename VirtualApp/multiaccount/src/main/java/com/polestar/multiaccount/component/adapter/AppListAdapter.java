package com.polestar.multiaccount.component.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.activity.AppListActivity;
import com.polestar.multiaccount.constant.Constants;
import com.polestar.multiaccount.model.AppModel;

import java.util.List;

public class AppListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<AppModel> mModels;
    private Context mContext;

    public AppListAdapter(Context context) {
        mContext = context;
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
            convertView = mInflater.inflate(R.layout.item_app_list, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.iconView.setImageDrawable(model.getIcon());
        viewHolder.nameView.setText(model.getName());
        viewHolder.description.setText(model.getDescription());
        viewHolder.cloneBtn.setText(R.string.btn_clone);
        viewHolder.cloneBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(Constants.EXTRA_APP_MODEL, model);
                ((AppListActivity) mContext).setResult(Activity.RESULT_OK, data);
                ((AppListActivity) mContext).finish();
            }
        });
        return convertView;
    }

    private static class ViewHolder {
        private ImageView iconView;
        private TextView nameView;
        private TextView description;
        private Button cloneBtn;

        public ViewHolder(View itemView) {
            iconView = (ImageView) itemView.findViewById(R.id.item_app_icon);
            nameView = (TextView) itemView.findViewById(R.id.item_app_name);
            description = (TextView) itemView.findViewById(R.id.item_app_description);
            cloneBtn = (Button) itemView.findViewById(R.id.btn_clone);
        }
    }
}
