package com.polestar.superclone.reward;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.widgets.SelectGridAppItem;
import com.polestar.superclone.widgets.SelectPkgGridAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final int APP_LIST_READY = 0;
    private static final String CONFIG_HOT_CLONE_LIST = "hot_clone_list";
    public static final String SLOT_ADD_CLONE_AD = "slot_add_clone_native";
    private List<SelectGridAppItem> hotAppList = new ArrayList<>();
    private List<SelectGridAppItem> otherAppList = new ArrayList<>();
    private LinearLayout hotAppLayout;
    private LinearLayout adContainer;
    private LinearLayout otherAppLayout;
    private GridView hotAppGridView;
    private GridView otherAppGridView;
    private TextView cloneButton;
    private int selected;
    private ProgressBar progressBar;
    private FuseAdLoader adLoader;
    private IAdAdapter mAd;
    private boolean appListReady;



    private void updateGrid() {
        if (hotAppList == null || hotAppList.size() == 0) {
            hotAppLayout.setVisibility(View.GONE);
            View otherTitle = findViewById(R.id.other_clone_title);
            View otherDetail = findViewById(R.id.other_clone_detail);
            View noHotTitle = findViewById(R.id.no_hot_title);
            otherDetail.setVisibility(View.GONE);
            otherTitle.setVisibility(View.GONE);
            noHotTitle.setVisibility(View.VISIBLE);
        } else {
            MLogs.d("Hot app size: " + hotAppList.size());
            hotAppLayout.setVisibility(View.VISIBLE);
            SelectPkgGridAdapter adapter = new SelectPkgGridAdapter(this,hotAppList);
            hotAppGridView.setAdapter(adapter);
            hotAppGridView.setOnItemClickListener(this);
            adapter.notifyDataSetChanged();
            //setGrideViewHeightBasedOnChildren(hotAppGridView);
        }
        if (otherAppList == null || otherAppList.size() == 0) {
            otherAppLayout.setVisibility(View.GONE);
        } else {
            MLogs.d("Other app size: " + otherAppList.size());
            otherAppLayout.setVisibility(View.VISIBLE);
            SelectPkgGridAdapter adapter = new SelectPkgGridAdapter(this,otherAppList);
            otherAppGridView.setAdapter(adapter);
            otherAppGridView.setOnItemClickListener(this);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        updateGrid();
    }

    private void initData() {
        //loadAppListAsync();
        SelectGridAppItem item = new SelectGridAppItem();
        item.icon = getResources().getDrawable(R.drawable.menu_icon);
        item.name = "111name";
        item.selected = false;
        item.pkg = "111pkg";

            hotAppList.add(item);


        item.icon = getResources().getDrawable(R.drawable.menu_icon);
        item.name = "222name";
        item.selected = false;
        item.pkg = "222pkg";

        otherAppList.add(item);
    }

    private void initView() {
        setContentView(R.layout.activity_products);
        setTitle("TITITITLE");
        hotAppLayout = (LinearLayout) findViewById(R.id.hot_clone_layout);
        hotAppGridView = (GridView) findViewById(R.id.hot_clone_grid);
        otherAppLayout = (LinearLayout) findViewById(R.id.other_clone_layout);
        otherAppGridView = (GridView) findViewById(R.id.other_clone_grid);
//        cloneButton = (TextView)findViewById(R.id.clone_button);
//        cloneButton.setText(String.format(getString(R.string.clone_action_txt), ""));
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SelectGridAppItem item = (SelectGridAppItem) view.getTag();
        if (item != null) {
            ImageView cbox = (ImageView) view.findViewById(R.id.select_cb_img);
            View cover = view.findViewById(R.id.cover);
            if (cbox != null) {
                item.selected = !item.selected;
                if (item.selected) {
                    selected ++;
                    cbox.setImageResource(R.drawable.selectd);
                    cover.setVisibility(View.INVISIBLE);
                } else {
                    selected --;
                    cbox.setImageResource(R.drawable.not_select);
                    cover.setVisibility(View.VISIBLE);
                }
//                if (selected > 0) {
//                    cloneButton.setText(String.format(getString(R.string.clone_action_txt), "(" + selected + ")"));
//                    cloneButton.setEnabled(true);
//
//                } else {
//                    cloneButton.setText(String.format(getString(R.string.clone_action_txt), ""));
//                    cloneButton.setEnabled(false);
//
//                }
            }
        }
    }
}
