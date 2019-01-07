package com.mobile.earnings.main.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.ads.MainAdsActivity;
import com.mobile.earnings.api.BaseFragment;
import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.main.MainActivity;
import com.mobile.earnings.main.PromoCodeActivity;
import com.mobile.earnings.main.RouletteActivity;
import com.mobile.earnings.main.TaskItemDecorator;
import com.mobile.earnings.main.adapters.BaseTasksAdapter;
import com.mobile.earnings.main.presenterImpls.TaskFragmentPresenterImpl;
import com.mobile.earnings.main.views.TaskFragmentView;
import com.mobile.earnings.utils.ImageUtils;
import com.mobile.earnings.utils.ReportEvents;
import com.crashlytics.android.Crashlytics;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mobile.earnings.main.DefTaskIds.DEF_AD_MOB;
import static com.mobile.earnings.main.DefTaskIds.DEF_BONUS;
import static com.mobile.earnings.main.DefTaskIds.DEF_COMMEND;
import static com.mobile.earnings.main.DefTaskIds.DEF_OFFERWALL;
import static com.mobile.earnings.main.DefTaskIds.DEF_REFERRAL;
import static com.mobile.earnings.main.DefTaskIds.DEF_ROULETTE;
import static com.mobile.earnings.main.DefTaskIds.DEF_VIDEO_ZONE;
import static com.mobile.earnings.main.DefTaskIds.DEF_VK;
import static com.mobile.earnings.main.adapters.BaseTasksAdapter.VIEW_TYPE_HEADER;
import static com.mobile.earnings.single.task.DetailedTaskActivity.getDetailedTaskIntent;
import static com.mobile.earnings.utils.Constantaz.CARD_VIEW_PADDING;
import static com.mobile.earnings.utils.Constantaz.REQUEST_CODE_PROMO_TASK;
import static com.mobile.earnings.utils.Constantaz.TASKS_COUNT;

public class TasksFragment extends BaseFragment implements TaskFragmentView, SwipyRefreshLayout.OnRefreshListener{

	@BindView(R.id.taskRecyclerView)
	RecyclerView       recyclerView;
//	@BindView(R.id.taskFrag_header)
//	TextView           headerTv;
//	@BindView(R.id.taskFrag_headerInfo)
//	TextView           headerInfoTv;
	@BindView(R.id.tasksRefreshLayout)
	SwipyRefreshLayout swipeRefreshLayout;

	private MainActivity              activity;
	private TaskFragmentPresenterImpl presenter;
	private BaseTasksAdapter          adapter;
	private
	@Nullable
	ArrayList<AppModel> taskList;

	private static final String EXTRA_ACTIVE_TASK_LIST = "active_tasks";
	private static final String EXTRA_TASK_LIST        = "retrieved_tasks";
	private static final String EXTRA_DEF_TASK_LIST    = "default.tasks";

	public static TasksFragment getInstance(ArrayList<AppModel> activeTasks,
                                            ArrayList<AppModel> tasks, ArrayList<AppModel> defaultTasks){
		TasksFragment fragment = new TasksFragment();
		Bundle args = new Bundle();
		args.putParcelableArrayList(EXTRA_ACTIVE_TASK_LIST, activeTasks);
		args.putParcelableArrayList(EXTRA_TASK_LIST, tasks);
		args.putParcelableArrayList(EXTRA_DEF_TASK_LIST, defaultTasks);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		presenter = new TaskFragmentPresenterImpl(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_tasks, container, false);
		ButterKnife.bind(this, view);
		Bundle args = getArguments();
		ArrayList<AppModel> activeTaskList = null;
		ArrayList<AppModel> serverTaskList = null;
		ArrayList<AppModel> defTaskList = null;

		if(args != null) {
			activeTaskList = args.getParcelableArrayList(EXTRA_ACTIVE_TASK_LIST);
			serverTaskList = args.getParcelableArrayList(EXTRA_TASK_LIST);
			defTaskList = args.getParcelableArrayList(EXTRA_DEF_TASK_LIST);
		}
		if(activeTaskList != null && serverTaskList != null && defTaskList != null) {
			taskList = new ArrayList<>();
			//Should sort active task - available task first
			Collections.sort(activeTaskList, new Comparator<AppModel>(){
				@Override
				public int compare(AppModel firstObject, AppModel secondObject){
					return secondObject.isAvailable.compareTo(firstObject.isAvailable);
				}
			});
			taskList.addAll(activeTaskList);
			if(serverTaskList != null && !serverTaskList.isEmpty()) {
				taskList.add(createServerTaskHeader());
			}
			taskList.addAll(serverTaskList);
			taskList.add(createDefTaskHeader());
			taskList.addAll(defTaskList);
//			taskList.add(createFooter());
		}
		initServerTasksRecyclerView();
		swipeRefreshLayout.setOnRefreshListener(this);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		setFragmentDesign();
	}

