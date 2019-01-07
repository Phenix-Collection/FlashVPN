package com.mobile.earnings.main;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class TaskItemDecorator extends RecyclerView.ItemDecoration{

	private int offset;

	public TaskItemDecorator(int offset){
		this.offset = offset;
	}
	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state){
		outRect.bottom = offset;
	}
}
