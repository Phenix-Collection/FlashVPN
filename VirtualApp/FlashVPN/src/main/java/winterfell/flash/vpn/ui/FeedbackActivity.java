package winterfell.flash.vpn.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import winterfell.flash.vpn.BuildConfig;
import winterfell.flash.vpn.R;
import winterfell.flash.vpn.utils.MLogs;


public class FeedbackActivity extends BaseActivity {

    private Context mContext;
    private EditText mEtFeedback;
    private Button mBtSubmit;
    private TextView mGoFAQ;

    private static final String EXTRA_RATING = "extra_rating";
    private int rating;

    public static void start(Context activity, int rating) {
        Intent intent = new Intent(activity, FeedbackActivity.class);
        intent.putExtra(FeedbackActivity.EXTRA_RATING, rating);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        mContext = this;
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.feedback));

        mEtFeedback = (EditText) findViewById(R.id.et_feedback);
        mBtSubmit = (Button) findViewById(R.id.bt_submit);
        mGoFAQ = (TextView) findViewById(R.id.tv_go_faq);
        mBtSubmit.setEnabled(false);

        SpannableString spanText=new SpannableString(getString(R.string.feedback_go_faq));
        spanText.setSpan(new ClickableSpan() {

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);       //设置文件颜色
                ds.setUnderlineText(true);      //设置下划线
            }

            @Override
            public void onClick(View view) {
                FaqActivity.start(FeedbackActivity.this);
            }
        }, spanText.length() - 4, spanText.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mGoFAQ.setText(spanText);
        mGoFAQ.setHighlightColor(Color.TRANSPARENT);
        mGoFAQ.setMovementMethod(LinkMovementMethod.getInstance());

        mEtFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    mBtSubmit.setEnabled(true);
                } else {
                    mBtSubmit.setEnabled(false);
                }
            }
        });

        mBtSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEtFeedback.getText().toString();
                if (content == null) {
                    Toast.makeText(mContext,R.string.feedback_no_description, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent data=new Intent(Intent.ACTION_SENDTO);
                data.setData(Uri.parse("mailto:winterfell.applab@gmail.com"));
                data.putExtra(Intent.EXTRA_SUBJECT, "Feedback about Flash VPN");
                String fullContent = content + "\n\n\n\n"  + "Additional Info: \n" + "Rating: "+ rating +  "Flash VPN version: " + BuildConfig.VERSION_NAME
                        + "\n" + "Model info: " + Build.FINGERPRINT + "\nGMS state: "+ "\n";


                data.putExtra(Intent.EXTRA_TEXT, fullContent);
                try {
                    startActivity(data);
                }catch (Exception e) {
                    MLogs.e("Start email activity fail!");
                    Toast.makeText(mContext,R.string.submit_success, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
