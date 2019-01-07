package com.mobile.earnings.main.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseFragment;
import com.mobile.earnings.api.data_models.PaymentSystem;
import com.mobile.earnings.main.presenterImpls.FundsPresenterImpl;
import com.mobile.earnings.main.views.FundsView;
import com.mobile.earnings.utils.ReportEvents;
import com.crashlytics.android.Crashlytics;
import com.yandex.metrica.YandexMetrica;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mobile.earnings.utils.Constantaz.PRIVAT_PAYMENT_SYSTEM;

public class FundsFragment extends BaseFragment implements FundsView, View.OnClickListener, PaymentSystemsModalSheet.OnPaymentSystemClickInterface{

	@BindView(R.id.fragmentMyFunds)
	TextView myFundsTV;
	@BindView(R.id.fragmentFundsReferralBalance)
	TextView referralBalanceTV;
	@BindView(R.id.fragmentFundsExpectedFunds)
	TextView expectedBalanceTV;
	@BindView(R.id.fragmentFundsReleaseFunds)
	Button   releaseFunds;

	private FundsPresenterImpl       presenter;
	private Activity                 activityContext;
	private String                   currencyCode;
	@Nullable
	private PaymentSystemsModalSheet modalBottomSheet;
	private boolean canReleaseMoney = false;

