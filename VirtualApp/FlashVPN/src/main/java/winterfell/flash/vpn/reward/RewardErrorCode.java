package winterfell.flash.vpn.reward;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.polestar.task.ADErrorCode;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.FlashUser;
import winterfell.flash.vpn.R;

/**
 * Created by guojia on 2019/3/21.
 */
final public class RewardErrorCode {
    private static final int REWARD_ERROR_CODE_BASE = ADErrorCode.MAX_SERVER_ERR_CODE + 1000;
    public static final int TASK_OK = REWARD_ERROR_CODE_BASE;
    public static final int TASK_EXCEED_DAY_LIMIT = TASK_OK + 1;
    public static final int TASK_AD_NO_FILL = TASK_OK + 2;
    public static final int TASK_UNEXPECTED_ERROR = TASK_OK + 3;
    public static final int TASK_AD_LOADING = TASK_OK + 4;
    public static final int TASK_SUBMIT_CODE_OK = TASK_OK + 5;
    public static final int TASK_CODE_ALREADY_SUBMITTED= TASK_OK + 6;

    public static final int PRODUCT_OK = REWARD_ERROR_CODE_BASE + 1000;
    public static final int PRODUCT_NO_ENOUGH_COIN = PRODUCT_OK + 1;


    public static final String getToastMessage(int code, Object ... args) {
        return getToastMessage(FlashApp.getApp(), code, args);
    }

    public static final String getToastMessage(Context context, int code, Object ... args) {
        switch (code) {
            case TASK_AD_NO_FILL:
                return context.getString(R.string.error_ad_no_fill);
            case TASK_EXCEED_DAY_LIMIT:
            case ADErrorCode.DAY_LIMITTED:
            case ADErrorCode.TOTAL_LIMITTED:
                return context.getString(R.string.error_day_limit);
            case TASK_OK:
                if (args.length == 1) {
                    float payment = (float) args[0];
                    if (payment > 0) {
                        return context.getString(R.string.task_ok_with_coin,
                                FlashUser.getInstance().coinToTimeString(payment));
                    }
                } else {
                    return context.getString(R.string.task_ok);
                }
//            case TASK_SUBMIT_CODE_OK:
//                return context.getString(R.string.submit_code_ok);
//            case TASK_CODE_ALREADY_SUBMITTED:
//            case ADErrorCode.ALREADY_REFERRED:
//            case ADErrorCode.INVALID_REFERRAL_CODE:
//                return context.getString(R.string.submit_code_fail);
//            case PRODUCT_OK:
//                if (args.length == 1) {
//                    float amount = (float) args[0];
//                    return context.getString(R.string.consume_coins, amount);
//                } else {
//                    return context.getString(R.string.product_ok);
//                }
//            case ADErrorCode.NOT_ENOUGH_MONEY:
//            case PRODUCT_NO_ENOUGH_COIN:
//                return context.getString(R.string.no_enough_coin);
//            case ADErrorCode.PRODUCT_NOTEXIST:
//                return context.getString(R.string.product_not_exist);
            default:
                break;
        }
        return context.getString(R.string.error_unexpected);
    }

    public static void toastMessage(Context context, int code, Object... args) {
        String msg = RewardErrorCode.getToastMessage(code, args);
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(context,
                    RewardErrorCode.getToastMessage(code, args), Toast.LENGTH_SHORT).show();
        }

    }
}