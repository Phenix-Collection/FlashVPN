//
// Created by huan zheng on 2019/1/30.
//

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring
//package com.polestar.superclone.component.activity;
//package com.example.huanzheng.testjni2;


JNICALL
Java_com_polestar_superclone_component_activity_LauncherActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    //jstring hello = (jstring) "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
