package com.mobile.earnings.main.holders;




import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.earnings.R;
import com.mobile.earnings.main.adapters.BaseTasksAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ServerTaskHolder extends RecyclerView.ViewHolder{

	@BindView(R.id.item_task_card)
	public CardView cardView;
	@BindView(R.id.item_task_container)
	public FrameLayout container;
	@BindView(R.id.item_task_icon)
	public ImageView   icon;
	@BindView(R.id.item_task_title)
	public TextView    title;
	@BindView(R.id.item_task_description)
	public TextView    description;
	@BindView(R.id.item_task_cost)
	public TextView    cost;
	@BindView(R.id.item_new_task_mark)
	public ImageView   newTaskMark;
	@BindView(R.id.item_not_enable_view)
	public View disableView;

	public ServerTaskHolder(View itemView, final BaseTasksAdapter.OnTaskSelectedListener listener){
		super(itemView);
		ButterKnife.bind(this, itemView);
		itemView.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				if(listener != null) {
					listener.onTaskSelected(getAdapterPosition());
				}
			}
		});
	}
}