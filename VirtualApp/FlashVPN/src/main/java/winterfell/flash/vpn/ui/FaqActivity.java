package winterfell.flash.vpn.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import winterfell.flash.vpn.R;


/**
 * Created by guojia on 2016/12/10.
 */

public class FaqActivity extends BaseActivity {
    private Context mContext;
    private ListView faqListView;
    private FaqListAdapter faqListAdapter;

    private void initView(){
        faqListView = findViewById(R.id.faq_list);
        faqListAdapter = new FaqListAdapter(this);
        faqListView.setAdapter(faqListAdapter);
        faqListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                faqListAdapter.notifyDataSetChanged();
            }
        });
    }

    public static void start(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, FaqActivity.class);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq_layout);
        setTitle(getResources().getString(R.string.faq));
        mContext = this;
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onContactTeamClick(View view){
        FeedbackActivity.start(this, 0);
    }
}
