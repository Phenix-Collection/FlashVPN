//
// VirtualApp Native Project
//
#include "HookNative.h"

typedef void (*Bridge_DalvikBridgeFunc)(const void **, void *, const void *, void *);
typedef jobject (*Native_openDexNativeFunc)(JNIEnv *, jclass, jstring, jstring, jint);
typedef jobject (*Native_openDexNativeFunc_N)(JNIEnv *, jclass, jstring, jstring, jint, jobject, jobject);
typedef jint (*Native_getCallingUid)(JNIEnv *, jclass);
typedef jint (*Native_cameraNativeSetup21)(JNIEnv *env, jobject thiz,
                                         jobject weak_this, jint cameraId, jint version, jstring clientPackageName);



static struct {

    bool isArt;
    int nativeOffset;
    jclass g_binder_classs;
    jmethodID g_methodid_onGetCallingUid;
    jmethodID g_methodid_onOpenDexFileNative;

    void* art_work_around_app_jni_bugs;
    char* (*GetCstrFromString)(void *);
    void* (*GetStringFromCstr)(const char*);


    void* g_sym_IPCThreadState_self;
    void* g_sym_IPCThreadState_getCallingUid;

    Native_getCallingUid orig_getCallingUid;
    void* orig_cameraNativeSetup;

    Bridge_DalvikBridgeFunc orig_openDexFile_dvm;
    union {
        Native_openDexNativeFunc beforeN;
        Native_openDexNativeFunc_N afterN;
    } orig_native_openDexNativeFunc;


} gOffset;


extern JavaVM *g_vm;
extern jclass g_jclass;


void mark() {
    // Do nothing
};

jint getCallingUid(JNIEnv *env, jclass jclazz) {
    jint uid;
    if (gOffset.isArt) {
        uid = gOffset.orig_getCallingUid(env, jclazz);
    } else {
        int (*org_getCallingUid)(int) = (int (*)(int)) gOffset.g_sym_IPCThreadState_getCallingUid;
        int (*func_self)(void) = (int (*)(void)) gOffset.g_sym_IPCThreadState_self;
        uid = org_getCallingUid(func_self());
    }
    uid = env->CallStaticIntMethod(g_jclass, gOffset.g_methodid_onGetCallingUid, uid);
    return uid;
}

//jstring CStr2Jstring( JNIEnv* env, const char* pat )
//{
//    //定义Java String类 strClass
//    jclass strClass = (env)->FindClass("Ljava/lang/String;");
//    //获取Java String类方法String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
//    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
//    //建立byte数组
//    jbyteArray bytes = (env)->NewByteArray((jsize)strlen(pat));
//    //将char* 转换为byte数组
//    (env)->SetByteArrayRegion(bytes, 0, (jsize)strlen(pat), (jbyte*)pat);
//    //设置String, 保存语言类型,用于byte数组转换至String时的参数
//    jstring encoding = (env)->NewStringUTF("GB2312");
//    //将byte数组转换为java String,并输出
//    return (jstring)(env)->NewObject(strClass, ctorID, bytes, encoding);
//
//}
//
//jstring clientStringFromStdString(JNIEnv *env,const std::string &str){
////    return env->NewStringUTF(str.c_str());
//    jbyteArray array = env->NewByteArray(str.size());
//    env->SetByteArrayRegion(array, 0, str.size(), (const jbyte*)str.c_str());
//    jstring strEncode = env->NewStringUTF("UTF-8");
//    jclass cls = env->FindClass("java/lang/String");
//    jmethodID ctor = env->GetMethodID(cls, "<init>", "([BLjava/lang/String;)V");
//    jstring object = (jstring) env->NewObject(cls, ctor, array, strEncode);
//    return object;
//}

