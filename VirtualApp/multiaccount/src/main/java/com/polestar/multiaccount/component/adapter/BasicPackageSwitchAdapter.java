package com.polestar.multiaccount.component.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.widgets.BlueSwitch;

import java.util.List;

public class BasicPackageSwitchAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<AppModel> mModels;
    private Context mContext;
    private OnCheckStatusChangedListener mListener;
    private IsCheckedCallback mCallback;

    public void setOnCheckStatusChangedListener(OnCheckStatusChangedListener l) {
        mListener = l;
    }

    public interface OnCheckStatusChangedListener {
        void onCheckStatusChangedListener(AppModel model, boolean status);
    }

    public interface IsCheckedCallback {
        boolean isCheckedCallback(AppModel model);
    }

    public void setIsCheckedCallback(IsCheckedCallback cb) {
        mCallback = cb;
    }
    public BasicPackageSwitchAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setModels(List<AppModel> list) {
        this.mModels = list;
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
            convertView = mInflater.inflate(R.layout.item_notification, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.iconView.setImageDrawable(model.getIcon());
        viewHolder.nameView.setText(model.getName());
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
