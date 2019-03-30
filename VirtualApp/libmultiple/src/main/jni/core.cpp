//
// VirtualApp Native Project
//
#include "core.h"
#include <signal.h>
//int sigaction(int signum,const struct sigaction *act,struct sigaction *oldact);
const int handledSignals[] = {
        SIGSEGV, SIGABRT, SIGFPE, SIGILL, SIGBUS
};
const int handledSignalsNum = sizeof(handledSignals) / sizeof(handledSignals[0]);
struct sigaction old_handlers[handledSignalsNum];

JavaVM *g_vm;
jclass g_jclass;
//notifyNativeCrash  = (*env)->GetMethodID(env, cls,  "notifyNativeCrash", "()V");
void my_sigaction(int signal, siginfo_t *info, void *reserved) {
    // Here catch the native crash
    extern JavaVM *g_vm;
    extern jclass g_jclass;
    JNIEnv *env = NULL;
    g_vm->GetEnv((void **) &env, JNI_VERSION_1_4);
    g_vm->AttachCurrentThread(&env, NULL);
    jmethodID notifyNativeCrash = env->GetStaticMethodID(g_jclass, "notifyNativeCrash", "(I)V");
    env->CallStaticVoidMethod(g_jclass, notifyNativeCrash, signal);
}

int nativeCrashHandler_onLoad(JNIEnv *env) {
    struct sigaction handler;
    //memset(&handler, 0, sizeof(sigaction));
    handler.sa_sigaction = my_sigaction;
    handler.sa_flags = SA_RESETHAND;

    for (int i = 0; i < handledSignalsNum; ++i) {
        sigaction(handledSignals[i], &handler, &old_handlers[i]);
    }

    return 1;
}



void hook_native(JNIEnv *env, jclass jclazz, jobjectArray javaMethods, jstring packageName, jboolean isArt, jint apiLevel, jint cameraMethodType) {
    static bool hasHooked = false;
    if (hasHooked) {
        return;
    }
    patchAndroidVM(javaMethods, packageName, isArt, apiLevel, cameraMethodType);
    nativeCrashHandler_onLoad(env);
    hasHooked = true;
}

void readOnly(JNIEnv *env, jclass jclazz, jstring _path) {
    const char *path = env->GetStringUTFChars(_path, NULL);
    IOUniformer::readOnly(path);
}

void whiteList(JNIEnv *env, jclass jclazz, jstring _path) {
    const char *path = env->GetStringUTFChars(_path, NULL);
    IOUniformer::whiteList(path);
}


void hook_io(JNIEnv *env, jclass jclazz, jint apiLevel) {
    static bool hasHooked = false;
    if (hasHooked) {
        return;
    }
    IOUniformer::startUniformer(apiLevel);
    hasHooked = true;
}

void redirect(JNIEnv *env, jclass jclazz, jstring orgPath, jstring newPath) {
    const char *org_path = env->GetStringUTFChars(orgPath, NULL);
    const char *new_path = env->GetStringUTFChars(newPath, NULL);
    IOUniformer::redirect(org_path, new_path);
}

jstring query(JNIEnv *env, jclass jclazz, jstring orgPath) {
    const char *org_path = env->GetStringUTFChars(orgPath, NULL);
    const char *redirected_path = IOUniformer::query(org_path);
    return env->NewStringUTF(redirected_path);
}

jstring restore(JNIEnv *env, jclass jclazz, jstring redirectedPath) {
    const char *redirected_path = env->GetStringUTFChars(redirectedPath, NULL);
    const char *org_path = IOUniformer::restore(redirected_path);
    return env->NewStringUTF(org_path);
}



static JNINativeMethod gMethods[] = {
        NATIVE_METHOD((void *) hook_io,  "nativeHook",                  "(I)V"),
        NATIVE_METHOD((void *) redirect, "nativeRedirect",              "(Ljava/lang/String;Ljava/lang/String;)V"),
        NATIVE_METHOD((void *) query,    "nativeGetRedirectedPath",     "(Ljava/lang/String;)Ljava/lang/String;"),
        NATIVE_METHOD((void *) restore,  "nativeRestoreRedirectedPath", "(Ljava/lang/String;)Ljava/lang/String;"),
        NATIVE_METHOD((void *) readOnly, "nativeReadOnly", "(Ljava/lang/String;)V"),
        NATIVE_METHOD((void *) whiteList, "nativeWhiteList", "(Ljava/lang/String;)V"),
        NATIVE_METHOD((void *) hook_native, "nativeHookNative", "(Ljava/lang/Object;Ljava/lang/String;ZII)V"),
};



JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    jclass javaClass = env->FindClass(JAVA_CLASS);
    if (javaClass == NULL) {
        LOGE("Error: Unable to find the NativeEngine class.");
        return JNI_ERR;
    }
    if (env->RegisterNatives(javaClass, gMethods, NELEM(gMethods)) < 0) {
        LOGE("Error: Unable to register the native methods.");
        return JNI_ERR;
    }
    g_vm = vm;
    g_jclass = (jclass) env->NewGlobalRef(javaClass);
    env->DeleteLocalRef(javaClass);
    return JNI_VERSION_1_6;
}



JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    env->DeleteGlobalRef((jobject)g_vm);
    env->DeleteGlobalRef((jobject)g_jclass);
}