void cameraNativeSetup(const void **args, void *pResult, const void *method, void *self) {
    JNIEnv *env = NULL;
    g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    g_vm->AttachCurrentThread(&env, NULL);

    typedef char* (*GetCstrFromString)(void *);
    typedef void* (*GetStringFromCstr)(const char*);

  //const char* origPkg3 = args[3] == NULL ? NULL : gOffset.GetCstrFromString((void*) args[3]);

    args[3] = gOffset.GetStringFromCstr("com.polestar.multiaccount");
    void (*func)(const void **, void *, const void *, void *) = (void (*)(const void **, void *, const void *, void *)) gOffset.orig_cameraNativeSetup;
    return func(args, pResult, method, self);

}

jint cameraNativeSetup21(JNIEnv *env, jobject thiz,
                       jobject weak_this, jint cameraId, jint version, jstring clientPackageName) {
    LOGD("PLIB cameraNativeSetup cameraId %d version %d", cameraId, version);
    jstring pkg = env->NewStringUTF("com.polestar.multiaccount");
    Native_cameraNativeSetup21 func = (Native_cameraNativeSetup21)gOffset.orig_cameraNativeSetup;
    int ret = func(env, thiz,weak_this, cameraId, version, pkg);
    LOGD("ret %d", ret);
    return ret;

}

static JNINativeMethod gMarkMethods[] = {
        NATIVE_METHOD((void *) mark, "nativeMark", "()V"),
};

JNINativeMethod gUidMethods[] = {
        NATIVE_METHOD((void *) getCallingUid, "getCallingUid", "()I"),
};

static jobject new_native_openDexNativeFunc(JNIEnv* env, jclass jclazz, jstring javaSourceName, jstring javaOutputName, jint options) {
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray array = env->NewObjectArray(2, stringClass, NULL);

    if (javaSourceName) {
        env->SetObjectArrayElement(array, 0, javaSourceName);
    }
    if (javaOutputName) {
        env->SetObjectArrayElement(array, 1, javaOutputName);
    }
    env->CallStaticVoidMethod(g_jclass, gOffset.g_methodid_onOpenDexFileNative, array);

    jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
    jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

    return gOffset.orig_native_openDexNativeFunc.beforeN(env, jclazz, newSource, newOutput, options);
}

static jobject new_native_openDexNativeFunc_N(JNIEnv* env, jclass jclazz, jstring javaSourceName, jstring javaOutputName, jint options, jobject loader, jobject elements) {
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray array = env->NewObjectArray(2, stringClass, NULL);

    if (javaSourceName) {
        env->SetObjectArrayElement(array, 0, javaSourceName);
    }
    if (javaOutputName) {
        env->SetObjectArrayElement(array, 1, javaOutputName);
    }
    env->CallStaticVoidMethod(g_jclass, gOffset.g_methodid_onOpenDexFileNative, array);

    jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
    jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

    return gOffset.orig_native_openDexNativeFunc.afterN(env, jclazz, newSource, newOutput, options, loader, elements);
}



static void new_bridge_openDexNativeFunc(const void **args, void *pResult, const void *method, void *self) {
    JNIEnv *env = NULL;
    g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    g_vm->AttachCurrentThread(&env, NULL);

    typedef char* (*GetCstrFromString)(void *);
    typedef void* (*GetStringFromCstr)(const char*);

    const char* source = args[0] == NULL ? NULL : gOffset.GetCstrFromString((void*) args[0]);
    const char* output = args[1] == NULL ? NULL : gOffset.GetCstrFromString((void*) args[1]);

    jstring orgSource = source == NULL ? NULL : env->NewStringUTF(source);
    jstring orgOutput = output == NULL ? NULL : env->NewStringUTF(output);

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray array = env->NewObjectArray(2, stringClass, NULL);
    if (orgSource) {
        env->SetObjectArrayElement(array, 0, orgSource);
    }
    if (orgOutput) {
        env->SetObjectArrayElement(array, 1, orgOutput);
    }

    env->CallStaticVoidMethod(g_jclass, gOffset.g_methodid_onOpenDexFileNative, array);

    jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
    jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

    const char *_newSource = newSource == NULL ? NULL : env->GetStringUTFChars(newSource, NULL);
    const char *_newOutput = newOutput == NULL ? NULL : env->GetStringUTFChars(newOutput, NULL);

    args[0] = _newSource == NULL ? NULL : gOffset.GetStringFromCstr(_newSource);
    args[1] = _newOutput == NULL ? NULL : gOffset.GetStringFromCstr(_newOutput);

    if (source && orgSource) {
        env->ReleaseStringUTFChars(orgSource, source);
    }
    if (output && orgOutput) {
        env->ReleaseStringUTFChars(orgOutput, output);
    }

    gOffset.orig_openDexFile_dvm(args, pResult, method, self);
}



