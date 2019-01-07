package com.mobile.earnings.main;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseActivity;
import com.mobile.earnings.api.responses.SendCopyAliPromoCode;
import com.mobile.earnings.main.presenterImpls.PromoPresenter;
import com.mobile.earnings.main.views.PromoView;
import com.mobile.earnings.utils.Constantaz;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mobile.earnings.utils.Constantaz.ALI_COPY_ENDPOINT;




public class PromoCodeActivity extends BaseActivity implements PromoView{

	@BindView(R.id.promoAct_promoET)
	EditText promoET;
	@BindView(R.id.promoAct_submitBut)
	Button submitBut;

 	private PromoPresenter presenter;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_promo_code);
		ButterKnife.bind(this);
		presenter = new PromoPresenter(this);
		setSupportActionBar(setToolBar(getString(R.string.promoAct_toolbarTitle)));
		if(getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@OnClick(R.id.promoAct_submitBut)
	void submit(){
		int hardcodedAliCopyOfferId = 3;
		presenter.activatePromoCode(promoET.getText().toString(), ALI_COPY_ENDPOINT, hardcodedAliCopyOfferId);
	}

	@Override
	public void informUser(int resourceId){
		Toast.makeText(this, resourceId, Toast.LENGTH_LONG).show();
	}

	@Override
	public void codeSubmitted(SendCopyAliPromoCode response){
		setResult(RESULT_OK, getIntent().putExtra(Constantaz.EXTRA_PROMO_BUDNLE, response));
		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			@Override
			public void run(){
				finish();
			}
		}, 500);

	}
}