	@Override
	public void updateTasksFromServer(ArrayList<AppModel> data){
		if(adapter != null) {
			adapter.updateList(data);
		}
		if(swipeRefreshLayout.isRefreshing()) {
			swipeRefreshLayout.setRefreshing(false);
		}
	}

	@Override
	public void updateActiveTasks(ArrayList<AppModel> data){
		if(adapter != null) {
			adapter.updateList(data);
		}
		if(swipeRefreshLayout.isRefreshing()) {
			swipeRefreshLayout.setRefreshing(false);
		}
	}

	@Override
	public void informUser(int resourceId){
		Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
	}

	@Override
	public void showLoad(){
		showProgressDialog();
	}

	@Override
	public void hideLoad(){
		hideProgressDialog();
	}

	@Override
	public void onRefresh(SwipyRefreshLayoutDirection direction){
		if(adapter != null) {
			presenter.getActiveTasks(adapter.getItemCount(), TASKS_COUNT);
			presenter.getTasksFromServer(adapter.getItemCount(), TASKS_COUNT);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == RC_PICK_IMAGE && data != null) {
			String realPath;
			// SDK >= 11 && SDK < 19
			if(Build.VERSION.SDK_INT < 19) {
				realPath = ImageUtils.getRealPathFromURI_API11to18(activity, data.getData());
			}
			// SDK > 19 (Android 4.4)
			else{
				realPath = ImageUtils.getRealPathFromURI_API19(activity, data.getData());
			}
			if(realPath != null) {
				presenter.sendScreenshotOnModeration(realPath, 0);
			} else{
				informUser(R.string.someErrorText);
			}
		}
	}

//	@OnClick(R.id.taskFrag_header)
//	void changeVisibility(){
//		showHideInfoHeader();
//	}

	private BaseTasksAdapter.OnTaskSelectedListener onTaskClickedListener = new BaseTasksAdapter.OnTaskSelectedListener(){
		@Override
		public void onTaskSelected(int itemPosition){
			if(taskList != null) {
				AppModel model = null;
				try{
					model = taskList.get(itemPosition);
				} catch(ArrayIndexOutOfBoundsException e){
					Crashlytics.logException(e);
				}
				if(model != null) {
					if(model.id > 0) {
						startActivity(getDetailedTaskIntent(activity, model));
					} else{
						handleOnDefTaskClick(model);
					}
				}
			}
		}
	};

	private void initServerTasksRecyclerView(){
		recyclerView.setLayoutManager(new LinearLayoutManager(activity));
		recyclerView.addItemDecoration(new TaskItemDecorator(CARD_VIEW_PADDING));
		recyclerView.setHasFixedSize(true);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setNestedScrollingEnabled(false);
		adapter = new BaseTasksAdapter(activity, taskList, onTaskClickedListener);
		recyclerView.setAdapter(adapter);
	}

