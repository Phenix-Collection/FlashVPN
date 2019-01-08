package mochat.multiple.parallel.whatsclone.component.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.component.BaseActivity;
import mochat.multiple.parallel.whatsclone.db.DbManager;
import mochat.multiple.parallel.whatsclone.model.AppModel;
import com.polestar.clone.CustomizeAppData;
import mochat.multiple.parallel.whatsclone.utils.MLogs;

import java.util.List;

/**
 * Created by guojia on 2017/7/29.
 */

public class CustomizeSettingActivity extends BaseActivity {
    private ListView listView;
    private List<AppModel> modelList;
    private BaseAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customize_setting_activity_layout);
        setTitle(getString(R.string.customize_title));
        listView = (ListView) findViewById(R.id.customize_apps);
        modelList = DbManager.queryAppList(this);
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return modelList.size();
            }

            @Override
            public Object getItem(int i) {
                return modelList.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View convertView, ViewGroup viewGroup) {
                AppModel model = modelList.get(i);
                if (model == null) return null;

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.custom_app_list_item, viewGroup, false);
                }
                if (convertView != null) {
                    ImageView iv = (ImageView) convertView.findViewById(R.id.item_icon);
                    TextView tv = (TextView) convertView.findViewById(R.id.item_name);
                    CustomizeAppData data = CustomizeAppData.loadFromPref(model.getPackageName(),
                            model.getPkgUserId());
                    iv.setImageBitmap(data.getCustomIcon());
                    tv.setText(data.customized? data.label: model.getName());
                }
                MLogs.d("getView " + i + " ret : " + convertView);
                return convertView;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppModel model = modelList.get(i);
                if (model == null) return ;
                CustomizeIconActivity.start(CustomizeSettingActivity.this, model.getPackageName(),
                        model.getPkgUserId());
            }
        });
    }

    @Override
    protected boolean useCustomTitleBar() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}
