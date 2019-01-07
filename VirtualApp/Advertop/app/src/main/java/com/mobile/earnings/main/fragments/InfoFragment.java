package com.mobile.earnings.main.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseFragment;
import com.mobile.earnings.api.BaseView;
import com.mobile.earnings.main.MainActivity;
import com.mobile.earnings.utils.Constantaz;
import com.mobile.earnings.utils.ReportEvents;
import com.crashlytics.android.Crashlytics;
import com.yandex.metrica.YandexMetrica;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mobile.earnings.utils.Constantaz.POLICY_LINK;



public class InfoFragment extends BaseFragment implements BaseView{

	@BindView(R.id.infoFrag_firstDescTV)
	TextView     firstDescTV;
	@BindView(R.id.infoFrag_secondDescTV)
	TextView     secondDescTV;
	@BindView(R.id.infoFrag_thirdDescTV)
	TextView     thirdDescTV;
	@BindView(R.id.infoFrag_fourthDescTV)
	TextView     fourthDescTV;
	@BindView(R.id.infoFrag_supportBut)
	LinearLayout supportBut;
	@BindView(R.id.infoFrag_firstTitleTV)
	TextView     firstTitle;
	@BindView(R.id.infoFrag_secondTitleTV)
	TextView     secondTitle;
	@BindView(R.id.infoFrag_thirdTitleTV)
	TextView     thirdTitle;
	@BindView(R.id.infoFrag_fourthTitleTV)
	TextView     fourthTitle;
	@BindView(R.id.infoFrag_firstDescLayout)
	LinearLayout firstLayout;
	@BindView(R.id.infoFrag_secondDescLayout)
	LinearLayout secondLayout;
	@BindView(R.id.infoFrag_thirdDescLayout)
	LinearLayout thirdLayout;
	@BindView(R.id.infoFrag_fourthDescLayout)
	LinearLayout fourthLayout;

	private boolean firstBackward = false, secondBackward = false, thirdBackward = false, fourthBackward = false;
	private MainActivity activity;

