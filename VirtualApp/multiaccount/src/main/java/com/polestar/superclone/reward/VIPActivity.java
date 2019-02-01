package com.polestar.superclone.reward;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.billing.BillingProvider;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.superclone.utils.MLogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by guojia on 2019/1/31.
 */

public class VIPActivity extends BaseActivity implements SkuDetailsResponseListener {
    private static final String EXTRA_FROM = "from";
    public static final int FROM_HOME_TITLE_ICON = 0;
    public static final int FROM_HOME_GIFT_ICON = 0;
    public static final int FROM_HOME_MENU = 1;
    public static final int FROM_SETTING = 2;
    public static final int FROM_TASK_DIALOG= 3;
    private ListView cardListView;
    private List<SkuDetails> skuDetailsList;
    private SubscribeCardAdapter cardAdapter;
    private int[] arrCardColor = new int[]{
            R.color.vip_card_first,R.color.vip_card_second, R.color.vip_card_third
    };

    public static void start(Activity activity, int from) {
        Intent intent = new Intent();
        intent.setClassName(activity, VIPActivity.class.getName());
        intent.putExtra(EXTRA_FROM, from);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_layout);
        setTitle(getString(R.string.user_activity_title));

        cardListView = (ListView) findViewById(R.id.card_list);
        skuDetailsList = new ArrayList<>(0);
        cardAdapter = new SubscribeCardAdapter();
        cardListView.setAdapter(cardAdapter);
        cardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(skuDetailsList.size() > i) {
                    SkuDetails item = skuDetailsList.get(i);
                    BillingProvider.get().getBillingManager().initiatePurchaseFlow(VIPActivity.this,
                            item.getSku(), BillingClient.SkuType.SUBS);
                }
            }
        });
        BillingProvider.get().querySkuDetails(BillingClient.SkuType.SUBS, this);

    }

    private int periodToDay(String s){
        if (!TextUtils.isEmpty(s)){
            String period = s.toLowerCase();
            char unit = period.charAt(period.length() -1 );
            Integer number = Integer.valueOf(period.substring(1, period.length()-1));
            switch (unit){
                case 'w':
                    return number.intValue()*7;
                case 'm':
                    return number.intValue()*30;
                case 'd':
                    return number.intValue();
                case 'y':
                    return number.intValue()*12*30;
                default:
                    return -1;
            }
        }
        return  -1;
    }

    @Override
    protected boolean useCustomTitleBar() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BillingProvider.get().updateStatus(null);
    }

    private long basePrice;

    @Override
    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
        MLogs.d("Billing Query SKU Response Code: " + responseCode);
        Collections.sort(skuDetailsList, new Comparator<SkuDetails>() {
            @Override
            public int compare(SkuDetails skuDetails, SkuDetails t1) {
                return (int) (skuDetails.getPriceAmountMicros() - t1.getPriceAmountMicros());
            }
        });
        this.skuDetailsList = skuDetailsList;
        for(SkuDetails item: this.skuDetailsList) {
            MLogs.d(item.toString());
        }
        if (skuDetailsList.size() > 0) {
            basePrice = skuDetailsList.get(0).getPriceAmountMicros()
                    / periodToDay(skuDetailsList.get(0).getSubscriptionPeriod());
            cardAdapter.notifyDataSetChanged();
        }

    }

    private class SubscribeCardAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return skuDetailsList.size();
        }

        @Override
        public Object getItem(int i) {
            return skuDetailsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(VIPActivity.this).inflate(R.layout.subscribe_card_layout, null);
            }
            View bg = view.findViewById(R.id.background);
            TextView main = view.findViewById(R.id.main_title);
            TextView price = view.findViewById(R.id.price_text);
            TextView trial = view.findViewById(R.id.free_trial);
            TextView discount = view.findViewById(R.id.discount_text);
            int colorIdx = i % arrCardColor.length;
            MLogs.d("colorIdx: " + colorIdx);
            bg.setBackgroundResource(arrCardColor[colorIdx]);
            price.setText(skuDetailsList.get(i).getPrice());
            //main.setText(skuDetailsList.get(i).getTitle());
            int day = periodToDay(skuDetailsList.get(i).getFreeTrialPeriod());
            MLogs.d("days: " + day);
            if (day >= 1) {
                trial.setText(getResources().getString(R.string.free_trial_text, day));
            } else {
                trial.setVisibility(View.INVISIBLE);
            }
            day = periodToDay(skuDetailsList.get(i).getSubscriptionPeriod());
            if (day % 360 == 0){
                main.setText(day/360 + " Year");
            } else if(day % 30 == 0) {
                main.setText(day/30 + " Month");
            } else if (day % 7 == 0) {
                main.setText(day/7 + " Week");
            } else {
                main.setText(day + " Days");
            }
            long dayPrice = skuDetailsList.get(i).getPriceAmountMicros()/day;
            if (dayPrice == basePrice) {
                discount.setVisibility(View.INVISIBLE);
            } else {
                if(basePrice != 0) {
                    long relative = 100 - dayPrice * 100 / basePrice;
                    discount.setText("%" + relative + " OFF");
                }
            }
            return view;
        }
    }
}
