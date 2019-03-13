//
// Created by wei on 16-12-4.
//

#include <string.h>
#include <android/log.h>
#include <jni.h>
#include "checksignature.h"

/*
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
    //LOGE("code_size: %d\n", code_size);


//    if (strcmp(package_name, app_packageName) != 0) {
//        return -1;
//    }
    for (int i = 0; i < code_size; i++) {
        if (hashCode == signature_hash_codes[i]) {
            //LOGE("item: %d\n", signature_hash_codes[i]);
            return 1;
        }
    }
    return -2;
//    if (hashCode != app_signature_hash_code) {
//        return -2;
//    }
//    return 1;
}*/

//chs length must be array_len*2+1; return jstring, and chars are inside parameter chs
// for now, always return null in order to avoid potential memory leak
jstring byteToHex(JNIEnv *env, jbyteArray array, char chs[]) {
    // 1. 数组长度；2. new StringBuilder(); or char[len * 2] 3. char[] -> jstring
    jstring ret = NULL;
    //LOGE("byteToHex: 1\n");
    if (array != NULL) {
        //得到数组的长度
        jsize len = (*env)->GetArrayLength(env, array);
        if (len > 0) {
            //存储编码后的字符, +1的原因是考虑到\0
            //LOGE("byteToHex: %d\n", len);
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

static const char* signature_sha1_codes[20];
static int signature_filled = 0;

jint check_signature_sha1(JNIEnv *env, jobject type, jobject context_object) {
    filleSignatures();
    //上下文对象
    jclass context_class = (*env)->GetObjectClass(env, context_object);

    //反射获取PackageManager
    jmethodID methodId = (*env)->GetMethodID(env, context_class, getPackageManagerFunc(), getPackageManagerSigFunc());
    jobject package_manager = (*env)->CallObjectMethod(env, context_object, methodId);
    if (package_manager == NULL) {
        //LOGE("package_manager is NULL!!!");
        return NULL;
    }

    //反射获取包名
    methodId = (*env)->GetMethodID(env, context_class, getPackageNameFunc(), getPackageNameSigFunc());
    jstring package_name = (jstring)(*env)->CallObjectMethod(env, context_object, methodId);
    if (package_name == NULL) {
        //LOGE("package_name is NULL!!!");
        return NULL;
    }
    (*env)->DeleteLocalRef(env, context_class);

    //获取PackageInfo对象
    jclass pack_manager_class = (*env)->GetObjectClass(env, package_manager);
    methodId = (*env)->GetMethodID(env, pack_manager_class, getPackageInfoFunc(), getPackageInfoSigFunc());
    (*env)->DeleteLocalRef(env, pack_manager_class);
    jobject package_info = (*env)->CallObjectMethod(env, package_manager, methodId, package_name, 0x40);
    if (package_info == NULL) {
        //LOGE("getPackageInfo() is NULL!!!");
        return NULL;
    }
    (*env)->DeleteLocalRef(env, package_manager);

    //获取签名信息
    jclass package_info_class = (*env)->GetObjectClass(env, package_info);
    jfieldID fieldId = (*env)->GetFieldID(env, package_info_class, signaturesFunc(), signaturesSigFunc());
    (*env)->DeleteLocalRef(env, package_info_class);
    jobjectArray signature_object_array = (jobjectArray)(*env)->GetObjectField(env, package_info, fieldId);
    if (signature_object_array == NULL) {
        //LOGE("signature is NULL!!!");
        return NULL;
    }
    jobject signature_object = (*env)->GetObjectArrayElement(env, signature_object_array, 0);
    (*env)->DeleteLocalRef(env, package_info);

    //签名信息转换成sha1值
    jclass signature_class = (*env)->GetObjectClass(env, signature_object);
    methodId = (*env)->GetMethodID(env, signature_class, toByteArrayFunc(), toByteArraySigFunc());
    (*env)->DeleteLocalRef(env, signature_class);
    jbyteArray signature_byte = (jbyteArray) (*env)->CallObjectMethod(env, signature_object, methodId);
    jclass byte_array_input_class=(*env)->FindClass(env, ByteArrayInputStreamFunc());
    methodId=(*env)->GetMethodID(env, byte_array_input_class,initFunc(),initSigFunc());
    jobject byte_array_input=(*env)->NewObject(env, byte_array_input_class,methodId,signature_byte);
    jclass certificate_factory_class=(*env)->FindClass(env, CertificateFactoryFunc());
    methodId=(*env)->GetStaticMethodID(env, certificate_factory_class,getInstanceFunc(), getInstanceSigFunc());
    jstring x_509_jstring=(*env)->NewStringUTF(env, X509Func());
    jobject cert_factory=(*env)->CallStaticObjectMethod(env, certificate_factory_class,methodId,x_509_jstring);
    methodId=(*env)->GetMethodID(env, certificate_factory_class, generateCertificateFunc(),(generateCertificateSigFunc()));
    jobject x509_cert=(*env)->CallObjectMethod(env, cert_factory,methodId,byte_array_input);
    (*env)->DeleteLocalRef(env, certificate_factory_class);
    jclass x509_cert_class=(*env)->GetObjectClass(env, x509_cert);
    methodId=(*env)->GetMethodID(env, x509_cert_class,getEncodedFunc(),getEncodedSigFunc());
    jbyteArray cert_byte=(jbyteArray)(*env)->CallObjectMethod(env, x509_cert,methodId);
    (*env)->DeleteLocalRef(env, x509_cert_class);
    jclass message_digest_class=(*env)->FindClass(env, MessageDigestFunc());
    methodId=(*env)->GetStaticMethodID(env, message_digest_class, getInstanceFunc(),getInstanceMDSigFunc());
    jstring sha1_jstring=(*env)->NewStringUTF(env, SHA1Func());
    jobject sha1_digest=(*env)->CallStaticObjectMethod(env, message_digest_class,methodId,sha1_jstring);
    methodId=(*env)->GetMethodID(env, message_digest_class,digestFunc(),digestSigFunc());
    jbyteArray sha1_bytes=(jbyteArray)(*env)->CallObjectMethod(env, sha1_digest,methodId,cert_byte);
    (*env)->DeleteLocalRef(env, message_digest_class);


    jsize len = (*env)->GetArrayLength(env, sha1_bytes);
    char chs[len * 2 + 1];
    byteToHex(env, sha1_bytes, chs);

    int code_size = sizeof(signature_sha1_codes)/sizeof(char*);
//    LOGE("code_size: %d\n", code_size);
//    LOGE("sig %s\n", chs);
    for (int i = 0; i < code_size; i++) {
        if (signature_sha1_codes[i] != NULL && strcmp(chs, signature_sha1_codes[i]) == 0) {
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


/**
 *
   "9B6091A2C1ACB04710F892BABBECF74141C3AD15", //flash-vpn.jks
   "55B1DF321918B74E75683E9CD92C155476E74D5C", //superb
   "1EB8328D065270ECF1C47AB3789C8821451BFE4B", //what's clone
   "E9B1C446DB550C27B09C5ED28F37F5DF2CDCF4DC", //what's clone 64
   "D13E5C923C246F34801C247EB0279230A193846C", //whatsclone.jks
   "4175FEBFCB45D8794EAC0E9046C63B3FA67A425F", //domultiple.jks
   "CD10F93C52888A9200BF577CBF48C82FB18B1617", //polestar.jks
   "7F964242C609B5857BD1B4665CC62B1D7C3FB009", //polestar-team.jks
   "2CA149BC1AB9F433959B38EB02433EA751059BCE", //domultipe-new.jks
   "5FF4F71BCC7C0B79AC0F5C882EE0628AAC28521C", //super at google console
   "A0F603477820F56526C1163D4D05E46071ECD778", //flashvpn at google console
 *
 *
 */

static char flashgoogle[50];
static void fillFlashGoogle() {
    int n = 0;

    flashgoogle[n++] = 'A';
    flashgoogle[n++] = '0';
    flashgoogle[n++] = 'F';
    flashgoogle[n++] = '6';
    flashgoogle[n++] = '0';
    flashgoogle[n++] = '3';
    flashgoogle[n++] = '4';
    flashgoogle[n++] = '7';
    flashgoogle[n++] = '7';
    flashgoogle[n++] = '8';
    flashgoogle[n++] = '2';
    flashgoogle[n++] = '0';
    flashgoogle[n++] = 'F';
    flashgoogle[n++] = '5';
    flashgoogle[n++] = '6';
    flashgoogle[n++] = '5';
    flashgoogle[n++] = '2';
    flashgoogle[n++] = '6';
    flashgoogle[n++] = 'C';
    flashgoogle[n++] = '1';
    flashgoogle[n++] = '1';
    flashgoogle[n++] = '6';
    flashgoogle[n++] = '3';
    flashgoogle[n++] = 'D';
    flashgoogle[n++] = '4';
    flashgoogle[n++] = 'D';
    flashgoogle[n++] = '0';
    flashgoogle[n++] = '5';
    flashgoogle[n++] = 'E';
    flashgoogle[n++] = '4';
    flashgoogle[n++] = '6';
    flashgoogle[n++] = '0';
    flashgoogle[n++] = '7';
    flashgoogle[n++] = '1';
    flashgoogle[n++] = 'E';
    flashgoogle[n++] = 'C';
    flashgoogle[n++] = 'D';
    flashgoogle[n++] = '7';
    flashgoogle[n++] = '7';
    flashgoogle[n++] = '8';
    flashgoogle[n++] = 0;
}


static char dev[50];
static void fillFlashVpn() {
//    char *tocheck = "9B6091A2C1ACB04710F892BABBECF74141C3AD15";
    int n = 0;
    dev[n++] = '9';
    dev[n++] = 'B';
    dev[n++] = '6';
    dev[n++] = '0';
    dev[n++] = '9';
    dev[n++] = '1';
    dev[n++] = 'A';
    dev[n++] = '2';
    dev[n++] = 'C';
    dev[n++] = '1';
    dev[n++] = 'A';
    dev[n++] = 'C';
    dev[n++] = 'B';
    dev[n++] = '0';
    dev[n++] = '4';
    dev[n++] = '7';
    dev[n++] = '1';
    dev[n++] = '0';
    dev[n++] = 'F';
    dev[n++] = '8';
    dev[n++] = '9';
    dev[n++] = '2';
    dev[n++] = 'B';
    dev[n++] = 'A';
    dev[n++] = 'B';
    dev[n++] = 'B';
    dev[n++] = 'E';
    dev[n++] = 'C';
    dev[n++] = 'F';
    dev[n++] = '7';
    dev[n++] = '4';
    dev[n++] = '1';
    dev[n++] = '4';
    dev[n++] = '1';
    dev[n++] = 'C';
    dev[n++] = '3';
    dev[n++] = 'A';
    dev[n++] = 'D';
    dev[n++] = '1';
    dev[n++] = '5';
    dev[n++] = 0;

//    if (strcmp(dev, tocheck) == 0) {
//        LOGE("dev %s ok\n", dev);
//    } else {
//        LOGE("dev %s BAD\n", dev);
//    }
}

//"55B1DF321918B74E75683E9CD92C155476E74D5C", //superb
static char superb[50];
static void fillSuperB() {
    int n = 0;
//    char *tocheck = "55B1DF321918B74E75683E9CD92C155476E74D5C";

    superb[n++] = '5';
    superb[n++] = '5';
    superb[n++] = 'B';
    superb[n++] = '1';
    superb[n++] = 'D';
    superb[n++] = 'F';
    superb[n++] = '3';
    superb[n++] = '2';
    superb[n++] = '1';
    superb[n++] = '9';
    superb[n++] = '1';
    superb[n++] = '8';
    superb[n++] = 'B';
    superb[n++] = '7';
    superb[n++] = '4';
    superb[n++] = 'E';
    superb[n++] = '7';
    superb[n++] = '5';
    superb[n++] = '6';
    superb[n++] = '8';
    superb[n++] = '3';
    superb[n++] = 'E';
    superb[n++] = '9';
    superb[n++] = 'C';
    superb[n++] = 'D';
    superb[n++] = '9';
    superb[n++] = '2';
    superb[n++] = 'C';
    superb[n++] = '1';
    superb[n++] = '5';
    superb[n++] = '5';
    superb[n++] = '4';
    superb[n++] = '7';
    superb[n++] = '6';
    superb[n++] = 'E';
    superb[n++] = '7';
    superb[n++] = '4';
    superb[n++] = 'D';
    superb[n++] = '5';
    superb[n++] = 'C';
    superb[n++] = 0;

//    if (strcmp(superb, tocheck) == 0) {
//        LOGE("superb %s ok\n", superb);
//    } else {
//        LOGE("superb %s BAD\n", superb);
//    }
}


//"1EB8328D065270ECF1C47AB3789C8821451BFE4B", //what's clone
static char whatsclone[50];
static void fillWhatsClone() {
    int n = 0;
//    char *tocheck = "1EB8328D065270ECF1C47AB3789C8821451BFE4B";

    whatsclone[n++] = '1';
    whatsclone[n++] = 'E';
    whatsclone[n++] = 'B';
    whatsclone[n++] = '8';
    whatsclone[n++] = '3';
    whatsclone[n++] = '2';
    whatsclone[n++] = '8';
    whatsclone[n++] = 'D';
    whatsclone[n++] = '0';
    whatsclone[n++] = '6';
    whatsclone[n++] = '5';
    whatsclone[n++] = '2';
    whatsclone[n++] = '7';
    whatsclone[n++] = '0';
    whatsclone[n++] = 'E';
    whatsclone[n++] = 'C';
    whatsclone[n++] = 'F';
    whatsclone[n++] = '1';
    whatsclone[n++] = 'C';
    whatsclone[n++] = '4';
    whatsclone[n++] = '7';
    whatsclone[n++] = 'A';
    whatsclone[n++] = 'B';
    whatsclone[n++] = '3';
    whatsclone[n++] = '7';
    whatsclone[n++] = '8';
    whatsclone[n++] = '9';
    whatsclone[n++] = 'C';
    whatsclone[n++] = '8';
    whatsclone[n++] = '8';
    whatsclone[n++] = '2';
    whatsclone[n++] = '1';
    whatsclone[n++] = '4';
    whatsclone[n++] = '5';
    whatsclone[n++] = '1';
    whatsclone[n++] = 'B';
    whatsclone[n++] = 'F';
    whatsclone[n++] = 'E';
    whatsclone[n++] = '4';
    whatsclone[n++] = 'B';
    whatsclone[n++] = 0;

//    if (strcmp(whatsclone, tocheck) == 0) {
//        LOGE("whatsclone %s ok\n", whatsclone);
//    } else {
//        LOGE("whatsclone %s BAD\n", whatsclone);
//    }
}

//"E9B1C446DB550C27B09C5ED28F37F5DF2CDCF4DC", //what's clone 64
static char whatsclone64[50];
static void fillWhatsClone64() {
    int n = 0;
//    char *tocheck = "E9B1C446DB550C27B09C5ED28F37F5DF2CDCF4DC";

    whatsclone64[n++] = 'E';
    whatsclone64[n++] = '9';
    whatsclone64[n++] = 'B';
    whatsclone64[n++] = '1';
    whatsclone64[n++] = 'C';
    whatsclone64[n++] = '4';
    whatsclone64[n++] = '4';
    whatsclone64[n++] = '6';
    whatsclone64[n++] = 'D';
    whatsclone64[n++] = 'B';
    whatsclone64[n++] = '5';
    whatsclone64[n++] = '5';
    whatsclone64[n++] = '0';
    whatsclone64[n++] = 'C';
    whatsclone64[n++] = '2';
    whatsclone64[n++] = '7';
    whatsclone64[n++] = 'B';
    whatsclone64[n++] = '0';
    whatsclone64[n++] = '9';
    whatsclone64[n++] = 'C';
    whatsclone64[n++] = '5';
    whatsclone64[n++] = 'E';
    whatsclone64[n++] = 'D';
    whatsclone64[n++] = '2';
    whatsclone64[n++] = '8';
    whatsclone64[n++] = 'F';
    whatsclone64[n++] = '3';
    whatsclone64[n++] = '7';
    whatsclone64[n++] = 'F';
    whatsclone64[n++] = '5';
    whatsclone64[n++] = 'D';
    whatsclone64[n++] = 'F';
    whatsclone64[n++] = '2';
    whatsclone64[n++] = 'C';
    whatsclone64[n++] = 'D';
    whatsclone64[n++] = 'C';
    whatsclone64[n++] = 'F';
    whatsclone64[n++] = '4';
    whatsclone64[n++] = 'D';
    whatsclone64[n++] = 'C';
    whatsclone64[n++] = 0;

//    if (strcmp(whatsclone64, tocheck) == 0) {
//        LOGE("whatsclone64 %s ok\n", whatsclone64);
//    } else {
//        LOGE("whatsclone64 %s BAD\n", whatsclone64);
//    }
}


//"D13E5C923C246F34801C247EB0279230A193846C", //whatsclone.jks
static char whatsclonejks[50];
static void fillWhatsCloneJKs() {
    int n = 0;
//    char *tocheck = "D13E5C923C246F34801C247EB0279230A193846C";

    whatsclonejks[n++] = 'D';
    whatsclonejks[n++] = '1';
    whatsclonejks[n++] = '3';
    whatsclonejks[n++] = 'E';
    whatsclonejks[n++] = '5';
    whatsclonejks[n++] = 'C';
    whatsclonejks[n++] = '9';
    whatsclonejks[n++] = '2';
    whatsclonejks[n++] = '3';
    whatsclonejks[n++] = 'C';
    whatsclonejks[n++] = '2';
    whatsclonejks[n++] = '4';
    whatsclonejks[n++] = '6';
    whatsclonejks[n++] = 'F';
    whatsclonejks[n++] = '3';
    whatsclonejks[n++] = '4';
    whatsclonejks[n++] = '8';
    whatsclonejks[n++] = '0';
    whatsclonejks[n++] = '1';
    whatsclonejks[n++] = 'C';
    whatsclonejks[n++] = '2';
    whatsclonejks[n++] = '4';
    whatsclonejks[n++] = '7';
    whatsclonejks[n++] = 'E';
    whatsclonejks[n++] = 'B';
    whatsclonejks[n++] = '0';
    whatsclonejks[n++] = '2';
    whatsclonejks[n++] = '7';
    whatsclonejks[n++] = '9';
    whatsclonejks[n++] = '2';
    whatsclonejks[n++] = '3';
    whatsclonejks[n++] = '0';
    whatsclonejks[n++] = 'A';
    whatsclonejks[n++] = '1';
    whatsclonejks[n++] = '9';
    whatsclonejks[n++] = '3';
    whatsclonejks[n++] = '8';
    whatsclonejks[n++] = '4';
    whatsclonejks[n++] = '6';
    whatsclonejks[n++] = 'C';
    whatsclonejks[n++] = 0;

//    if (strcmp(whatsclonejks, tocheck) == 0) {
//        LOGE("whatsclonejks %s ok\n", whatsclonejks);
//    } else {
//        LOGE("whatsclonejks %s BAD\n", whatsclonejks);
//    }
}

//"4175FEBFCB45D8794EAC0E9046C63B3FA67A425F", //domultiple.jks
static char domultiple[50];
static void fillDomultiple() {
    int n = 0;
//    char *tocheck = "4175FEBFCB45D8794EAC0E9046C63B3FA67A425F";

    domultiple[n++] = '4';
    domultiple[n++] = '1';
    domultiple[n++] = '7';
    domultiple[n++] = '5';
    domultiple[n++] = 'F';
    domultiple[n++] = 'E';
    domultiple[n++] = 'B';
    domultiple[n++] = 'F';
    domultiple[n++] = 'C';
    domultiple[n++] = 'B';
    domultiple[n++] = '4';
    domultiple[n++] = '5';
    domultiple[n++] = 'D';
    domultiple[n++] = '8';
    domultiple[n++] = '7';
    domultiple[n++] = '9';
    domultiple[n++] = '4';
    domultiple[n++] = 'E';
    domultiple[n++] = 'A';
    domultiple[n++] = 'C';
    domultiple[n++] = '0';
    domultiple[n++] = 'E';
    domultiple[n++] = '9';
    domultiple[n++] = '0';
    domultiple[n++] = '4';
    domultiple[n++] = '6';
    domultiple[n++] = 'C';
    domultiple[n++] = '6';
    domultiple[n++] = '3';
    domultiple[n++] = 'B';
    domultiple[n++] = '3';
    domultiple[n++] = 'F';
    domultiple[n++] = 'A';
    domultiple[n++] = '6';
    domultiple[n++] = '7';
    domultiple[n++] = 'A';
    domultiple[n++] = '4';
    domultiple[n++] = '2';
    domultiple[n++] = '5';
    domultiple[n++] = 'F';
    domultiple[n++] = 0;

//    if (strcmp(domultiple, tocheck) == 0) {
//        LOGE("domultiple %s ok\n", domultiple);
//    } else {
//        LOGE("domultiple %s BAD\n", domultiple);
//    }
}

//"CD10F93C52888A9200BF577CBF48C82FB18B1617", //polestar.jks
static char polestar[50];
static void fillPoleStar() {
    int n = 0;
//    char *tocheck = "CD10F93C52888A9200BF577CBF48C82FB18B1617";

    polestar[n++] = 'C';
    polestar[n++] = 'D';
    polestar[n++] = '1';
    polestar[n++] = '0';
    polestar[n++] = 'F';
    polestar[n++] = '9';
    polestar[n++] = '3';
    polestar[n++] = 'C';
    polestar[n++] = '5';
    polestar[n++] = '2';
    polestar[n++] = '8';
    polestar[n++] = '8';
    polestar[n++] = '8';
    polestar[n++] = 'A';
    polestar[n++] = '9';
    polestar[n++] = '2';
    polestar[n++] = '0';
    polestar[n++] = '0';
    polestar[n++] = 'B';
    polestar[n++] = 'F';
    polestar[n++] = '5';
    polestar[n++] = '7';
    polestar[n++] = '7';
    polestar[n++] = 'C';
    polestar[n++] = 'B';
    polestar[n++] = 'F';
    polestar[n++] = '4';
    polestar[n++] = '8';
    polestar[n++] = 'C';
    polestar[n++] = '8';
    polestar[n++] = '2';
    polestar[n++] = 'F';
    polestar[n++] = 'B';
    polestar[n++] = '1';
    polestar[n++] = '8';
    polestar[n++] = 'B';
    polestar[n++] = '1';
    polestar[n++] = '6';
    polestar[n++] = '1';
    polestar[n++] = '7';
    polestar[n++] = 0;

//    if (strcmp(polestar, tocheck) == 0) {
//        LOGE("polestar %s ok\n", polestar);
//    } else {
//        LOGE("polestar %s BAD\n", polestar);
//    }
}

//"7F964242C609B5857BD1B4665CC62B1D7C3FB009", //polestar-team.jks
static char polestarteam[50];
static void fillPolestarteam() {
    int n = 0;
//    char *tocheck = "7F964242C609B5857BD1B4665CC62B1D7C3FB009";

    polestarteam[n++] = '7';
    polestarteam[n++] = 'F';
    polestarteam[n++] = '9';
    polestarteam[n++] = '6';
    polestarteam[n++] = '4';
    polestarteam[n++] = '2';
    polestarteam[n++] = '4';
    polestarteam[n++] = '2';
    polestarteam[n++] = 'C';
    polestarteam[n++] = '6';
    polestarteam[n++] = '0';
    polestarteam[n++] = '9';
    polestarteam[n++] = 'B';
    polestarteam[n++] = '5';
    polestarteam[n++] = '8';
    polestarteam[n++] = '5';
    polestarteam[n++] = '7';
    polestarteam[n++] = 'B';
    polestarteam[n++] = 'D';
    polestarteam[n++] = '1';
    polestarteam[n++] = 'B';
    polestarteam[n++] = '4';
    polestarteam[n++] = '6';
    polestarteam[n++] = '6';
    polestarteam[n++] = '5';
    polestarteam[n++] = 'C';
    polestarteam[n++] = 'C';
    polestarteam[n++] = '6';
    polestarteam[n++] = '2';
    polestarteam[n++] = 'B';
    polestarteam[n++] = '1';
    polestarteam[n++] = 'D';
    polestarteam[n++] = '7';
    polestarteam[n++] = 'C';
    polestarteam[n++] = '3';
    polestarteam[n++] = 'F';
    polestarteam[n++] = 'B';
    polestarteam[n++] = '0';
    polestarteam[n++] = '0';
    polestarteam[n++] = '9';
    polestarteam[n++] = 0;

//    if (strcmp(polestarteam, tocheck) == 0) {
//        LOGE("polestarteam %s ok\n", polestarteam);
//    } else {
//        LOGE("polestarteam %s BAD\n", polestarteam);
//    }
}

//"2CA149BC1AB9F433959B38EB02433EA751059BCE", //domultipe-new.jks
static char domultiplenew[50];
static void fillDomultipleNew() {
    int n = 0;
//    char *tocheck = "2CA149BC1AB9F433959B38EB02433EA751059BCE";

    domultiplenew[n++] = '2';
    domultiplenew[n++] = 'C';
    domultiplenew[n++] = 'A';
    domultiplenew[n++] = '1';
    domultiplenew[n++] = '4';
    domultiplenew[n++] = '9';
    domultiplenew[n++] = 'B';
    domultiplenew[n++] = 'C';
    domultiplenew[n++] = '1';
    domultiplenew[n++] = 'A';
    domultiplenew[n++] = 'B';
    domultiplenew[n++] = '9';
    domultiplenew[n++] = 'F';
    domultiplenew[n++] = '4';
    domultiplenew[n++] = '3';
    domultiplenew[n++] = '3';
    domultiplenew[n++] = '9';
    domultiplenew[n++] = '5';
    domultiplenew[n++] = '9';
    domultiplenew[n++] = 'B';
    domultiplenew[n++] = '3';
    domultiplenew[n++] = '8';
    domultiplenew[n++] = 'E';
    domultiplenew[n++] = 'B';
    domultiplenew[n++] = '0';
    domultiplenew[n++] = '2';
    domultiplenew[n++] = '4';
    domultiplenew[n++] = '3';
    domultiplenew[n++] = '3';
    domultiplenew[n++] = 'E';
    domultiplenew[n++] = 'A';
    domultiplenew[n++] = '7';
    domultiplenew[n++] = '5';
    domultiplenew[n++] = '1';
    domultiplenew[n++] = '0';
    domultiplenew[n++] = '5';
    domultiplenew[n++] = '9';
    domultiplenew[n++] = 'B';
    domultiplenew[n++] = 'C';
    domultiplenew[n++] = 'E';
    domultiplenew[n++] = 0;

//    if (strcmp(domultiplenew, tocheck) == 0) {
//        LOGE("domultiplenew %s ok\n", domultiplenew);
//    } else {
//        LOGE("domultiplenew %s BAD\n", domultiplenew);
//    }
}

//"5FF4F71BCC7C0B79AC0F5C882EE0628AAC28521C", //super at google console
static char super[50];
static void fillSuper() {
    int n = 0;
//    char *tocheck = "5FF4F71BCC7C0B79AC0F5C882EE0628AAC28521C";

    super[n++] = '5';
    super[n++] = 'F';
    super[n++] = 'F';
    super[n++] = '4';
    super[n++] = 'F';
    super[n++] = '7';
    super[n++] = '1';
    super[n++] = 'B';
    super[n++] = 'C';
    super[n++] = 'C';
    super[n++] = '7';
    super[n++] = 'C';
    super[n++] = '0';
    super[n++] = 'B';
    super[n++] = '7';
    super[n++] = '9';
    super[n++] = 'A';
    super[n++] = 'C';
    super[n++] = '0';
    super[n++] = 'F';
    super[n++] = '5';
    super[n++] = 'C';
    super[n++] = '8';
    super[n++] = '8';
    super[n++] = '2';
    super[n++] = 'E';
    super[n++] = 'E';
    super[n++] = '0';
    super[n++] = '6';
    super[n++] = '2';
    super[n++] = '8';
    super[n++] = 'A';
    super[n++] = 'A';
    super[n++] = 'C';
    super[n++] = '2';
    super[n++] = '8';
    super[n++] = '5';
    super[n++] = '2';
    super[n++] = '1';
    super[n++] = 'C';
    super[n++] = 0;

//    if (strcmp(super, tocheck) == 0) {
//        LOGE("super %s ok\n", super);
//    } else {
//        LOGE("super %s BAD\n", super);
//    }
}

static void filleSignatures() {
    //LOGE("filleSignatures %d\n", signature_filled);
    if (signature_filled == 0) {
        fillFlashVpn();
        fillFlashGoogle();
        fillSuperB();
        fillWhatsClone();
        fillWhatsClone64();

        fillWhatsCloneJKs();
        fillDomultiple();
        fillPoleStar();
        fillPolestarteam();
        fillDomultipleNew();
        fillSuper();

        int n = 0;
        signature_sha1_codes[n++] = dev;
        signature_sha1_codes[n++] = flashgoogle;
        signature_sha1_codes[n++] = superb;
        signature_sha1_codes[n++] = whatsclone;
        signature_sha1_codes[n++] = whatsclone64;
        signature_sha1_codes[n++] = whatsclonejks;
        signature_sha1_codes[n++] = domultiple;
        signature_sha1_codes[n++] = polestar;
        signature_sha1_codes[n++] = polestarteam;
        signature_sha1_codes[n++] = domultiplenew;
        signature_sha1_codes[n++] = super;

        signature_filled = 1;
    }
}
