package com.mobile.earnings.utils;

import android.widget.EditText;

import java.util.List;

public class TextUtils{

	public static String getText(EditText eText){
		return eText.getText().toString();
	}

	public static String getRightUri(String imageUri){
		String urlPrefix = "http:";
		if(imageUri != null) {
			if(imageUri.startsWith(urlPrefix)) {
				return imageUri;
			} else{
				return urlPrefix.concat(imageUri);
			}
		} else {
			return "";
		}
	}

	public static String provideKeywords(List<String> keywords){
		if(keywords == null || keywords.isEmpty())
			return "";
		StringBuilder builder = new StringBuilder();
		for(String keyword : keywords)
			builder.append(keyword.concat(" или "));
		return builder.substring(0, builder.length() - 5);
	}
}
