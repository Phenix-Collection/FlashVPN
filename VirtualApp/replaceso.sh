#!/bin/bash
ls -la .//aesjni/build/intermediates/intermediate-jars/release/jni/armeabi-v7a/libwittermsg.so 
ls -la .//aesjni/build/intermediates/intermediate-jars/release/jni/arm64-v8a/libwittermsg.so
file .//aesjni/build/intermediates/intermediate-jars/release/jni/armeabi-v7a/libwittermsg.so 
file .//aesjni/build/intermediates/intermediate-jars/release/jni/arm64-v8a/libwittermsg.so
cp .//aesjni/build/intermediates/intermediate-jars/release/jni/armeabi-v7a/libwittermsg.so jniprebuilt/libs/armeabi-v7a/
cp .//aesjni/build/intermediates/intermediate-jars/release/jni/arm64-v8a/libwittermsg.so jniprebuilt/libs/arm64-v8a/
