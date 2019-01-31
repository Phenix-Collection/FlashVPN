//
// Created by huan zheng on 2019/1/30.
//

#include <jni.h>
#include <string>

extern "C"
{
JNIEXPORT jstring JNICALL
Java_com_polestar_superclone_component_activity_LauncherActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    //jstring hello = (jstring) "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


JNIEXPORT jbyteArray JNICALL
Java_com_polestar_superclone_component_activity_LauncherActivity_nativeGetSig(
        JNIEnv *env, jobject type, jobject context) {
    // context.getPackageManager()
    jclass context_clazz = env->GetObjectClass(context);
    jmethodID getPackageManager = env->GetMethodID(context_clazz,
                                                   "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject packageManager = env->CallObjectMethod(context,
                                                   getPackageManager);

    // context.getPackageName()
    jmethodID getPackageName = env->GetMethodID(context_clazz,
                                                "getPackageName", "()Ljava/lang/String;");
    jstring packageName = (jstring) env->CallObjectMethod(context,
                                                          getPackageName);

    // packageManager->getPackageInfo(packageName, GET_SIGNATURES);
    jclass package_manager_clazz = env->GetObjectClass(packageManager);
    jmethodID getPackageInfo = env->GetMethodID(package_manager_clazz,
                                                "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jint flags = 0x00000040;
    jobject packageInfo = env->CallObjectMethod(packageManager,
                                                getPackageInfo, packageName, flags);

    jthrowable exception = env->ExceptionOccurred();
    env->ExceptionClear();
    if (exception) {
        return NULL;
    }

    // packageInfo.signatures[0]
    jclass package_info_clazz = env->GetObjectClass(packageInfo);
    jfieldID fid = env->GetFieldID(package_info_clazz, "signatures",
                                   "[Landroid/content/pm/Signature;");
    jobjectArray signatures = (jobjectArray) env->GetObjectField(
            packageInfo, fid);
    jobject signature = env->GetObjectArrayElement(signatures, 0);

    // signature.toByteArray()
    jclass signature_clazz = env->GetObjectClass(signature);
    jmethodID signature_toByteArray = env->GetMethodID(signature_clazz,
                                                       "toByteArray", "()[B");
    jbyteArray sig_bytes = (jbyteArray) env->CallObjectMethod(
            signature, signature_toByteArray);

    // X509Certificate appCertificate = X509Certificate.getInstance(sig_bytes);
    jclass x509_clazz = env->FindClass("javax/security/cert/X509Certificate");
    jmethodID x509_getInstance = env->GetStaticMethodID(x509_clazz,
                                                        "getInstance", "([B)Ljavax/security/cert/X509Certificate;");
    jobject x509 = (jstring) env->CallStaticObjectMethod(x509_clazz,
                                                         x509_getInstance, sig_bytes);

    exception = env->ExceptionOccurred();
    env->ExceptionClear();
    if (exception) {
        return NULL;
    }

    // x509.getEncoded()
    jmethodID getEncoded = env->GetMethodID(x509_clazz,
                                            "getEncoded", "()[B");
    jbyteArray public_key = (jbyteArray) env->CallObjectMethod(x509, getEncoded);

    exception = env->ExceptionOccurred();
    env->ExceptionClear();
    if (exception) {
        return NULL;
    }

    // MessageDigest.getInstance("SHA1")
    jclass message_digest_clazz = env->FindClass("java/security/MessageDigest");
    jmethodID message_digest_getInstance = env->GetStaticMethodID(
            message_digest_clazz, "getInstance",
            "(Ljava/lang/String;)Ljava/security/MessageDigest;");
    jstring sha1_name = env->NewStringUTF("SHA1");
    jobject sha1 = env->CallStaticObjectMethod(message_digest_clazz,
                                               message_digest_getInstance, sha1_name);

    exception = env->ExceptionOccurred();
    env->ExceptionClear();
    if (exception) {
        return NULL;
    }

    // sha1.digest(public_key)
    jmethodID digest = env->GetMethodID(message_digest_clazz,
                                        "digest", "([B)[B");
    jbyteArray sha1_bytes = (jbyteArray) env->CallObjectMethod(
            sha1, digest, public_key);

    return sha1_bytes;
}
};
