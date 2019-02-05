//
// Created by wei on 16-12-4.
//

#include <string.h>
#include <android/log.h>
#include <jni.h>
#include "checksignature.h"


jint check_signature(JNIEnv *env, jobject thiz, jobject context) {
    //Context的类
    jclass context_clazz = (*env)->GetObjectClass(env, context);
    // 得到 getPackageManager 方法的 ID
    jmethodID methodID_getPackageManager = (*env)->GetMethodID(env,
                                                               context_clazz, "getPackageManager",
                                                               "()Landroid/content/pm/PackageManager;");

    // 获得PackageManager对象
    jobject packageManager = (*env)->CallObjectMethod(env, context,
                                                      methodID_getPackageManager);
//	// 获得 PackageManager 类
    jclass pm_clazz = (*env)->GetObjectClass(env, packageManager);
    // 得到 getPackageInfo 方法的 ID
    jmethodID methodID_pm = (*env)->GetMethodID(env, pm_clazz, "getPackageInfo",
                                                "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
//
//	// 得到 getPackageName 方法的 ID
    jmethodID methodID_pack = (*env)->GetMethodID(env, context_clazz,
                                                  "getPackageName", "()Ljava/lang/String;");

    // 获得当前应用的包名
    jstring application_package = (*env)->CallObjectMethod(env, context,
                                                           methodID_pack);
    const char *package_name = (*env)->GetStringUTFChars(env,
                                                         application_package, 0);
    //LOGE("packageName: %s\n", package_name);

    // 获得PackageInfo
    jobject packageInfo = (*env)->CallObjectMethod(env, packageManager,
                                                   methodID_pm, application_package, 64);

    jclass packageinfo_clazz = (*env)->GetObjectClass(env, packageInfo);
    jfieldID fieldID_signatures = (*env)->GetFieldID(env, packageinfo_clazz,
                                                     "signatures", "[Landroid/content/pm/Signature;");
    jobjectArray signature_arr = (jobjectArray) (*env)->GetObjectField(env,
                                                                       packageInfo, fieldID_signatures);
    //Signature数组中取出第一个元素
    jobject signature = (*env)->GetObjectArrayElement(env, signature_arr, 0);
    //读signature的hashcode
    jclass signature_clazz = (*env)->GetObjectClass(env, signature);
    jmethodID methodID_hashcode = (*env)->GetMethodID(env, signature_clazz,
                                                      "hashCode", "()I");
    jint hashCode = (*env)->CallIntMethod(env, signature, methodID_hashcode);
    //LOGE("hashcode: %d\n", hashCode);
    //__android_log_write(ANDROID_LOG_ERROR, "code", hashCode);//Or ANDROID_LOG_INFO, ...

    int code_size = sizeof(signature_hash_codes)/sizeof(int);
    LOGE("code_size: %d\n", code_size);


//    if (strcmp(package_name, app_packageName) != 0) {
//        return -1;
//    }
    for (int i = 0; i < code_size; i++) {
        if (hashCode == signature_hash_codes[i]) {
            LOGE("item: %d\n", signature_hash_codes[i]);
            return 1;
        }
    }
    return -2;
//    if (hashCode != app_signature_hash_code) {
//        return -2;
//    }
//    return 1;
}

//chs length must be array_len*2+1; return jstring, and chars are inside parameter chs
// for now, always return null in order to avoid potential memory leak
jstring byteToHex(JNIEnv *env, jbyteArray array, char chs[]) {
    // 1. 数组长度；2. new StringBuilder(); or char[len * 2] 3. char[] -> jstring
    jstring ret = NULL;
    LOGE("byteToHex: 1\n");
    if (array != NULL) {
        //得到数组的长度
        jsize len = (*env)->GetArrayLength(env, array);
        if (len > 0) {
            //存储编码后的字符, +1的原因是考虑到\0
            LOGE("byteToHex: %d\n", len);
            jboolean b = JNI_FALSE;
            //得到数据的原始数据 此处注意要取b的地址!
            jbyte *data = (*env)->GetByteArrayElements(env, array, &b);
            int index;
            for (index = 0; index < len; index++) {
                jbyte bc = data[index];
                //拆分成高位, 低位
                jbyte h = (jbyte) ((bc >> 4) & 0x0f);
                jbyte l = (jbyte) (bc & 0x0f);
                //把高位和地位转换成字符
                jchar ch;
                jchar cl;

                if (h > 9) {
                    ch = (jchar) ('A' + (h - 10));
                } else {
                    ch = (jchar) ('0' + h);
                }

                if (l > 9) {
                    cl = (jchar) ('A' + (l - 10));
                } else {
                    cl = (jchar) ('0' + l);
                }
                //转换之后拼接
                chs[index * 2] = (char) ch;
                chs[index * 2 + 1] = (char) cl;
            }
            //最后一位置为0
            chs[len * 2] = 0;
            //释放数组
            (*env)->ReleaseByteArrayElements(env, array, data, JNI_ABORT);
            //ret = (*env)->NewStringUTF(env, chs);
        }
    }
    return ret;
}

jint check_signature_sha1(JNIEnv *env, jobject type, jobject context_object) {
    //上下文对象
    jclass context_class = (*env)->GetObjectClass(env, context_object);

    //反射获取PackageManager
    jmethodID methodId = (*env)->GetMethodID(env, context_class, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject package_manager = (*env)->CallObjectMethod(env, context_object, methodId);
    if (package_manager == NULL) {
        LOGE("package_manager is NULL!!!");
        return NULL;
    }

    //反射获取包名
    methodId = (*env)->GetMethodID(env, context_class, "getPackageName", "()Ljava/lang/String;");
    jstring package_name = (jstring)(*env)->CallObjectMethod(env, context_object, methodId);
    if (package_name == NULL) {
        LOGE("package_name is NULL!!!");
        return NULL;
    }
    (*env)->DeleteLocalRef(env, context_class);

    //获取PackageInfo对象
    jclass pack_manager_class = (*env)->GetObjectClass(env, package_manager);
    methodId = (*env)->GetMethodID(env, pack_manager_class, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    (*env)->DeleteLocalRef(env, pack_manager_class);
    jobject package_info = (*env)->CallObjectMethod(env, package_manager, methodId, package_name, 0x40);
    if (package_info == NULL) {
        LOGE("getPackageInfo() is NULL!!!");
        return NULL;
    }
    (*env)->DeleteLocalRef(env, package_manager);

    //获取签名信息
    jclass package_info_class = (*env)->GetObjectClass(env, package_info);
    jfieldID fieldId = (*env)->GetFieldID(env, package_info_class, "signatures", "[Landroid/content/pm/Signature;");
    (*env)->DeleteLocalRef(env, package_info_class);
    jobjectArray signature_object_array = (jobjectArray)(*env)->GetObjectField(env, package_info, fieldId);
    if (signature_object_array == NULL) {
        LOGE("signature is NULL!!!");
        return NULL;
    }
    jobject signature_object = (*env)->GetObjectArrayElement(env, signature_object_array, 0);
    (*env)->DeleteLocalRef(env, package_info);

    //签名信息转换成sha1值
    jclass signature_class = (*env)->GetObjectClass(env, signature_object);
    methodId = (*env)->GetMethodID(env, signature_class, "toByteArray", "()[B");
    (*env)->DeleteLocalRef(env, signature_class);
    jbyteArray signature_byte = (jbyteArray) (*env)->CallObjectMethod(env, signature_object, methodId);
    jclass byte_array_input_class=(*env)->FindClass(env, "java/io/ByteArrayInputStream");
    methodId=(*env)->GetMethodID(env, byte_array_input_class,"<init>","([B)V");
    jobject byte_array_input=(*env)->NewObject(env, byte_array_input_class,methodId,signature_byte);
    jclass certificate_factory_class=(*env)->FindClass(env, "java/security/cert/CertificateFactory");
    methodId=(*env)->GetStaticMethodID(env, certificate_factory_class,"getInstance","(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;");
    jstring x_509_jstring=(*env)->NewStringUTF(env, "X.509");
    jobject cert_factory=(*env)->CallStaticObjectMethod(env, certificate_factory_class,methodId,x_509_jstring);
    methodId=(*env)->GetMethodID(env, certificate_factory_class,"generateCertificate",("(Ljava/io/InputStream;)Ljava/security/cert/Certificate;"));
    jobject x509_cert=(*env)->CallObjectMethod(env, cert_factory,methodId,byte_array_input);
    (*env)->DeleteLocalRef(env, certificate_factory_class);
    jclass x509_cert_class=(*env)->GetObjectClass(env, x509_cert);
    methodId=(*env)->GetMethodID(env, x509_cert_class,"getEncoded","()[B");
    jbyteArray cert_byte=(jbyteArray)(*env)->CallObjectMethod(env, x509_cert,methodId);
    (*env)->DeleteLocalRef(env, x509_cert_class);
    jclass message_digest_class=(*env)->FindClass(env, "java/security/MessageDigest");
    methodId=(*env)->GetStaticMethodID(env, message_digest_class,"getInstance","(Ljava/lang/String;)Ljava/security/MessageDigest;");
    jstring sha1_jstring=(*env)->NewStringUTF(env, "SHA1");
    jobject sha1_digest=(*env)->CallStaticObjectMethod(env, message_digest_class,methodId,sha1_jstring);
    methodId=(*env)->GetMethodID(env, message_digest_class,"digest","([B)[B");
    jbyteArray sha1_bytes=(jbyteArray)(*env)->CallObjectMethod(env, sha1_digest,methodId,cert_byte);
    (*env)->DeleteLocalRef(env, message_digest_class);


    jsize len = (*env)->GetArrayLength(env, sha1_bytes);
    char chs[len * 2 + 1];
    byteToHex(env, sha1_bytes, chs);

    int code_size = sizeof(signature_sha1_codes)/sizeof(char*);
    LOGE("code_size: %d\n", code_size);
    for (int i = 0; i < code_size; i++) {
        if (strcmp(chs, signature_sha1_codes[i]) == 0) {
            return 1;
        }
    }
    return -2;
}



/*
JNIEXPORT jbyteArray JNICALL
Java_com_lulu_encodedemo_Codec_hexDecode(JNIEnv *env, jclass type, jstring str) {
    jbyteArray ret = NULL;
    if (str != NULL) {
        // TODO
        jsize len = (*env)->GetStringLength(env, str);
        //判断只有在长度为偶数的情况下才继续
        if (len % 2 == 0) {
            jsize dLen = len >> 1;
            jbyte data[dLen];
            jboolean b = JNI_FALSE;
            const jchar *chs = (*env)->GetStringChars(env, str, &b);
            int index;
            for (index = 0; index < dLen; index++) {
                //获取到单个字符
                jchar ch = chs[index * 2];
                jchar cl = chs[index * 2 + 1];
                jint h = 0;
                jint l = 0;
                //得到高位和低位的 ascii
                if (ch >= 'A') {
                    h = ch - 'A' + 10;
                } else if (ch >= 'a') {
                    h = ch - 'a' + 10;
                } else if(ch >= '0') {
                    h = ch - '0';
                }
                if (cl >= 'A') {
                    l = cl - 'A' + 10;
                } else if (cl >= 'a') {
                    l = cl - 'a' + 10;
                } else if(cl >= '0'){
                    l = cl - '0';
                }
                //高位和地位拼接
                data[index] = (jbyte) ((h << 4) | l);
            }
            //释放
            (*env)->ReleaseStringChars(env, str, chs);
            //创建新的字节数组
            ret = (*env)->NewByteArray(env, dLen);
            //给新创建的数组设置数值
            (*env)->SetByteArrayRegion(env, ret, 0,dLen, data);
        }
    }
    return ret;
}*/