	public static InfoFragment newInstance(){
		Bundle args = new Bundle();
		InfoFragment fragment = new InfoFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		View root = inflater.inflate(R.layout.fragment_info, container, false);
		ButterKnife.bind(this, root);
		YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_INFO);
		return root;
	}

	@Override
	public void informUser(int resourceId){
		Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
	}

	@OnClick({R.id.infoFrag_firstDescLayout, R.id.infoFrag_secondDescLayout, R.id.infoFrag_thirdDescLayout, R.id.infoFrag_fourthDescLayout, R.id.infoFrag_supportBut})
	void onViewClick(View view){
		int id = view.getId();
		switch(id){
			case R.id.infoFrag_firstDescLayout:
				startAnim(firstBackward, firstDescTV, id);
				break;
			case R.id.infoFrag_secondDescLayout:
				startAnim(secondBackward, secondDescTV, id);
				break;
			case R.id.infoFrag_thirdDescLayout:
				startAnim(thirdBackward, thirdDescTV, id);
				break;
			case R.id.infoFrag_fourthDescLayout:
				startAnim(fourthBackward, fourthDescTV, id);
				break;
			case R.id.infoFrag_supportBut:
				createSupportDialog();
				break;
			default:
				break;
		}
	}

	@OnClick(R.id.infoFrag_policyBut)
	void onPolicyButClick(){
		try{
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(POLICY_LINK));
			activity.startActivity(browserIntent);
		} catch(ActivityNotFoundException e){
			informUser(R.string.registerAct_browserActNotFoundException);
		}
	}

	private void startAnim(boolean backward, final TextView translatableLayout, int id){
		switch(id){
			case R.id.infoFrag_firstDescLayout:
				if(backward) {
					translatableLayout.animate().translationY(0).setListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation){
							super.onAnimationEnd(animation);
							translatableLayout.setVisibility(View.GONE);
							firstLayout.setBackgroundResource(R.color.primaryIconText);
							firstTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_arrow, 0);
						}
					});
					firstBackward = false;
				} else{
					translatableLayout.animate().translationY(translatableLayout.getLayoutParams().height).setListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation){
							super.onAnimationEnd(animation);
							firstLayout.setBackgroundResource(R.color.colorDarkGray);
							translatableLayout.setVisibility(View.VISIBLE);
							firstTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
						}
					});
					firstBackward = true;
				}
				break;
			case R.id.infoFrag_secondDescLayout:
				if(backward) {
					translatableLayout.animate().translationY(0).setListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation){
							super.onAnimationEnd(animation);
							translatableLayout.setVisibility(View.GONE);
							secondLayout.setBackgroundResource(R.color.primaryIconText);
							secondTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_arrow, 0);
						}
					});
					secondBackward = false;
				} else{
					translatableLayout.animate().translationY(translatableLayout.getLayoutParams().height).setListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation){
							super.onAnimationEnd(animation);
							secondLayout.setBackgroundResource(R.color.colorDarkGray);
							translatableLayout.setVisibility(View.VISIBLE);
							secondTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
						}
					});
					secondBackward = true;
				}
				break;
			case R.id.infoFrag_thirdDescLayout:
				if(backward) {
					translatableLayout.animate().translationY(0).setListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation){
							super.onAnimationEnd(animation);
							translatableLayout.setVisibility(View.GONE);
							thirdLayout.setBackgroundResource(R.color.primaryIconText);
							thirdTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_arrow, 0);
						}
					});
					thirdBackward = false;
				} else{
					translatableLayout.animate().translationY(translatableLayout.getLayoutParams().height).setListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation){
							super.onAnimationEnd(animation);
							thirdLayout.setBackgroundResource(R.color.colorDarkGray);
							translatableLayout.setVisibility(View.VISIBLE);
							thirdTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
						}
					});
					thirdBackward = true;
				}
				break;
			case R.id.infoFrag_fourthDescLayout:
				if(backward) {
					translatableLayout.animate().translationY(0).setListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation){
							super.onAnimationEnd(animation);
							translatableLayout.setVisibility(View.GONE);
							fourthLayout.setBackgroundResource(R.color.primaryIconText);
							fourthTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_arrow, 0);
						}
					});
					fourthBackward = false;
				} else{
					translatableLayout.animate().translationY(translatableLayout.getLayoutParams().height).setListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation){
							super.onAnimationEnd(animation);
							fourthLayout.setBackgroundResource(R.color.colorDarkGray);
							translatableLayout.setVisibility(View.VISIBLE);
							fourthTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
						}
					});
					fourthBackward = true;
				}
				break;
		}

	}

	private void createSupportDialog(){
		View customView = LayoutInflater.from(activity).inflate(R.layout.fragment_rules, null);
		final EditText messageView = (EditText) customView.findViewById(R.id.fragmentRulesCallBackET);
		Button button = (Button) customView.findViewById(R.id.fragmentRulesSendBut);
		AlertDialog dialog = new AlertDialog.Builder(activity, R.style.DefaultDialogStyle).setView(customView).create();
		if(activity.isFinishing()) {
			return;
		}
		try{
			dialog.show();
		} catch(IllegalArgumentException e){
			Crashlytics.logException(e);
		}
		View.OnClickListener onSendButClicked = new View.OnClickListener(){
			@Override
			public void onClick(View v){
				if(!messageView.getText().toString().isEmpty()) {
					sendEmail("Запитання", messageView.getText().toString());
				} else{
					informUser(R.string.rulesFrag_questionException);
				}
			}
		};
		button.setOnClickListener(onSendButClicked);
	}

	private void sendEmail(String subject, String text){
		if(subject.isEmpty())
			subject = "Запитання";
		ShareCompat.IntentBuilder.from(activity)
				.setType("message/rfc822")
				.addEmailTo(Constantaz.ADMIN_EMAIL)
				.setSubject(subject)
				.setText(text)
				.setChooserTitle("")
				.startChooser();
	}
}