void searchJniOffset(JNIEnv *env, bool isArt) {

    jmethodID mtd_nativeHook = env->GetStaticMethodID(g_jclass, gMarkMethods[0].name, gMarkMethods[0].signature);

    size_t startAddress = (size_t) mtd_nativeHook;
    size_t targetAddress = (size_t) mark;
    if (isArt && gOffset.art_work_around_app_jni_bugs) {
        targetAddress = (size_t) gOffset.art_work_around_app_jni_bugs;
    }

    int offset = 0;
    bool found = false;
    while (true) {
        if (*((size_t*) (startAddress + offset)) == targetAddress) {
            found = true;
            break;
        }
        offset += 4;
        if (offset >= 100) {
            LOGE("Error: Unable to find the jni function.");
            break;
        }
    }
    if (found) {
        gOffset.nativeOffset = offset;
        if (!isArt) {
            gOffset.nativeOffset += (sizeof(int) + sizeof(void*));
        }
    }
}


inline void replaceGetCallingUid(JNIEnv *env, jboolean isArt) {
    if (isArt) {
        size_t mtd_getCallingUid = (size_t) env->GetStaticMethodID(gOffset.g_binder_classs, "getCallingUid", "()I");
        int nativeFuncOffset = gOffset.nativeOffset;
        void** jniFuncPtr = (void**)(mtd_getCallingUid + nativeFuncOffset);
        gOffset.orig_getCallingUid = (Native_getCallingUid)(*jniFuncPtr);
        *jniFuncPtr = (void*) getCallingUid;
    } else {
        env->RegisterNatives(gOffset.g_binder_classs, gUidMethods, NELEM(gUidMethods));
    }
}

inline void replaceCameraNativeSetup(JNIEnv *env, jboolean isArt, int apiLevel) {
    if (apiLevel < 19)
        return;
    LOGD("PLIB replaceCameraNativeSetup");
    if (gOffset.nativeOffset == 0) {
        LOGE("native offset null");
        return;
    }
    jclass camera_class = env->FindClass("android/hardware/Camera");
    if (camera_class != NULL) {
        size_t mtd_nativeSetup ;
        if (apiLevel <= 19) {
            mtd_nativeSetup = (size_t) env->GetMethodID(camera_class, "native_setup",
                                                        "(Ljava/lang/Object;ILjava/lang/String;)V");
        } else {
            mtd_nativeSetup = (size_t) env->GetMethodID(camera_class, "native_setup",
                                                        "(Ljava/lang/Object;IILjava/lang/String;)I");
        }
        if (mtd_nativeSetup == 0){
            LOGE("mtd_nativeSetup null");
            return;
        }
        void **jniFuncPtr = (void **) (gOffset.nativeOffset + mtd_nativeSetup);
        LOGD("PLIB replaceCameraNativeSetup jniFuncPtr 0x%x " ,jniFuncPtr);

        gOffset.orig_cameraNativeSetup = *jniFuncPtr;
        LOGD("PLIB replaceCameraNativeSetup orig %x ", gOffset.orig_cameraNativeSetup);
        if (apiLevel <= 19) {
            *jniFuncPtr = (void *) cameraNativeSetup;
        } else {
            *jniFuncPtr = (void *) cameraNativeSetup21;
        }
    }
}

