
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#APP_ABI :=  armeabi  armeabi-v7a arm64-v8a x86_64
APP_ABI :=  armeabi-v7a arm64-v8a

#NDK_TOOLCHAIN_VERSION := clang-ollvm4.0
#NDK_TOOLCHAIN_VERSION := clang3.4-obfuscator

include $(BUILD_EXECUTABLE)
