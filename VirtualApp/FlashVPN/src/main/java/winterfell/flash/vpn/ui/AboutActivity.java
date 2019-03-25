package winterfell.flash.vpn.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import winterfell.flash.vpn.BuildConfig;
import winterfell.flash.vpn.R;
import winterfell.flash.vpn.utils.CommonUtils;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.RemoteConfig;

public class AboutActivity extends BaseActivity {
    private TextView termsTxt;
    private TextView versionTxt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(getString(R.string.about));

        termsTxt = (TextView) findViewById(R.id.terms_txt);
        versionTxt = (TextView) findViewById(R.id.version_info);
        versionTxt.setText("Version " + BuildConfig.VERSION_NAME);
        SpannableString spanText=new SpannableString(getString(R.string.settings_terms_text));
        spanText.setSpan(new ClickableSpan() {

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.black));       //设置文件颜色
                ds.setUnderlineText(true);      //设置下划线
            }

            @Override
            public void onClick(View view) {
                onTermsClick(view);
            }
        }, 0, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanText.setSpan(new ClickableSpan() {

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.black));
                ds.setUnderlineText(true);      //设置下划线
            }

            @Override
            public void onClick(View view) {
                onPrivacyPolicyClick(view);
            }
        }, 21, spanText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsTxt.setText(spanText);
        termsTxt.setHighlightColor(Color.TRANSPARENT);
        termsTxt.setMovementMethod(LinkMovementMethod.getInstance());

    }

    public void onCheckUpdateClick(View view) {
        String forceUpdateUrl = RemoteConfig.getString("force_update_to");
        if (!TextUtils.isEmpty(forceUpdateUrl)) {
            CommonUtils.jumpToUrl(this,forceUpdateUrl);
        } else {
            CommonUtils.jumpToMarket(this, getPackageName());
        }
    }


    public void onPrivacyPolicyClick(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.settings_privacy_policy));
        intent.putExtra(WebViewActivity.EXTRA_URL, "file:///android_asset/privacy_policy.html");
        startActivity(intent);
    }

    public void onTermsClick(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.settings_terms_of_service));
        intent.putExtra(WebViewActivity.EXTRA_URL, "file:///android_asset/term_of_service.html");
        startActivity(intent);
    }
}
