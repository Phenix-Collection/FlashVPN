package com.mobile.earnings.main.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mobile.earnings.R;

import butterknife.BindView;
import butterknife.ButterKnife;




public class HeaderHolder extends RecyclerView.ViewHolder{

	@BindView(R.id.item_divider_text)
	public TextView header;

	public HeaderHolder(View itemView){
		super(itemView);
		ButterKnife.bind(this, itemView);
	}
}