package mochat.multiple.parallel.whatsclone;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.lody.virtual.client.core.IAppApiDelegate;
import mochat.multiple.parallel.whatsclone.utils.BitmapUtils;

/**
 * Created by guojia on 2016/12/19.
 */

public class AppApiDelegate implements IAppApiDelegate {
    @Override
    public String getCloneTagedLabel(String label) {
        return label == null? MApp.getApp().getResources().getString(R.string.app_name) :
                MApp.getApp().getResources().getString(R.string.clone_label_tag, label);
    }
}
