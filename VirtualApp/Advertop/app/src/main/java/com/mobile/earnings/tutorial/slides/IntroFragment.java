package com.mobile.earnings.tutorial.slides;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IntroFragment extends BaseFragment{

	public static final String EXTRA_POSITION = "adapter_position";

	@BindView(R.id.itemIntroImage)
	ImageView tutorialImageView;
	@BindView(R.id.itemIntroTitle)
	TextView tvTitle;
	@BindView(R.id.itemIntroContentText)
	TextView tvContent;
//	@BindView(R.id.itemIntroPagerTestGoBut)
//	TextView goBut;

	public static IntroFragment getInstance(int position){
		Bundle args = new Bundle();
		args.putInt(EXTRA_POSITION, position);
		IntroFragment fragment = new IntroFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		View root = inflater.inflate(R.layout.item_intro_pager, container, false);
		ButterKnife.bind(this, root);
		int position = getArguments().getInt(EXTRA_POSITION);
		int[] images = provideImages();
		String[] titles = App.getContext().getResources().getStringArray(R.array.intro_titles);
		String[] contentText = App.getContext().getResources().getStringArray(R.array.intro_content);
		GlideDrawableImageViewTarget loadingImageViewTarget = new GlideDrawableImageViewTarget(tutorialImageView);
		Glide.with(getActivity()).load(images[position]).into(loadingImageViewTarget);
		tvTitle.setText(titles[position]);
		tvContent.setText(contentText[position]);
//		if(position == 2) {
//			goBut.setVisibility(View.VISIBLE);
//			goBut.setOnClickListener(new View.OnClickListener(){
//				@Override
//				public void onClick(View v){
//					Intent intent = new Intent(getActivity(), MainActivity.class);
//					startActivity(intent);
//					getActivity().finish();
//					App.getPrefs().edit().putBoolean(Constantaz.PREFS_TUTORIAL, true).apply();
//				}
//			});
//		} else
//			goBut.setVisibility(View.GONE);
		return root;
	}

	private int[] provideImages(){
			return new int[]{R.drawable.ic_intro_slide, R.drawable.ic_intro_slide2, R.drawable.ic_intro_slide3, R.drawable.ic_intro_slide4, R.drawable.ic_intro_slide5};
	}

}
