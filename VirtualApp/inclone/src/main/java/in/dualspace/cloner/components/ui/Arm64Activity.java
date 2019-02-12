package in.dualspace.cloner.components.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import in.dualspace.cloner.AppConstants;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.R;
import in.dualspace.cloner.utils.CommonUtils;

/**
 * Created by guojia on 2018/5/25.
 */

public class Arm64Activity extends BaseActivity {

    private boolean isInstalled;
    private TextView button;
    private TextView hint;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arm64);
        setTitle(R.string.arm64_support);
        hint = (TextView) findViewById(R.id.hint);
        button = (TextView) findViewById(R.id.button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInstalled = DualApp.isSupportPkgExist();
        if (isInstalled) {
            hint.setText(R.string.installed_arm64_support);
            button.setText(R.string.feedback);
        } else {
            hint.setText(R.string.install_arm64_support);
            button.setText(R.string.install);
        }
    }

    public void onButtonClick(View v){
        if (isInstalled) {
            FeedbackActivity.start(this, 0);
        } else {
            CommonUtils.jumpToMarket(this, AppConstants.ARM64_SUPPORT_PKG);
        }
    }
}
