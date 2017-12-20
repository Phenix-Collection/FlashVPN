package com.polestar.domultiple.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.domultiple.R;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.CustomizeAppData;

import java.util.List;

/**
 * Created by PolestarApp on 2017/7/18.
 */

public class PackageSwitchListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<CloneModel> mModels;
    private Context mContext;
    private OnCheckStatusChangedListener mListener;
    private IsCheckedCallback mCallback;

    public void setOnCheckStatusChangedListener(OnCheckStatusChangedListener l) {
        mListener = l;
    }

    public interface OnCheckStatusChangedListener {
        void onCheckStatusChangedListener(CloneModel model, boolean status);
    }

    public interface IsCheckedCallback {
        boolean isCheckedCallback(CloneModel model);
    }

    public void setIsCheckedCallback(IsCheckedCallback cb) {
        mCallback = cb;
    }
    public PackageSwitchListAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setModels(List<CloneModel> list) {
        this.mModels = list;
    }

    public List<CloneModel> getModels() {
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
        CloneModel model = mModels.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_notification, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        CustomizeAppData data = CustomizeAppData.loadFromPref(model.getPackageName(), model.getPkgUserId());
        viewHolder.iconView.setImageBitmap(data.getCustomIcon());
        viewHolder.nameView.setText(data.customized? data.label: model.getName());
//        viewHolder.iconView.setImageDrawable(model.getIconDrawable(mContext));
//        viewHolder.nameView.setText(model.getName());
        if (position == (getCount() - 1)) {
            viewHolder.divider.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.divider.setVisibility(View.VISIBLE);
        }
        viewHolder.switchView.setChecked(mCallback == null? false: mCallback.isCheckedCallback(model));
        viewHolder.switchView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean val = ((BlueSwitch) v).isChecked();
                if (mListener != null) {
                    mListener.onCheckStatusChangedListener(model, val);
                }
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        private ImageView iconView;
        private TextView nameView;
        private BlueSwitch switchView;
        private View divider;

        public ViewHolder(View itemView) {
            iconView = (ImageView) itemView.findViewById(R.id.item_notification_icon);
            nameView = (TextView) itemView.findViewById(R.id.item_notification_app_name);
            switchView = (BlueSwitch) itemView.findViewById(R.id.item_notification_switch);
            divider = itemView.findViewById(R.id.item_divider);
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
