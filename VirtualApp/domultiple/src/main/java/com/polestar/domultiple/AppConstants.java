package com.polestar.domultiple;

public class AppConstants {

    public static boolean IS_RELEASE_VERSION;

    public static final String DEVELOP_CHANNEL = "develop";

    public static final int REQUEST_SELECT_APP = 5;
    public static final int REQUEST_INSTALL_APP = 6;

    public static final int HOME_GRID_COLUMN = 3;

    public static final String EXTRA_APP_MODEL = "com.polestar.applab.utils.extra.APP_MODEL";

    public static final String KEY_CLONED_APPS = "Cloned Apps";

    public static final String EXTRA_CLONED_APP_PACKAGENAME = "app_packagename";
    public static final String EXTRA_FROM = "From where";
    public static final String EXTRA_IS_INSTALL_SUCCESS = "is install success";
    public static final String VALUE_FROM_SHORTCUT = "From shortcut";
    public static final String VALUE_FROM_HOME = "From home";

    public static final int APP_ICON_WIDTH = 50;//dp
    public static final int APP_ICON_RADIUS = 12;//dp
    public static final int APP_ICON_PADDING = 5;//dp

    public static final String PREFERENCE_NAME = "DoMultiple-pref";
    public static final String KEY_AUTO_CREATE_SHORTCUT = "key_auto_create_shortcut";

    public class PreferencesKey {
        public static final String SHOWN_CLONE_GUIDE = "shown_clone_guide";
        public static final String SHOWN_LONG_CLICK_GUIDE = "show_guide_for_long_press";
        public static final String LOCKER_FEATURE_ENABLED = "locker_feature_enabled";
        public static final String ENCODED_PATTERN_PWD = "encoded_pattern_pwd";
        public static final String IS_LOCKER_SCREEN = "is_locker_screen";
        public static final String SAFE_QUESTION_ANSWER = "safe_question_answer";
        public static final String CUSTOMIZED_SAFE_QUESTION = "custom_safe_ques";
        public static final String APP_LOCK_INVISIBLE_PATTERN_PATH = "app_lock_invisible_pattern_path";
        public static final String SAFE_QUESTION_ID = "safe_question_id";
        public static final String NOTIFICATION_MASTER_SWITCH = "notification_all";
    }

    public class AppLockState{
        public static final int DISABLED = 0;
        public static final int ENABLED_FOR_CLONE = 1;
        public static final int ENABLED_FOR_BOTH = 2;
    }

    public class CrashTag {
        public static final int MAPP_CRASH = 33117;
        public static final int CLONE_CRASH = 32459;
        public static final int SERVER_CRASH = 41781;
        public static final int FG_CRASH = 41784;
        public static final int NATIVE_CRASH = 41784;

    }

    public static final String WALL_UNIT_ID = "8442";
}