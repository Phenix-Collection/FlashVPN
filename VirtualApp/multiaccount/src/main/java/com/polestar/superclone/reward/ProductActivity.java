package com.polestar.superclone.reward;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.polestar.task.ADErrorCode;
import com.polestar.task.IProductStatusListener;
import com.polestar.task.network.datamodels.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends Activity implements IProductStatusListener{



    private Product mProduct;
    private TextView mName;
    private TextView mDescription;
    private TextView mPrice;

    private Button mPurchase;
    private ImageView mIcon;

    private AppUser mAppUser;

    private LinearLayout mEmailLayout;
    private LinearLayout mPaypalLayout;

    private EditText mEmail;
    private EditText mPaypal;

    public static final String EXTRA_PRODUCT = "product";

    public static void start(Activity activity, Product product) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PRODUCT, product);
        intent.setClass(activity, ProductActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_1);

        mAppUser = AppUser.getInstance();
//
        mName = (TextView) findViewById(R.id.activity_product_product_name);
        mDescription = (TextView) findViewById(R.id.activity_product_product_description);
        mPurchase = (Button) findViewById(R.id.activity_product_purchase);
        mIcon = (ImageView) findViewById(R.id.activity_product_product_icon);
        mPrice = (TextView) findViewById(R.id.activity_product_price);
        mEmail = (EditText) findViewById(R.id.activity_product_email_input);
        mPaypal = (EditText) findViewById(R.id.activity_product_paypal_input);

        mEmailLayout = (LinearLayout) findViewById(R.id.activity_product_email_layout);
        mPaypalLayout = (LinearLayout) findViewById(R.id.activity_product_paypal_layout);

        Intent intent = getIntent();
        mProduct = intent.getParcelableExtra(EXTRA_PRODUCT);
        mName.setText(mProduct.mName);
        mDescription.setText(mProduct.mDescription);
        mIcon.setImageDrawable(AssetHelper.getDrawable(this, mProduct.mIconUrl));
        mPrice.setText("" + (int) mProduct.mCost);

        if (mProduct.isFunctionalProduct()) {
            mEmailLayout.setVisibility(View.GONE);
            mPaypalLayout.setVisibility(View.GONE);
        } else {
            if (!mProduct.isPaypal()) {
                mPaypalLayout.setVisibility(View.GONE);
            }
        }

        mPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int status = mAppUser.checkProduct(mProduct);
                if (status != RewardErrorCode.PRODUCT_OK) {
                    toast(status);
                    return;
                }
                mAppUser.consumeProduct(mProduct.mId, 1,
                        mEmail.getText().toString(), mPaypal.getText().toString(), ProductActivity.this );
                //consumeProduct();
            }
        });
    }

    @Override
    public void onConsumeSuccess(long id, int amount, float totalCost, float balance) {
        toast(RewardErrorCode.PRODUCT_OK);
        finish();
    }

    @Override
    public void onConsumeFail(ADErrorCode code) {
        toast(code.getErrCode());
    }

    @Override
    public void onGetAllAvailableProducts(ArrayList<Product> products) {

    }

    @Override
    public void onGeneralError(ADErrorCode code) {
        toast(code.getErrCode());
    }

    private void toast(int code, Object... args){
        Toast.makeText(this, RewardErrorCode.getToastMessage(code, args), Toast.LENGTH_SHORT).show();
    }

    public void onClose(View view) {
        finish();
    }
}