	private AppModel createServerTaskHeader(){
		return new AppModel(1, VIEW_TYPE_HEADER, activity.getString(R.string.tasksFrag_serverTasksHeader), null, null, null, 0);
	}

	private AppModel createDefTaskHeader(){
		return new AppModel(2, VIEW_TYPE_HEADER, activity.getString(R.string.tasksFrag_defaultTasksHeader), null, null, null, 0);
	}

//	private AppModel createFooter(){
//		return new AppModel(3, VIEW_TYPE_HEADER, activity.getString(R.string.tasksFrag_footer), null, null, null, 0);
//	}

	private void handleOnDefTaskClick(AppModel model){
		switch(model.id){
			case DEF_AD_MOB:
				YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_VIDEO);
				activity.showAdMob();
				break;
			case DEF_OFFERWALL:
				startActivity(MainAdsActivity.getLaunchIntent(activity, false));
				break;
			case DEF_ROULETTE:
				startActivity(RouletteActivity.getRouletteIntent(activity, activity.mainBalance));
				break;
			case DEF_VIDEO_ZONE:
				startActivity(MainAdsActivity.getLaunchIntent(activity, true));
				break;
			case DEF_REFERRAL:
				activity.onElementClick(R.id.nav_friends);
				break;
			case DEF_VK:
				YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_VK_GROUP);
				try{
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/advertapp_me"));
					startActivity(browserIntent);
					presenter.finishVKTask();
				} catch(ActivityNotFoundException e){
					informUser(R.string.registerAct_browserActNotFoundException);
				}
				break;
			case DEF_COMMEND:
				if(isAdded()) {
					createReviewAppTaskDialog(false, model.defaultTaskKeywords, 5);
				} else{
					informUser(R.string.someErrorText);
				}
				break;
			case DEF_BONUS:
				activity.startActivityForResult(new Intent(activity, PromoCodeActivity.class), REQUEST_CODE_PROMO_TASK);
				break;
			default:
				break;
		}
	}

	private void setFragmentDesign(){
		YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_TASKS);
//		headerTv.setText(activity.getString(R.string.tasksFrag_header));
	}

	private boolean mIsAnimationDown = true;

//	private void showHideInfoHeader(){
//		if(mIsAnimationDown) {
//			headerInfoTv.setVisibility(View.VISIBLE);
//			mIsAnimationDown = false;
//		} else{
//			headerInfoTv.setVisibility(View.GONE);
//			mIsAnimationDown = true;
//		}
//	}

	private static final int RC_PICK_IMAGE = 154;

	private void createReviewAppTaskDialog(boolean isReviewOnly, String commentKeywords, int stars){
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity, R.style.DefaultDialogStyle);
		View view = LayoutInflater.from(activity).inflate(R.layout.dialog_additional_task, null);
		dialogBuilder.setView(view);
		final AlertDialog dialog = dialogBuilder.create();
		TextView conditionsTv = (TextView) view.findViewById(R.id.dialog_star_conditions);
		Button sendScreenShotBut = (Button) view.findViewById(R.id.dialog_star_send_screenshot);
		View.OnClickListener onButClickListener = new View.OnClickListener(){
			@Override
			public void onClick(View v){
				if(isAdded()) {
					try{
						TasksFragment.this.startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RC_PICK_IMAGE);
					} catch(ActivityNotFoundException e){
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(Intent.createChooser(intent, "Select Picture"), RC_PICK_IMAGE);
					}
				} else{
					informUser(R.string.someErrorText);
				}
				dialog.dismiss();
			}
		};
		sendScreenShotBut.setOnClickListener(onButClickListener);
		if(isReviewOnly) {
			conditionsTv.setText(getString(R.string.dialogStarApp_conditions, stars));
		} else{
			conditionsTv.setText(getString(R.string.dialogStarCommentApp_conditions, stars, commentKeywords));
		}
		dialog.show();
	}

}
