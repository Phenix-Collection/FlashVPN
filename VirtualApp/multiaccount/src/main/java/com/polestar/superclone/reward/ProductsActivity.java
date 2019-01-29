package com.polestar.superclone.reward;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.ad.AdLog;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.widgets.ProductGridAdapter;
import com.polestar.superclone.widgets.ProductGridItem;
import com.polestar.task.network.datamodels.Product;

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
    private TextView mUserBlanceTextView;

    private AppUser mAppUser;
    public static final String EXTRA_PRODUCT = "product";

    private void updateGrid() {
        if (mFunctionProductList == null || mFunctionProductList.size() == 0) {
            mFunctionProductLayout.setVisibility(View.GONE);
            View otherTitle = findViewById(R.id.other_clone_title);
//            View otherDetail = findViewById(R.id.other_clone_detail);
//            View noHotTitle = findViewById(R.id.no_hot_title);
//            otherDetail.setVisibility(View.GONE);
            otherTitle.setVisibility(View.GONE);
//            noHotTitle.setVisibility(View.VISIBLE);
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

        testCipher();
    }

    private void initData() {
        //loadAppListAsync();
        ArrayList<Product> allProducts = (ArrayList<Product>) mAppUser.getProducts();
        for (Product product : allProducts) {
            ProductGridItem item = ProductGridItem.fromProduct(product);
            if (product.isFunctionalProduct()) {
                mFunctionProductList.add(item);
            } else {
                mMoneyProductList.add(item);
            }
        }

        /*
        ProductGridItem item = new ProductGridItem();
        item.iconUrl = "default_product.png";
        item.name = "20 coins";
        item.description = "111pkgdescription";
        mFunctionProductList.add(item);


        ProductGridItem item2 = new ProductGridItem();
        item2.iconUrl = "default_product.png";
        item2.name = "100 coins";
        item2.description = "222pkgdescription";

        mMoneyProductList.add(item2);*/
    }

    private void initView() {
        setContentView(R.layout.activity_products);
        setTitle("Store");
        mFunctionProductLayout = (LinearLayout) findViewById(R.id.hot_clone_layout);
        mFunctionProductGridView = (GridView) findViewById(R.id.hot_clone_grid);
        mMoneyProductLayout = (LinearLayout) findViewById(R.id.other_clone_layout);
        mMoneyProductGridView = (GridView) findViewById(R.id.other_clone_grid);
        mUserBlanceTextView = (TextView) findViewById(R.id.activity_products_user_balance_txt);

        mUserBlanceTextView.setText(mAppUser.getMyBalance() + " coins");
//        cloneButton = (TextView)findViewById(R.id.clone_button);
//        cloneButton.setText(String.format(getString(R.string.clone_action_txt), ""));
//        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }

    private void startProductActivity(Product product) {
        Intent i = new Intent(this, ProductActivity.class);
        i.putExtra(EXTRA_PRODUCT, product);
        startActivity(i);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ProductGridItem item = (ProductGridItem) view.getTag();
        if (item != null) {
            Toast.makeText(this, "Product id " + item.id + " clicked", Toast.LENGTH_LONG).show();
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
