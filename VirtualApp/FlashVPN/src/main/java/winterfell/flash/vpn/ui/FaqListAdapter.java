package winterfell.flash.vpn.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import winterfell.flash.vpn.R;

public class FaqListAdapter extends BaseAdapter {

    private Context mContext;
    private String[] titleArray;
    private String[] answerArray;

    public FaqListAdapter(Context context) {
        mContext = context;
        titleArray = context.getResources().getStringArray(R.array.faqTitle);
        answerArray = context.getResources().getStringArray(R.array.faqAnswer);
    }

    @Override
    public int getCount() {
        return titleArray.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            FaqItem item = new FaqItem();
            item.opened = false;
            item.title = titleArray[i];
            item.answer = answerArray[i];
            view = LayoutInflater.from(mContext).inflate(R.layout.faq_item,null);
            view.setTag(item);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FaqItem item = (FaqItem) view.getTag();
                    item.opened = !item.opened;
                    ImageView imageView = view.findViewById(R.id.icon);
                    TextView answer = view.findViewById(R.id.answer);
                    if(item.opened){
                        imageView.setImageResource(R.drawable.ic_question_answer_grey);
                        answer.setVisibility(View.VISIBLE);
                    }else{
                        imageView.setImageResource(R.drawable.ic_question_answer_blue);
                        answer.setVisibility(View.GONE);
                    }

                }
            });
        }
        FaqItem item = (FaqItem) view.getTag();
        ImageView imageView = view.findViewById(R.id.icon);
        TextView answer = view.findViewById(R.id.answer);
        TextView title = view.findViewById(R.id.title);
        answer.setText(answerArray[i]);
        title.setText(titleArray[i]);
        if(item.opened){
            imageView.setImageResource(R.drawable.ic_question_answer_grey);
            answer.setVisibility(View.VISIBLE);
        }else{
            imageView.setImageResource(R.drawable.ic_question_answer_blue);
            answer.setVisibility(View.GONE);
        }
        return view;
    }

    private class FaqItem {
        boolean opened;
        String title;
        String answer;
    }
}
