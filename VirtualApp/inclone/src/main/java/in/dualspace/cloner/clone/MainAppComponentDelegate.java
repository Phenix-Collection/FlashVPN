package in.dualspace.cloner.clone;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.polestar.clone.client.hook.delegate.ComponentDelegate;

import in.dualspace.cloner.components.ui.AppLoadingActivity;
import in.dualspace.cloner.components.ui.AppLockActivity;
import in.dualspace.cloner.components.ui.WrapCoverAdActivity;

/**
 * Created by guojia on 2019/2/22.
 */

public class MainAppComponentDelegate extends BaseComponentDelegate{
    @Override
    public void beforeApplicationCreate(Application application) {

    }

    @Override
    public void afterApplicationCreate(Application application) {

    }

    @Override
    public void beforeActivityCreate(Activity activity) {

    }

    @Override
    public void beforeActivityResume(String pkg, int userId) {

    }

    @Override
    public void beforeActivityPause(String pkg, int userId) {

    }

    @Override
    public void beforeActivityDestroy(Activity activity) {

    }

    @Override
    public void afterActivityCreate(Activity activity) {

    }

    @Override
    public void afterActivityResume(Activity activity) {
        String className = activity.getComponentName().getClassName();
        //avoid lock ads
        if (className.startsWith("in.dualspace.cloner")) {
            if (!className.equals(AppLockActivity.class.getName())
                    && !className.equals(AppLoadingActivity.class.getName())
                    && !className.equals(WrapCoverAdActivity.class.getName())) {
                super.afterActivityResume(activity);
            }
        }
    }

    @Override
    public void afterActivityPause(Activity activity) {
        String className = activity.getComponentName().getClassName();
        if (className.startsWith("in.dualspace.cloner")) {
            if (!className.equals(AppLockActivity.class.getName())
                    && !className.equals(AppLoadingActivity.class.getName())
                    && !className.equals(WrapCoverAdActivity.class.getName())) {
                super.afterActivityPause(activity);
            }
        }
    }

    @Override
    public void afterActivityDestroy(Activity activity) {

    }

    @Override
    public void onSendBroadcast(Intent intent) {

    }

    @Override
    public boolean isNotificationEnabled(String pkg, int userId) {
        return false;
    }

    @Override
    public void reloadSetting(String lockKey, boolean adFree, long lockInterval, boolean quickSwitch) {

    }

    @Override
    public boolean handleStartActivity(String name) {
        return false;
    }
}
