package com.mobile.earnings.main.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.earnings.R;
import com.mobile.earnings.main.adapters.BaseTasksAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;




public class DefaultTaskHolder extends RecyclerView.ViewHolder{

	@BindView(R.id.item_task_icon)
	public ImageView icon;
	@BindView(R.id.item_task_title)
	public TextView  title;
	@BindView(R.id.item_task_description)
	public TextView  description;
	@BindView(R.id.item_task_cost)
	public TextView  cost;

	public DefaultTaskHolder(View itemView, final BaseTasksAdapter.OnTaskSelectedListener listener){
		super(itemView);
		ButterKnife.bind(this, itemView);
		itemView.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				if(listener != null){
					listener.onTaskSelected(getAdapterPosition());
				}
			}
		});
	}
}