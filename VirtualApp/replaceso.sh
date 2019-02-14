#!/bin/bash
ls -la .//aesjni/build/intermediates/intermediate-jars/release/jni/armeabi-v7a/libtwittermsg.so 
ls -la .//aesjni/build/intermediates/intermediate-jars/release/jni/arm64-v8a/libtwittermsg.so
file .//aesjni/build/intermediates/intermediate-jars/release/jni/armeabi-v7a/libtwittermsg.so 
file .//aesjni/build/intermediates/intermediate-jars/release/jni/arm64-v8a/libtwittermsg.so
cp .//aesjni/build/intermediates/intermediate-jars/release/jni/armeabi-v7a/libtwittermsg.so jniprebuilt/libs/armeabi-v7a/
cp .//aesjni/build/intermediates/intermediate-jars/release/jni/arm64-v8a/libtwittermsg.so jniprebuilt/libs/arm64-v8a/
