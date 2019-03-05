package winterfell.flash.vpn.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import winterfell.flash.vpn.R;

/**
 * Created by yxx on 2016/7/26.
 */
public class WebViewActivity extends BaseActivity {

    public final static String EXTRA_URL = "extra_url";
    public final static String EXTRA_TITLE = "extra_title";

    private WebView webView;
    private ProgressBar progressBar;
    private String url;
    private String title;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initData();
        initView();
    }

    private void initData(){
        Intent intent = getIntent();
        if(intent != null){
            url = intent.getStringExtra(EXTRA_URL);
            title = intent.getStringExtra(EXTRA_TITLE);
        }
    }

    private void initView(){
        if(!TextUtils.isEmpty(title)){
            setTitle(title);
        }
        webView = (WebView) findViewById(R.id.webview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        webView.requestFocus();
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setBuiltInZoomControls(false);
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSaveFormData(true);
        s.setDomStorageEnabled(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccess(true);
        s.setDomStorageEnabled(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.e("test",newProgress + "");
                if (newProgress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    if (View.INVISIBLE == progressBar.getVisibility()) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        webView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
                return false;
            }
        });
        if(url != null){
            webView.loadUrl(url);
        }
    }
}
