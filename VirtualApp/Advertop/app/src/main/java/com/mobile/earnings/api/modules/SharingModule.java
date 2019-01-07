package com.mobile.earnings.api.modules;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.listeners.VkPostRequestListener;
import com.facebook.share.model.ShareLinkContent;
import com.google.android.gms.plus.PlusShare;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;

public class SharingModule{

	public static ShareLinkContent shareLinkThroughFacebook(Uri link, String contentTitle, String contentDescription){
		return new ShareLinkContent.Builder().setContentUrl(link).setContentDescription(contentDescription).build();
	}

	public static Intent shareThroughGoogle(Activity activity, String message, Uri link){
		return new PlusShare.Builder(activity).setType("text/plain").setText(message).setContentUrl(link).getIntent();
	}

	public static void shareThroughVkifLoggedIn(final Activity activity, final String message, final String scope){
		if(VKSdk.isLoggedIn()) {
			if(VKSdk.wakeUpSession(activity)) {
				vkShare(message);
				Toast.makeText(activity, App.getRes().getString(R.string.friends_vk_share_toast_text), Toast.LENGTH_SHORT).show();
			}
		} else
			VKSdk.login(activity, scope);
	}

	public static void vkShare(String message){
		final VKRequest request = new VKRequest("wall.post", VKParameters.from(VKApiConst.MESSAGE, message));
		request.executeWithListener(new VkPostRequestListener());
	}
}
