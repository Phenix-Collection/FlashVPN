package com.polestar.superclone.reward;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.widgets.ProductGridAdapter;
import com.polestar.superclone.widgets.ProductGridItem;
import com.polestar.task.network.datamodels.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends BaseActivity {



    private Product mProduct;
    private TextView mName;
    private TextView mDescription;
    private Button mPurchase;
    private ImageView mIcon;

    private AppUser mAppUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        setTitle("Purchase");

        mAppUser = AppUser.getInstance();

        mName = (TextView) findViewById(R.id.activity_product_product_name);
        mDescription = (TextView) findViewById(R.id.activity_product_product_description);
        mPurchase = (Button) findViewById(R.id.activity_product_purchase);
        mIcon = (ImageView) findViewById(R.id.activity_product_product_icon);

        Intent intent = getIntent();
        mProduct = intent.getParcelableExtra(ProductsActivity.EXTRA_PRODUCT);
        mName.setText(mProduct.mName);
        mDescription.setText(mProduct.mDescription);
        mIcon.setImageDrawable(AssetHelper.getDrawable(this, mProduct.mIconUrl));

        mPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                consumeProduct();
            }
        });
    }

    private void consumeProduct() {
        mAppUser.consumeProduct(mProduct.mId, 1);
    }

}