inline void replaceOpenDexFileMethod(JNIEnv *env, jobject javaMethod, jboolean isArt, int apiLevel) {

    size_t mtd_openDexNative = (size_t) env->FromReflectedMethod(javaMethod);
    int nativeFuncOffset = gOffset.nativeOffset;
    void** jniFuncPtr = (void**)(mtd_openDexNative + nativeFuncOffset);

    if (!isArt) {
        gOffset.orig_openDexFile_dvm = (Bridge_DalvikBridgeFunc)(*jniFuncPtr);
        *jniFuncPtr = (void*) new_bridge_openDexNativeFunc;
    } else {
        if (apiLevel < 24) {
            gOffset.orig_native_openDexNativeFunc.beforeN = (Native_openDexNativeFunc)(*jniFuncPtr);
            *jniFuncPtr = (void*) new_native_openDexNativeFunc;
        } else {
            gOffset.orig_native_openDexNativeFunc.afterN = (Native_openDexNativeFunc_N)(*jniFuncPtr);
            *jniFuncPtr = (void*) new_native_openDexNativeFunc_N;
        }
    }

}


void hookNative(jobject javaMethod, jboolean isArt, jint apiLevel) {

    JNIEnv *env = NULL;
    g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    g_vm->AttachCurrentThread(&env, NULL);

    if (env->RegisterNatives(g_jclass, gMarkMethods, NELEM(gMarkMethods)) < 0) {
        return;
    }
    gOffset.isArt = isArt;

    char vmSoName[15] = {0};
    __system_property_get("persist.sys.dalvik.vm.lib", vmSoName);
    LOGD("Find the so name : %s.", strlen(vmSoName) == 0 ? "<EMPTY>" : vmSoName);

    void* vmHandle = dlopen(vmSoName, 0);
    if (!vmHandle) {
        vmHandle = RTLD_DEFAULT;
    }
    gOffset.g_binder_classs = env->FindClass("android/os/Binder");
    gOffset.g_methodid_onGetCallingUid = env->GetStaticMethodID(g_jclass, "onGetCallingUid", "(I)I");
    gOffset.g_methodid_onOpenDexFileNative = env->GetStaticMethodID(g_jclass, "onOpenDexFileNative", "([Ljava/lang/String;)V");

    if (isArt) {
        gOffset.art_work_around_app_jni_bugs = dlsym(vmHandle, "art_work_around_app_jni_bugs");
    } else {
        gOffset.g_sym_IPCThreadState_self = dlsym(RTLD_DEFAULT, "_ZN7android14IPCThreadState4selfEv");
        gOffset.g_sym_IPCThreadState_getCallingUid = dlsym(RTLD_DEFAULT, "_ZNK7android14IPCThreadState13getCallingUidEv");
        if (gOffset.g_sym_IPCThreadState_getCallingUid == NULL) {
            gOffset.g_sym_IPCThreadState_getCallingUid = dlsym(RTLD_DEFAULT, "_ZN7android14IPCThreadState13getCallingUidEv");
        }

        gOffset.GetCstrFromString = (char *(*)(void *)) dlsym(vmHandle, "_Z23dvmCreateCstrFromStringPK12StringObject");
        if (!gOffset.GetCstrFromString) {
            gOffset.GetCstrFromString = (char *(*)(void *)) dlsym(vmHandle, "dvmCreateCstrFromString");
        }
        gOffset.GetStringFromCstr = (void *(*)(const char *)) dlsym(vmHandle, "_Z23dvmCreateStringFromCstrPKc");
        if (!gOffset.GetStringFromCstr) {
            gOffset.GetStringFromCstr = (void *(*)(const char *)) dlsym(vmHandle, "dvmCreateStringFromCstr");
        }
    }
    searchJniOffset(env, isArt);
    replaceGetCallingUid(env, isArt);
    replaceOpenDexFileMethod(env, javaMethod, isArt, apiLevel);
    replaceCameraNativeSetup(env, isArt, apiLevel);
}


