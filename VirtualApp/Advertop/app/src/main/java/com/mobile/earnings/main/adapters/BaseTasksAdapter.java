package com.mobile.earnings.main.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.main.holders.DefaultTaskHolder;
import com.mobile.earnings.main.holders.HeaderHolder;
import com.mobile.earnings.main.holders.ServerTaskHolder;
import com.mobile.earnings.utils.TextUtils;
import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;



public class BaseTasksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

	public static final int VIEW_TYPE_TASK         = 0;
	public static final int VIEW_TYPE_DEFAULT_TASK = 1;
	public static final int VIEW_TYPE_HEADER       = 2;
	public static final int VIEW_TYPE_ACTIVE_TASK  = 3;

	public static final int TASK_DELAY_TWO_DAYS   = 2;
	public static final int TASK_DELAY_THREE_DAYS = 3;

	public interface OnTaskSelectedListener{
		void onTaskSelected(int itemPosition);
	}

	private final ArrayList<AppModel>    items;
	private final Context                context;
	private       OnTaskSelectedListener listener;

	public BaseTasksAdapter(Context context, ArrayList<AppModel> items, BaseTasksAdapter.OnTaskSelectedListener listener){
		this.items = items;
		this.context = context;
		this.listener = listener;
	}

	@Override
	public int getItemViewType(int position){
		return items.get(position).viewType;
	}

	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
		return produceViewHolder(viewType, parent);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
		bind(holder, items.get(position));
	}

	@Override
	public int getItemCount(){
		return items == null ? 0 : items.size();
	}

	public void updateList(ArrayList<AppModel> newTasks){
		if(items != null) {
			items.addAll(newTasks);
			notifyDataSetChanged();
		}
	}

	private RecyclerView.ViewHolder produceViewHolder(int viewType, ViewGroup parent){
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View itemView;
		switch(viewType){
			case VIEW_TYPE_TASK:
				itemView = inflater.inflate(R.layout.item_server_task, parent, false);
				return new ServerTaskHolder(itemView, listener);
			case VIEW_TYPE_ACTIVE_TASK:
				itemView = inflater.inflate(R.layout.item_server_task, parent, false);
				return new ServerTaskHolder(itemView, listener);
			case VIEW_TYPE_DEFAULT_TASK:
				itemView = inflater.inflate(R.layout.item_default_task, parent, false);
				return new DefaultTaskHolder(itemView, listener);
			default:
				itemView = inflater.inflate(R.layout.item_divider_text, parent, false);
				return new HeaderHolder(itemView);
		}
	}

	private void bind(RecyclerView.ViewHolder h, AppModel model){
		if(h instanceof ServerTaskHolder) {
			ServerTaskHolder holder = (ServerTaskHolder) h;
			if(model.image != null) {
				Glide.with(context).load(TextUtils.getRightUri(model.image)).placeholder(R.mipmap.ic_launcher).into(holder.icon);
			} else{
				holder.icon.setImageResource(model.resourceImage);
			}
			if(model.isAvailable != null) { //ActiveTask
				holder.title.setText(model.name);
				holder.description.setText(model.description);
				holder.cost.setBackgroundResource(0);
				if(model.isAvailable) {
					holder.cost.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_available_task, 0, 0, 0);
				} else{
					int timeDelayInDays = model.timeDelayInSeconds / 24 / 60 / 60;
					switch(timeDelayInDays){
						case TASK_DELAY_TWO_DAYS:
							holder.cost.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_not_available_48, 0, 0, 0);
							break;
						case TASK_DELAY_THREE_DAYS:
							holder.cost.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_not_available_72, 0, 0, 0);
							break;
						default:
							holder.cost.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_not_available_task, 0, 0, 0);
							break;
					}
					if(model.isReviewAvailable) {
						holder.cardView.setCardElevation(5); //Making merging effect with window bg
						holder.container.setBackgroundResource(R.drawable.selector_selected_recycler_view_item);
						holder.disableView.setVisibility(View.GONE);
						holder.itemView.setEnabled(true);
						holder.itemView.setClickable(true);
					} else{
						holder.cardView.setCardElevation(0); //Making merging effect with window bg
						holder.container.setBackgroundResource(R.drawable.selector_not_enabled_item);
						holder.disableView.setVisibility(View.VISIBLE);
						holder.itemView.setEnabled(false);
						holder.itemView.setClickable(false);
					}

				}
			} else{ //Standard task
				if(model.isTaskWatched) {
					holder.title.setText(R.string.main_toolbar_title);
				} else{
					holder.title.setText(R.string.taskAdapter_newTaskTitle);
					holder.container.setBackgroundResource(R.drawable.selector_selected_new_task);
					holder.newTaskMark.setVisibility(View.VISIBLE);
				}
				String currencyCode = StringEscapeUtils.unescapeJava("\\" + model.currency);
				String price = String.format(App.getRes().getString(R.string.task_price), model.amount).concat(currencyCode);
				holder.cost.setText(price);
			}
		} else if(h instanceof DefaultTaskHolder) {
			DefaultTaskHolder holder = (DefaultTaskHolder) h;
			if(model.image != null) {
				Glide.with(context).load(model.image).placeholder(R.mipmap.ic_launcher).into(holder.icon);
			} else
				holder.icon.setImageResource(model.resourceImage);
			holder.title.setText(model.name);
			holder.description.setText(makeUnderLined(model.description));
			String currencyCode = StringEscapeUtils.unescapeJava("\\" + model.currency);
			String price;
			if(model.defaultTaskReward == null) {
				price = String.format(App.getRes().getString(R.string.task_price), model.amount).concat(currencyCode);
			} else{
				price = model.defaultTaskReward.contains("%") ? model.defaultTaskReward : model.defaultTaskReward.concat(currencyCode);
			}
			holder.cost.setText(price);
		} else if(h instanceof HeaderHolder) {
			HeaderHolder holder = (HeaderHolder) h;
			holder.header.setText(model.name);
		}
	}

	private SpannableString makeUnderLined(String text){
		SpannableString content = new SpannableString(text);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		return content;
	}

}
