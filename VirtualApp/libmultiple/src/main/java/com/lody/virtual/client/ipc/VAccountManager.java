package com.lody.virtual.client.ipc;

import android.accounts.Account;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.IAccountManagerResponse;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.stub.AmsTask;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.IAccountManager;

import java.util.Map;

import static com.lody.virtual.helper.compat.AccountManagerCompat.KEY_ANDROID_PACKAGE_NAME;

/**
 * @author Lody
 */

public class VAccountManager {

    private static VAccountManager sMgr = new VAccountManager();

    private IAccountManager mRemote;

    public static VAccountManager get() {
        return sMgr;
    }

    public IAccountManager getRemote() {
        if (mRemote == null ||
                (!mRemote.asBinder().isBinderAlive() && !VirtualCore.get().isVAppProcess())) {
            synchronized (VAccountManager.class) {
                Object remote = getStubInterface();
                mRemote = LocalProxyUtils.genProxy(IAccountManager.class, remote);
            }
        }
        return mRemote;
    }

	public void clearRemoteInterface() {
		mRemote = null;
	}

    private Object getStubInterface() {
        return IAccountManager.Stub
                .asInterface(ServiceManagerNative.getService(ServiceManagerNative.ACCOUNT));
    }

    public AuthenticatorDescription[] getAuthenticatorTypes() {
        try {
            return getRemote().getAuthenticatorTypes(VUserHandle.myUserId());
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void removeAccount(IAccountManagerResponse response, Account account, boolean expectActivityLaunch) {
        try {
            getRemote().removeAccount(VUserHandle.myUserId(), response, account, expectActivityLaunch);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle loginOptions) {
        try {
            getRemote().getAuthToken(VUserHandle.myUserId(), response, account, authTokenType, notifyOnAuthFailure, expectActivityLaunch, loginOptions);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle extras) {
        try {
            return getRemote().addAccountExplicitly(VUserHandle.myUserId(), account, password, extras);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public Account[] getAccounts(int userId, String type) {
        try {
            VLog.logbug("VAccount", "getAccounts " + type);
            return getRemote().getAccounts(userId, type);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public Account[] getAccounts(String type) {
        try {
            VLog.logbug("VAccount", "getAccounts " + type);
            return getRemote().getAccounts(VUserHandle.myUserId(), type);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public String peekAuthToken(Account account, String authTokenType) {
        try {
            return getRemote().peekAuthToken(VUserHandle.myUserId(), account, authTokenType);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public String getPreviousName(Account account) {
        try {
            return getRemote().getPreviousName(VUserHandle.myUserId(), account);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void hasFeatures(IAccountManagerResponse response, Account account, String[] features) {
        try {
            getRemote().hasFeatures(VUserHandle.myUserId(), response, account, features);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean accountAuthenticated(Account account) {
        try {
            return getRemote().accountAuthenticated(VUserHandle.myUserId(), account);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void clearPassword(Account account) {
        try {
            getRemote().clearPassword(VUserHandle.myUserId(), account);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void renameAccount(IAccountManagerResponse response, Account accountToRename, String newName) {
        try {
            getRemote().renameAccount(VUserHandle.myUserId(), response, accountToRename, newName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setPassword(Account account, String password) {
        try {
            getRemote().setPassword(VUserHandle.myUserId(), account, password);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void addAccount(int userId, IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn) {
        try {
            getRemote().addAccount(userId, response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, optionsIn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void addAccount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn) {
        try {
            getRemote().addAccount(VUserHandle.myUserId(), response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, optionsIn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle loginOptions) {
        try {
            getRemote().updateCredentials(VUserHandle.myUserId(), response, account, authTokenType, expectActivityLaunch, loginOptions);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean removeAccountExplicitly(Account account) {
        try {
            return getRemote().removeAccountExplicitly(VUserHandle.myUserId(), account);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void setUserData(Account account, String key, String value) {
        try {
            getRemote().setUserData(VUserHandle.myUserId(), account, key, value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch) {
        try {
            getRemote().editProperties(VUserHandle.myUserId(), response, accountType, expectActivityLaunch);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getAuthTokenLabel(IAccountManagerResponse response, String accountType, String authTokenType) {
        try {
            getRemote().getAuthTokenLabel(VUserHandle.myUserId(), response, accountType, authTokenType);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void confirmCredentials(IAccountManagerResponse response, Account account, Bundle options, boolean expectActivityLaunch) {
        try {
            getRemote().confirmCredentials(VUserHandle.myUserId(), response, account, options, expectActivityLaunch);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void invalidateAuthToken(String accountType, String authToken) {
        try {
            getRemote().invalidateAuthToken(VUserHandle.myUserId(), accountType, authToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getAccountsByFeatures(IAccountManagerResponse response, String type, String[] features) {
        try {
            VLog.logbug("VAccount", "getAccountsByFeatures " + type);
            getRemote().getAccountsByFeatures(VUserHandle.myUserId(), response, type, features);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setAuthToken(Account account, String authTokenType, String authToken) {
        try {
            getRemote().setAuthToken(VUserHandle.myUserId(), account, authTokenType, authToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public Object getPassword(Account account) {
        try {
            return getRemote().getPassword(VUserHandle.myUserId(), account);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public String getUserData(Account account, String key) {
        try {
            return getRemote().getUserData(VUserHandle.myUserId(), account, key);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    /**
     * Asks the user to add an account of a specified type.  The authenticator
     * for this account type processes this request with the appropriate user
     * interface.  If the user does elect to create a new account, the account
     * name is returned.
     * <p>
     * <p>This method may be called from any thread, but the returned
     * {@link AccountManagerFuture} must not be used on the main thread.
     * <p>
     *
     */
    public AccountManagerFuture<Bundle> addAccount(final int userId, final String accountType,
                                                   final String authTokenType, final String[] requiredFeatures,
                                                   final Bundle addAccountOptions,
                                                   final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        VLog.logbug("VAccount", "addAccount " + accountType);
        if (accountType == null) throw new IllegalArgumentException("accountType is null");
        final Bundle optionsIn = new Bundle();
        if (addAccountOptions != null) {
            optionsIn.putAll(addAccountOptions);
        }
        optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, "android");

        return new AmsTask(activity, handler, callback) {
            @Override
            public void doWork() throws RemoteException {
                addAccount(userId, mResponse, accountType, authTokenType,
                        requiredFeatures, activity != null, optionsIn);
            }
        }.start();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void registerAccountListener(String[] accountTypes, String opPackageName) {
        try {
            getRemote().registerAccountListener(VUserHandle.myUserId(), accountTypes, opPackageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void unregisterAccountListener(String[] accountTypes, String opPackageName) {
        try {
            getRemote().unregisterAccountListener(VUserHandle.myUserId(), accountTypes, opPackageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void startAddAccountSession(IAccountManagerResponse response, String accountType,
                                       String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch,
                                       Bundle options) {
        try {
            getRemote().startAddAccountSession(VUserHandle.myUserId(), response, accountType,
                    authTokenType, requiredFeatures, expectActivityLaunch, options);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void startUpdateCredentialsSession(IAccountManagerResponse response, Account account,
                                              String authTokenType, boolean expectActivityLaunch, Bundle options) {
        try {
            getRemote().startUpdateCredentialsSession(VUserHandle.myUserId(), response, account, authTokenType,
                    expectActivityLaunch, options);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void finishSessionAsUser(IAccountManagerResponse response, Bundle sessionBundle,
                                    boolean expectActivityLaunch, Bundle appInfo, int userId) {
        try {
            getRemote().finishSessionAsUser(VUserHandle.myUserId(), response, sessionBundle,
                    expectActivityLaunch, appInfo, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void isCredentialsUpdateSuggested(IAccountManagerResponse response, Account account,
                                             String statusToken) {
        try {
            getRemote().isCredentialsUpdateSuggested(VUserHandle.myUserId(), response, account, statusToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Map getPackagesAndVisibilityForAccount(Account account) {
        try {
            getRemote().getPackagesAndVisibilityForAccount(VUserHandle.myUserId(), account);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public boolean addAccountExplicitlyWithVisibility(Account account, String password, Bundle extras,
                                                      Map visibility) {
        try {
            getRemote().addAccountExplicitlyWithVisibility(VUserHandle.myUserId(), account, password,
                    extras,
                    visibility);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public boolean setAccountVisibility(Account account, String packageName, int newVisibility) {
        try {
            getRemote().setAccountVisibility(VUserHandle.myUserId(), account, packageName, newVisibility);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public int getAccountVisibility(Account account, String packageName) {
        try {
            getRemote().getAccountVisibility(VUserHandle.myUserId(), account, packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Map getAccountsAndVisibilityForPackage(String packageName, String accountType) {
        try {
            getRemote().getAccountsAndVisibilityForPackage(VUserHandle.myUserId(), packageName, accountType);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