	public static FundsFragment getInstance(){
		FundsFragment fragment = new FundsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		activityContext = getActivity();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_funds, container, false);
		YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_BALANCE);
		ButterKnife.bind(this, view);
		presenter = new FundsPresenterImpl(this);
		releaseFunds.setOnClickListener(this);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onResume(){
		super.onResume();
		presenter.getFundsFromServer();
	}

	@Override
	public void onPause(){
		super.onPause();
		if(modalBottomSheet != null && modalBottomSheet.isVisible()) {
			modalBottomSheet.dismiss();
		}
	}

	@Override
	public void onDetach(){
		super.onDetach();
	}

	@Override
	public void onStop(){
		super.onStop();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}

	@Override
	public void onClick(View v){
		if(modalBottomSheet != null) {
			modalBottomSheet.setOnPaymentSystemClickListener(this);
			if(!modalBottomSheet.isAdded()) {
				try{
					if(canReleaseMoney) {
						modalBottomSheet.show(getFragmentManager(), modalBottomSheet.getTag());
					} else{
						informUser(R.string.fundsFrag_launchLimitException);
					}
				} catch(IllegalStateException e){
					modalBottomSheet.dismissAllowingStateLoss();
					informUser(R.string.someErrorText);
				}
			}
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_RELEASE_FUNDS);
		} else{
			informUser(R.string.someErrorText);
		}
	}

	@Override
	public void setDataFromServer(float ownBalance, float referralBalance, float overallBalance, float expectedBalance, String currencyCode){
		Resources res = App.getRes();
		this.currencyCode = StringEscapeUtils.unescapeJava("\\" + currencyCode);
		myFundsTV.setText(String.format(res.getString(R.string.fragment_funds_text), ownBalance).concat(this.currencyCode));
		referralBalanceTV.setText(String.format(res.getString(R.string.fragment_funds_text), referralBalance).concat(this.currencyCode));
		expectedBalanceTV.setText(String.format(res.getString(R.string.fragment_funds_text), expectedBalance).concat(this.currencyCode));
	}


	@Override
	public void showLoading(){
		showProgressDialog();
	}

	@Override
	public void hideLoading(){
		hideProgressDialog();
	}

	@Override
	public void informUser(int resourceId){
		Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
	}

	private String getHint(String name){
		if(name.contentEquals("Webmoney") || name.contentEquals("Qiwi") || name.contentEquals("Yandex")) {
			return App.getContext().getString(R.string.payeerDialogInputWalletHint);
		}
		if(name.contentEquals("Beeline") || name.contentEquals("Megafon") || name.contentEquals("Mts") || name.contentEquals("Tele2")) {
			return App.getContext().getString(R.string.payeerDialogInputMobileHint);
		}
		if(name.contentEquals("Mastercard") || name.contentEquals("Visa")) {
			return App.getContext().getString(R.string.payeerDialogInputCardHint);
		} else return App.getContext().getString(R.string.payeerDialogInputAccountHint);
	}

	@Override
	public void onSystemClick(PaymentSystem model){
		createPaymentDialog(model);
	}

	@Override
	public void showRefillDialog(final String account, final float amount, final float cleanAmount, final String paymentSystemName){
		AlertDialog.Builder dialog = new AlertDialog.Builder(activityContext, R.style.DefaultDialogStyle);
		String currencyCode = StringEscapeUtils.unescapeJava("\\" + this.currencyCode);
		dialog.setMessage(String.format(activityContext.getString(R.string.refillAct_confirmDialog), cleanAmount).concat(currencyCode));
		dialog.setPositiveButton(activityContext.getString(R.string.refillAct_confirmationDialogPosButText), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				presenter.refillUserBalance(account, amount, paymentSystemName);
				dialog.dismiss();
			}
		}).setNegativeButton(activityContext.getString(R.string.refillAct_confirmationDialogNegButText), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		}).create().show();
	}

	@Override
	public void informAboutLimit(float limit){
		AlertDialog.Builder dialog = new AlertDialog.Builder(activityContext, R.style.DefaultDialogStyle);
		String currencyCode = StringEscapeUtils.unescapeJava("\\" + this.currencyCode);
		dialog.setMessage(activityContext.getString(R.string.refillAct_limitException, limit).concat(currencyCode));
		DialogInterface.OnClickListener onDismissButClicked = new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		};
		dialog.setPositiveButton(android.R.string.ok, onDismissButClicked);
		dialog.create();
		if(activityContext.isFinishing()) {
			return;
		}
		try{
			dialog.show();
		} catch(IllegalArgumentException e){
			Crashlytics.logException(e);
		}
	}

	@Override
	public void createPaymentSystemSheet(ArrayList<PaymentSystem> items){
		if(modalBottomSheet == null) {
			modalBottomSheet = PaymentSystemsModalSheet.newInstance(items);
		}
	}

	@Override
	public void canReleaseFunds(boolean canReleaseFunds){
		canReleaseMoney = canReleaseFunds;
	}

	@OnClick(R.id.fragmentFundsTransferBut)
	void transferFunds(){
		YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_TRANSFER);
		presenter.transferFunds();
	}

	private void createPaymentDialog(final PaymentSystem model){
		final AlertDialog.Builder dialog = new AlertDialog.Builder(activityContext, R.style.DefaultDialogStyle);
		View customView = LayoutInflater.from(activityContext).inflate(R.layout.payeer_payment_dialog, null);
		final EditText amountET = (EditText) customView.findViewById(R.id.payeerDialogEditText);
		final EditText accountET = (EditText) customView.findViewById(R.id.payeerDialogEditTextAccount);
		accountET.setHint(getHint(model.name));
		dialog.setView(customView);
		dialog.setTitle(activityContext.getString(R.string.payeerDialogTitle)).setPositiveButton(activityContext.getString(R.string.payeerDialogOkBut), null).setNegativeButton(activityContext.getString(R.string.payeerDialogCancelBut), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		});
		final AlertDialog paymentDialog = dialog.create();
		if(activityContext.isFinishing()) {
			return;
		}
		try{
			paymentDialog.show();
		} catch(IllegalArgumentException e){
			Crashlytics.logException(e);
		}
		paymentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				String amount = amountET.getText().toString();
				String account = accountET.getText().toString();
				if(!amount.isEmpty() && !account.isEmpty()) {
					String title = model.name;
					if(title.contentEquals("Kyivstar") || title.contentEquals("Vodafone") || title.contentEquals("Lifecell")) {
						presenter.checkAmount(account, Float.valueOf(amount), PRIVAT_PAYMENT_SYSTEM);
					} else{
						presenter.checkAmount(account, Float.valueOf(amount), model.name);
					}
					if(activityContext.isFinishing()) {
						return;
					}
					try{
						paymentDialog.dismiss();
					} catch(IllegalArgumentException e){
						Crashlytics.logException(e);
					}
				} else{
					Toast.makeText(App.getContext(), App.getContext().getString(R.string.payeerPaymentDialogWrongAccountException), Toast.LENGTH_LONG).show();
				}
			}
		});
	}

}
