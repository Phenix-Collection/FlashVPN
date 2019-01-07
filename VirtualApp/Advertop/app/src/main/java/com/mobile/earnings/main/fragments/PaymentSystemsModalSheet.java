package com.mobile.earnings.main.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.earnings.R;
import com.mobile.earnings.api.data_models.PaymentSystem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;




public class PaymentSystemsModalSheet extends BottomSheetDialogFragment{

	@BindView(R.id.paymentSystemsBottomSheet_gridView)
	GridView gridView;

	private static final String ARGS_PAYMENT_SYSTEMS = "payment_systems";

	private OnPaymentSystemClickInterface anInterface;

	public interface OnPaymentSystemClickInterface{
		void onSystemClick(PaymentSystem model);
	}

	public static PaymentSystemsModalSheet newInstance(ArrayList<PaymentSystem> items){
		Bundle args = new Bundle();
		args.putParcelableArrayList(ARGS_PAYMENT_SYSTEMS, items);
		PaymentSystemsModalSheet fragment = new PaymentSystemsModalSheet();
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		View modalBottomSheetView = inflater.inflate(R.layout.bottom_sheet_payment_systems, container, false);
		ButterKnife.bind(this, modalBottomSheetView);
		Bundle args = getArguments();
		if(args != null) {
			ArrayList<PaymentSystem> items = args.getParcelableArrayList(ARGS_PAYMENT_SYSTEMS);
			if(items != null && !items.isEmpty()) prepareData(items);
		}
		return modalBottomSheetView;
	}

	public void setOnPaymentSystemClickListener(OnPaymentSystemClickInterface listener){
		anInterface = listener;
	}

	private void prepareData(final ArrayList<PaymentSystem> items){
		gridView.setAdapter(new PaymentSystemsAdapter(items));
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				if(anInterface != null) {
					anInterface.onSystemClick(items.get(position));
				}
				dismiss();
			}
		});
	}

	private class PaymentSystemsAdapter extends BaseAdapter{

		private final ArrayList<PaymentSystem> items;

		PaymentSystemsAdapter(ArrayList<PaymentSystem> items){
			this.items = items;
		}

		@Override
		public boolean isEnabled(int position){
			return items.get(position).active == 1;
		}

		@Override
		public int getCount(){
			return items == null ? 0 : items.size();
		}

		@Override
		public Object getItem(int position){
			return null;
		}

		@Override
		public long getItemId(int position){
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			View bottomSheetItem;
			if(convertView == null) {
				bottomSheetItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_systems_bottom_sheet, parent, false);
				ImageView icon = (ImageView) bottomSheetItem.findViewById(R.id.item_payment_systems_bottom_sheet_icon);
				TextView title = (TextView) bottomSheetItem.findViewById(R.id.item_payment_systems_bottom_sheet_title);
				icon.setImageResource(items.get(position).icon);
				if(items.get(position).active != 1) icon.setAlpha(.4f);
				title.setText(items.get(position).name);
			} else{
				bottomSheetItem = (View) convertView;
			}
			return bottomSheetItem;
		}
	}

}
