package in.dualspace.cloner.clone;

import com.polestar.clone.client.core.IAppApiDelegate;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.R;

/**
 * Created by DualApp on 2017/7/16.
 */

public class CloneApiDelegate implements IAppApiDelegate {


    @Override
    public String getCloneTagedLabel(String label) {
        return label == null? DualApp.getApp().getResources().getString(R.string.app_name) :
                DualApp.getApp().getResources().getString(R.string.clone_label_tag, label);
    }
}
