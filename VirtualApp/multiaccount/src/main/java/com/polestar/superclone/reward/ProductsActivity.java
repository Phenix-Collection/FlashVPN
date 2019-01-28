package com.polestar.superclone.reward;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.widgets.ProductGridAdapter;
import com.polestar.superclone.widgets.ProductGridItem;
import com.polestar.superclone.widgets.SelectGridAppItem;
import com.polestar.superclone.widgets.SelectPkgGridAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private List<ProductGridItem> mFunctionProductList = new ArrayList<>();
    private List<ProductGridItem> mMoneyProductList = new ArrayList<>();
    private LinearLayout mFunctionProductLayout;
    private LinearLayout adContainer;
    private LinearLayout mMoneyProductLayout;
    private GridView mFunctionProductGridView;
    private GridView mMoneyProductGridView;

    private AppUser mAppUser;

    private void updateGrid() {
        if (mFunctionProductList == null || mFunctionProductList.size() == 0) {
            mFunctionProductLayout.setVisibility(View.GONE);
            View otherTitle = findViewById(R.id.other_clone_title);
            View otherDetail = findViewById(R.id.other_clone_detail);
            View noHotTitle = findViewById(R.id.no_hot_title);
            otherDetail.setVisibility(View.GONE);
            otherTitle.setVisibility(View.GONE);
            noHotTitle.setVisibility(View.VISIBLE);
        } else {
            MLogs.d("Hot app size: " + mFunctionProductList.size());
            mFunctionProductLayout.setVisibility(View.VISIBLE);
            ProductGridAdapter adapter = new ProductGridAdapter(this, mFunctionProductList);
            mFunctionProductGridView.setAdapter(adapter);
            mFunctionProductGridView.setOnItemClickListener(this);
            adapter.notifyDataSetChanged();
            //setGrideViewHeightBasedOnChildren(mFunctionProductGridView);
        }
        if (mMoneyProductList == null || mMoneyProductList.size() == 0) {
            mMoneyProductLayout.setVisibility(View.GONE);
        } else {
            MLogs.d("Other app size: " + mMoneyProductList.size());
            mMoneyProductLayout.setVisibility(View.VISIBLE);
            ProductGridAdapter adapter = new ProductGridAdapter(this, mMoneyProductList);
            mMoneyProductGridView.setAdapter(adapter);
            mMoneyProductGridView.setOnItemClickListener(this);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppUser = AppUser.getInstance();

        initView();
        initData();
        updateGrid();
    }

    private void initData() {
        //loadAppListAsync();
        ProductGridItem item = new ProductGridItem();
        item.iconUrl = "default_product.png";
        item.name = "20 coins";
        item.description = "111pkgdescription";
        mFunctionProductList.add(item);


        ProductGridItem item2 = new ProductGridItem();
        item2.iconUrl = "default_product.png";
        item2.name = "100 coins";
        item2.description = "222pkgdescription";

        mMoneyProductList.add(item2);
    }

    private void initView() {
        setContentView(R.layout.activity_products);
        setTitle("Products");
        mFunctionProductLayout = (LinearLayout) findViewById(R.id.hot_clone_layout);
        mFunctionProductGridView = (GridView) findViewById(R.id.hot_clone_grid);
        mMoneyProductLayout = (LinearLayout) findViewById(R.id.other_clone_layout);
        mMoneyProductGridView = (GridView) findViewById(R.id.other_clone_grid);
//        cloneButton = (TextView)findViewById(R.id.clone_button);
//        cloneButton.setText(String.format(getString(R.string.clone_action_txt), ""));
//        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ProductGridItem item = (ProductGridItem) view.getTag();
        if (item != null) {
//            ImageView cbox = (ImageView) view.findViewById(R.id.select_cb_img);
//            View cover = view.findViewById(R.id.cover);
//            if (cbox != null) {
//                item.selected = !item.selected;
//                if (item.selected) {
//                    selected ++;
//                    cbox.setImageResource(R.drawable.selectd);
//                    cover.setVisibility(View.INVISIBLE);
//                } else {
//                    selected --;
//                    cbox.setImageResource(R.drawable.not_select);
//                    cover.setVisibility(View.VISIBLE);
//                }
//                if (selected > 0) {
//                    cloneButton.setText(String.format(getString(R.string.clone_action_txt), "(" + selected + ")"));
//                    cloneButton.setEnabled(true);
//
//                } else {
//                    cloneButton.setText(String.format(getString(R.string.clone_action_txt), ""));
//                    cloneButton.setEnabled(false);
//
//                }
//            }
        }
    }
}
