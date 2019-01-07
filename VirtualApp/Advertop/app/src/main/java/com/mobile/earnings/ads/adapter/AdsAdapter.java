package com.mobile.earnings.ads.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.earnings.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;



public class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.AdViewHolder>{

	private ArrayList<AdModel>    mItems;
	private OnAdSelectedInterface mInterface;

	public AdsAdapter(ArrayList<AdModel> mItems){
		this.mItems = mItems;
	}

	@Override
	public AdViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ad, parent, false);
		return new AdViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(AdViewHolder holder, int position){
		holder.icon.setImageResource(mItems.get(position).getIcon());
		holder.text.setText(mItems.get(position).getTitle());
//		holder.description.setText(mItems.get(position).getDescription());
	}

	@Override
	public int getItemCount(){
		return mItems == null ? 0 : mItems.size();
	}

	public void setOnAdSelectedInterface(OnAdSelectedInterface listener){
		mInterface = listener;
	}

	public interface OnAdSelectedInterface{
		void onAdSelected(int id);
	}

	class AdViewHolder extends RecyclerView.ViewHolder{

		@BindView(R.id.item_ad_icon)
		ImageView icon;
		@BindView(R.id.item_ad_title)
		TextView text;
		@BindView(R.id.item_ad_description)
		TextView description;

		AdViewHolder(View itemView){
			super(itemView);
			ButterKnife.bind(this, itemView);
			if(mInterface != null) {
				itemView.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v){
						mInterface.onAdSelected(getAdapterPosition());
					}
				});
			}
		}
	}
}
