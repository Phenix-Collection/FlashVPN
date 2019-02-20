#include <jni.h>
#include "aes.h"
#include "checksignature.h"
#include "check_emulator.h"
#include <string.h>
#include <sys/ptrace.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#define CBC 1
#define ECB 1


// 获取数组的大小
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
// 指定要注册的类，对应完整的java类名
#define JNIREG_CLASS "com/twitter/msg/Sender"

const char *UNSIGNATURE = "fk";
int secure = 0;

jstring charToJstring(JNIEnv *envPtr, char *src) {
    JNIEnv env = *envPtr;

    jsize len = strlen(src);
    jclass clsstring = env->FindClass(envPtr, "java/lang/String");
    jstring strencode = env->NewStringUTF(envPtr, "UTF-8");
    jmethodID mid = env->GetMethodID(envPtr, clsstring, "<init>",
                                     "([BLjava/lang/String;)V");
    jbyteArray barr = env->NewByteArray(envPtr, len);
    env->SetByteArrayRegion(envPtr, barr, 0, len, (jbyte *) src);

    jstring ret = (jstring) env->NewObject(envPtr, clsstring, mid, barr, strencode);
    free(src);
    return ret;
}

//__attribute__((section (".mytext")))//隐藏字符表 并没有什么卵用 只是针对初阶hacker的一个小方案而已
char *getKey() {
    int n = 0;
    char s[23];//"NMTIzNDU2Nzg5MGFiY2RlZg"; //NjEzNzU2MjgxMGRlZmFiYw

    s[n++] = 'J';
    s[n++] = 'N';
    s[n++] = 'j';
    s[n++] = 'E';
    s[n++] = 'z';
    s[n++] = 'N';
    s[n++] = 'z';
    s[n++] = 'U';
    s[n++] = '2';
    s[n++] = 'M';
    s[n++] = 'j';
    s[n++] = 'g';
    s[n++] = 'x';
    s[n++] = 'M';
    s[n++] = 'G';
    s[n++] = 'R';
    s[n++] = 'l';
    s[n++] = 'Z';
    s[n++] = 'm';
    s[n++] = 'F';
    s[n++] = 'i';
    s[n++] = 'Y';
    s[n++] = 'w';
    char *encode_str = s + 1;
    return b64_decode(encode_str, strlen(encode_str));

    //初版hidekey的方案
}

//__attribute__((section (".mytext")))
JNIEXPORT jstring JNICALL encode(JNIEnv *env, jobject instance, jstring str_) {

    //先进行apk被 二次打包的校验
    //if (check_signature(env, instance, context) != 1 || check_is_emulator(env) != 1) {
    if (secure != 1) {
        char *str = UNSIGNATURE;
//        return (*env)->NewString(env, str, strlen(str));
        return charToJstring(env,str);
    }

    uint8_t *AES_KEY = (uint8_t *) getKey();
    const char *in = (*env)->GetStringUTFChars(env, str_, JNI_FALSE);
    char *baseResult = AES_128_ECB_PKCS5Padding_Encrypt(in, AES_KEY);
    (*env)->ReleaseStringUTFChars(env, str_, in);
    jstring ret = (*env)->NewStringUTF(env, baseResult);
    free(AES_KEY);
    free(baseResult);
    return ret;
}

//__attribute__((section (".mytext")))
JNIEXPORT jstring JNICALL decode(JNIEnv *env, jobject instance, jstring str_) {


    //先进行apk被 二次打包的校验
    //if (check_signature(env, instance, context) != 1|| check_is_emulator(env) != 1) {
    if (secure != 1) {
        char *str = UNSIGNATURE;
//        return (*env)->NewString(env, str, strlen(str));
        return charToJstring(env,str);
    }

    uint8_t *AES_KEY = (uint8_t *) getKey();
    const char *str = (*env)->GetStringUTFChars(env, str_, JNI_FALSE);
    char *desResult = AES_128_ECB_PKCS5Padding_Decrypt(str, AES_KEY);
    (*env)->ReleaseStringUTFChars(env, str_, str);
//    return (*env)->NewStringUTF(env, desResult);
    //不用系统自带的方法NewStringUTF是因为如果desResult是乱码,会抛出异常
    free(AES_KEY);
    return charToJstring(env,desResult);
}



/**
 * if rerurn 1 ,is check pass.
 */
JNIEXPORT jint JNICALL
check_jni(JNIEnv *env, jobject instance, jobject context) {
    if (check_signature_sha1(env, instance, context) != 1 || check_is_emulator(env) != 1) {
        secure = 0;
    } else {
        secure = 1;
    }
    return secure;
}

// Java和JNI函数的绑定表
static JNINativeMethod method_table[] = {
        {"check",   "(Ljava/lang/Object;)I", (void *) check_jni},
        {"receive", "(Ljava/lang/String;)Ljava/lang/String;", (void *) decode},
        {"send",    "(Ljava/lang/String;)Ljava/lang/String;", (void *) encode},
};

// 注册native方法到java中
static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;
    clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

int register_ndk_load(JNIEnv *env) {
    // 调用注册方法
    return registerNativeMethods(env, JNIREG_CLASS,
                                 method_table, NELEM(method_table));
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    ptrace(PTRACE_TRACEME, 0, 0, 0);//反调试
    //这是一种比较简单的防止被调试的方案
    // 有更复杂更高明的方案，比如：不用这个ptrace而是每次执行加密解密签先去判断是否被trace,目前的版本不做更多的负载方案，您想做可以fork之后，自己去做


    JNIEnv *env = NULL;
    jint result = -1;

    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    register_ndk_load(env);

    // 返回jni的版本
    return JNI_VERSION_1_4;
}

