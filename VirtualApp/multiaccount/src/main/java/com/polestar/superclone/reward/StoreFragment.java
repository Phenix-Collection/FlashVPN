package com.polestar.superclone.reward;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.ad.AdLog;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseFragment;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.widgets.ProductGridAdapter;
import com.polestar.superclone.widgets.ProductGridItem;
import com.polestar.task.network.datamodels.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojia on 2019/1/23.
 */

public class StoreFragment extends BaseFragment implements AdapterView.OnItemClickListener {


    private List<ProductGridItem> mFunctionProductList = new ArrayList<>();
    private List<ProductGridItem> mMoneyProductList = new ArrayList<>();
    private LinearLayout mFunctionProductLayout;
    private LinearLayout adContainer;
    private LinearLayout mMoneyProductLayout;
    private GridView mFunctionProductGridView;
    private GridView mMoneyProductGridView;
    private TextView mUserBlanceTextView;
    private View mContentView;

    private AppUser mAppUser;
    public static final String EXTRA_PRODUCT = "product";

    private void updateGrid() {
        if (mFunctionProductList == null || mFunctionProductList.size() == 0) {
            mFunctionProductLayout.setVisibility(View.GONE);
            View otherTitle = mContentView.findViewById(R.id.other_clone_title);
//            View otherDetail = findViewById(R.id.other_clone_detail);
//            View noHotTitle = findViewById(R.id.no_hot_title);
//            otherDetail.setVisibility(View.GONE);
            otherTitle.setVisibility(View.GONE);
//            noHotTitle.setVisibility(View.VISIBLE);
        } else {
            MLogs.d("Hot app size: " + mFunctionProductList.size());
            mFunctionProductLayout.setVisibility(View.VISIBLE);
            ProductGridAdapter adapter = new ProductGridAdapter(getContext(), mFunctionProductList);
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
            ProductGridAdapter adapter = new ProductGridAdapter(getContext(), mMoneyProductList);
            mMoneyProductGridView.setAdapter(adapter);
            mMoneyProductGridView.setOnItemClickListener(this);
            adapter.notifyDataSetChanged();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAppUser = AppUser.getInstance();

        mContentView = inflater.inflate(R.layout.activity_products, null);
        mFunctionProductLayout = (LinearLayout) mContentView.findViewById(R.id.hot_clone_layout);
        mFunctionProductGridView = (GridView) mContentView.findViewById(R.id.hot_clone_grid);
        mMoneyProductLayout = (LinearLayout) mContentView.findViewById(R.id.other_clone_layout);
        mMoneyProductGridView = (GridView) mContentView.findViewById(R.id.other_clone_grid);
        mUserBlanceTextView = (TextView) mContentView.findViewById(R.id.activity_products_user_balance_txt);

        mUserBlanceTextView.setText(String.format(getString(R.string.you_have_coins),
                mAppUser.getMyBalance() , getActivity().getString(R.string.coin_unit)));

        initData();
        updateGrid();

        testCipher();
        return mContentView;
    }

    private void initData() {
        //loadAppListAsync();
        ArrayList<Product> allProducts = (ArrayList<Product>) mAppUser.getProducts();
        if (allProducts != null) {
            for (Product product : allProducts) {
                ProductGridItem item = ProductGridItem.fromProduct(product);
                if (product.isFunctionalProduct()) {
                    mFunctionProductList.add(item);
                } else {
                    mMoneyProductList.add(item);
                }
            }
        }
    }

    private void startProductActivity(Product product) {
        Intent i = new Intent(getActivity(), ProductActivity.class);
        i.putExtra(EXTRA_PRODUCT, product);
        startActivity(i);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ProductGridItem item = (ProductGridItem) view.getTag();
        if (item != null) {
            startProductActivity(item.getProduct());
        }
    }

    private void testCipher() {
        String iv = "fedcba9876543210";
        String key = "0123456789abcdef";
        String data = "Hello World!!!!";
        String encrypted = AdCipher.encrypt(key, iv, data);
        String decrypted = AdCipher.decrypt(key, encrypted);
        AdLog.i("encrypted " + encrypted + "    decrypted " + decrypted);

    }

}
