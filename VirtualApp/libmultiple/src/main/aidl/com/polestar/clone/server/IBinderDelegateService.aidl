// IBinderDelegateService.aidl
package com.polestar.clone.server;

import android.content.ComponentName;

interface IBinderDelegateService {

   ComponentName getComponent();

   IBinder getService();

}
