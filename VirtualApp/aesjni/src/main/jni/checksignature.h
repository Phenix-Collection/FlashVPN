//
// Created by wei on 16-12-4.
//

#define   LOG_TAG    "code"
#define   LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#ifndef AESJNIENCRYPT_SIGNACTURECHECK_H
#define AESJNIENCRYPT_SIGNACTURECHECK_H

/**
 * whatsclone -1048786450
 * domultiple.jks -1208429200
 * polestar.jks 1642031816
 * polestar-team.jks 1404035346
 * domultipe-new.jks 128670161
 * nova-vpn.jks -2145233489
 */
//合法的APP包名
static const char *app_packageName = "com.androidyuan.aesjniencrypt";
//合法的hashcode -625644214:这个值是我生成的这个可以store文件的hash值
static const int app_signature_hash_code = -1085342784;

static const int signature_hash_codes[] = {-1085342784, -1048786450, -1208429200,
                                           1642031816, 1404035346, 128670161, -2145233489};

/**
 * 校验APP 包名和签名是否合法
 *
 * 返回值为1 表示合法
 */
jint check_signature(JNIEnv *env, jobject thiz, jobject context);

#endif //AESJNIENCRYPT_SIGNACTURECHECK_H
