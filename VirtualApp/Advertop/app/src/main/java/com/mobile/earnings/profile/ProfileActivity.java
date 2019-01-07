package com.mobile.earnings.profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseActivity;
import com.mobile.earnings.api.responses.UserResponse;
import com.mobile.earnings.profile.presentation.ProfilePresenter;
import com.mobile.earnings.profile.presentation.ProfileView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;




public class ProfileActivity extends BaseActivity implements ProfileView, DatePickerDialog.OnDateSetListener {

    @BindView(R.id.profile_activity_user_id)
    TextView          idTv;
    @BindView(R.id.profile_activity_name_et)
    AppCompatEditText nameEt;
    @BindView(R.id.profile_activity_email_et)
    AppCompatEditText emailEt;
    @BindView(R.id.profile_activity_number_et)
    AppCompatEditText phoneNumberEt;
    @BindView(R.id.profile_activity_date_selection_but)
    TextView          selectDateBut;
    @BindView(R.id.profile_switch)
    AppCompatSpinner  genderSpinner;

    private ProfilePresenter presenter;

    private String mGender = "male";
    private int mBirthYear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        presenter = new ProfilePresenter(this);
        setSupportActionBar(setToolBar(getString(R.string.menu_profile)));
        displayHomeAsUpEnabled(true);
        presenter.getUserData();
        idTv.setText(getString(R.string.profileAct_idTitle, App.getDeviceID()));
    }

    @Override
    public void informUser(int resourceId) {
        Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mBirthYear = year;
        selectDateBut.setText(String.valueOf(mBirthYear));
    }

    @Override
    public void setProfileData(UserResponse.UserInfo info) {
        nameEt.setText(info.name);
        emailEt.setText(info.email);
        phoneNumberEt.setText(info.phoneNumber);
        Calendar calendar = Calendar.getInstance();
        int yearOfBirth = calendar.get(Calendar.YEAR) - info.age;
        selectDateBut.setText(String.valueOf(yearOfBirth));
        if (info.gender.contentEquals("male")) {
            genderSpinner.setSelection(0);
        } else {
            genderSpinner.setSelection(1);
        }
    }

    @OnItemSelected(R.id.profile_switch)
    void genderSelected(AdapterView<?> parent, View view, int position) {
        if (position == 1) {
            mGender = "female";
        } else {
            mGender = "male";
        }
    }

    @OnClick(R.id.profile_activity_date_selection_but)
    void onDateSelectButClick() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(this, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dpd.show(getFragmentManager(), "DatePickerDialog");
    }

    @OnClick(R.id.profile_activity_done_but)
    void onDoneButClick() {
        String name = nameEt.getText().toString();
        String email = emailEt.getText().toString();
        String phoneNumber = phoneNumberEt.getText().toString();
        presenter.updateProfileData(name, email, phoneNumber, mBirthYear, mGender);
    }

    @OnClick(R.id.profile_activity_user_id)
    void copyUserId() {
        copyUserIdToBuffer(App.getDeviceID());
    }

    private void copyUserIdToBuffer(String userId) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("user_id", userId);
        clipboard.setPrimaryClip(clip);
        informUser(R.string.profileAct_idCopiedMessage);
    }

}
