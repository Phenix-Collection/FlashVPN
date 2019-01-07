package com.mobile.earnings.autorization;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseActivity;
import com.mobile.earnings.api.listeners.OnRegisterListener;
import com.mobile.earnings.api.modules.RegisterModule;
import com.mobile.earnings.api.responses.AuthorizationResponse;
import com.mobile.earnings.autorization.presentersImpl.RegisterPresenterImpl;
import com.mobile.earnings.autorization.views.RegisterView;
import com.mobile.earnings.utils.Constantaz;
import com.mobile.earnings.utils.ReportEvents;
import com.yandex.metrica.YandexMetrica;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mobile.earnings.autorization.LoginActivity.getLoginIntent;
import static com.mobile.earnings.main.MainActivity.getMainIntent;
import static com.mobile.earnings.tutorial.IntroActivity.getIntroIntent;
import static com.mobile.earnings.utils.Constantaz.EXTRA_CITY_NAME;
import static com.mobile.earnings.utils.Constantaz.EXTRA_LAT;
import static com.mobile.earnings.utils.Constantaz.EXTRA_LON;
import static com.mobile.earnings.utils.Constantaz.POLICY_LINK;

public class RegisterActivity extends BaseActivity implements RegisterView, OnRegisterListener {

    @BindView(R.id.registerIfTV)
    Button            signInTV;
    @BindView(R.id.registerPromodET)
    AppCompatEditText promoCodeET;
    @BindView(R.id.registerAct_policyText)
    TextView          policyTV;

    private RegisterPresenterImpl presenter;
    private RegisterModule        module;
    private String                cityName;
    private float                 lat, lon;

    public static Intent getRegisterIntent(@NonNull Context context, @NonNull String cityName, float lat, float lon) {
        Intent intent = new Intent(context, RegisterActivity.class);
        intent.putExtra(EXTRA_CITY_NAME, cityName);
        intent.putExtra(EXTRA_LAT, lat);
        intent.putExtra(EXTRA_LON, lon);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        module = new RegisterModule();
        module.setOnRegisterSuccessListener(this);
        presenter = new RegisterPresenterImpl(this);

        cityName = getIntent().getStringExtra(EXTRA_CITY_NAME);
        lat = getIntent().getFloatExtra(EXTRA_LAT, 0f);
        lon = getIntent().getFloatExtra(EXTRA_LON, 0f);

        setUpPolicy();
        setUpSignInBut();
    }

    @Override
    public void informUser(int resourceId) {
        Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess(AuthorizationResponse model) {
        if (App.getPrefs().getBoolean(Constantaz.PREFS_TUTORIAL, false)) {
            startFinishingActivity(getMainIntent(this));
        } else {
            startFinishingActivity(getIntroIntent(this));
        }
    }

    @Override
    public void onError(@StringRes int resourceId) {
        DialogInterface.OnClickListener onPositiveButClicked = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DefaultDialogStyle);
        dialog.setMessage(resourceId).setPositiveButton(android.R.string.ok, onPositiveButClicked).create();
        if (!isFinishing()) {
            dialog.show();
        }
    }

    @OnClick(R.id.registerBut)
    void registerButClicked() {
        YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_REGISTER);
        String promoCode = promoCodeET.getText().toString();
        module.registerUser(promoCode.isEmpty() ? null : promoCode, "", "", cityName, lat, lon, "", "", 0, "");
    }

    @OnClick(R.id.registerAct_policyText)
    void showPolicy() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(POLICY_LINK));
            startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            informUser(R.string.registerAct_browserActNotFoundException);
        }
    }

    private void setUpPolicy() {
        //noinspection deprecation using old API
        policyTV.setText(Html.fromHtml(getString(R.string.registerAct_policy)));
    }

    private void setUpSignInBut() {
        //noinspection deprecation using old API
        signInTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_SIGN_IN);
                startActivity(getLoginIntent(RegisterActivity.this, cityName, lat, lon));
            }
        });
    }

}
