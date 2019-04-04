package com.polestar.clone.client.env;

import android.content.Intent;

import com.polestar.clone.client.stub.ShortcutHandleActivity;

/**
 * @author Lody
 *
 */
public class Constants {

	public static final String EXTRA_USER_HANDLE = "android.intent.extra.user_handle";
	/**
	 * Install shortcut action
	 */
	public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
	/**
	 * Uninstall shortcut action
	 */
	public static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";
	/**
	 * Package name of System-UI.apk
	 */
	public static final String SYSTEM_UI_PKG = "com.android.systemui";
	/**
	 * If an apk declared the "fake-signature" attribute on its Application TAG,
	 * we will use its signature instead of the real signature.
	 *
	 * For more detail, please see :
	 * https://github.com/microg/android_packages_apps_GmsCore/blob/master/
	 * patches/android_frameworks_base-M.patch.
	 */
	public static final String FEATURE_FAKE_SIGNATURE = "fake-signature";
	public static final String VIRTUAL_PROTECT_INTENT_PREFIX = "virtual.";
	public static final String ACTION_PACKAGE_ADDED = "virtual." + Intent.ACTION_PACKAGE_ADDED;
	public static final String ACTION_PACKAGE_REMOVED = "virtual." + Intent.ACTION_PACKAGE_REMOVED;
	public static final String ACTION_PACKAGE_CHANGED = "virtual." + Intent.ACTION_PACKAGE_CHANGED;
	public static final String ACTION_USER_ADDED = "virtual." + "android.intent.action.USER_ADDED";
	public static final String ACTION_USER_REMOVED = "virtual." + "android.intent.action.USER_REMOVED";
	public static final String ACTION_USER_INFO_CHANGED = "virtual." + "android.intent.action.USER_CHANGED";
	public static final String ACTION_USER_STARTED = "Virtual." + "android.intent.action.USER_STARTED";
	public static String META_KEY_IDENTITY = "X-Identity";
	public static String META_VALUE_STUB = "Stub-User";
	/**
	 * Server process name of VA
	 */
	public static String SERVER_PROCESS_NAME = ":x";
	/**
	 * The activity who handle the shortcut.
	 */
	public static String SHORTCUT_PROXY_ACTIVITY_NAME = ShortcutHandleActivity.class.getName();

	public static final String VA_INTENT_KEY_USERID = "_VA_|_user_id_";
	public static final String VA_INTENT_KEY_COMPONENT = "_VA_|_component_";
	public static final String VA_INTENT_KEY_INTENT = "_VA_|_intent_";
	public static final String VA_INTENT_KEY_BRDATA = "_PL_|_data_";
	public static final String VA_INTENT_KEY_PACKAGE = "_VA_|_creator_";
	public static final String VA_INTENT_KEY_COMPONENT_ACTION_FMT = "_VA_%s_%s";
	public static final String ACTION_PROXY_BROADCAST = "virtual.android.intent.action.PendingIntent";
}
