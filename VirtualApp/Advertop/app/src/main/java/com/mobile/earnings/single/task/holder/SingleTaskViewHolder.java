package com.mobile.earnings.single.task.holder;

import android.widget.ImageView;
import android.widget.TextView;

public class SingleTaskViewHolder{

	public final ImageView icon;
	public final TextView taskTitle;
	public final TextView daysLeftTitleTV;

	public SingleTaskViewHolder(ImageView icon, TextView taskTitle, TextView daysLeftTitleTV){
		this.icon = icon;
		this.taskTitle = taskTitle;
		this.daysLeftTitleTV = daysLeftTitleTV;
	}
}
