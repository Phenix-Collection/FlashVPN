LOCAL_PATH := $(call my-dir)
MAIN_LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := spc-native

LOCAL_CFLAGS := -Wno-error=format-security -fpermissive
LOCAL_CFLAGS += -fno-rtti -fno-exceptions

USE_HOOKZZ := n

ifeq ($(TARGET_ARCH_ABI),x86)
    LOCAL_CFLAGS += -D_NOT_HOOK_DL_OPEN_
else ifeq ($(TARGET_ARCH_ABI),x86_64)
    LOCAL_CFLAGS += -D_NOT_HOOK_DL_OPEN_
else ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    USE_HOOKZZ = y
endif


LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Foundation

LOCAL_SRC_FILES := core.cpp \
				   Foundation/IOUniformer.cpp \
				   Foundation/VMPatch.cpp

LOCAL_LDLIBS := -llog

ifeq ($(USE_HOOKZZ),y)
    LOCAL_CFLAGS += -DUSE_HOOKZZ
    LOCAL_STATIC_LIBRARIES := hookzz
    LOCAL_SRC_FILES += Foundation/SymbolFinder.cpp
else
    LOCAL_C_INCLUDES += $(LOCAL_PATH)/InlineHook
    LOCAL_SRC_FILES += InlineHook/MSHook.cpp \
    				   InlineHook/x86_64.cpp \
    				   InlineHook/ARM.cpp \
    				   InlineHook/Debug.cpp \
    				   InlineHook/Hooker.cpp \
    				   InlineHook/PosixMemory.cpp \
    				   InlineHook/Thumb.cpp \
    				   InlineHook/util.cpp \
    				   InlineHook/x86.cpp
endif


include $(BUILD_SHARED_LIBRARY)

ifeq ($(USE_HOOKZZ),y)
    include $(MAIN_LOCAL_PATH)/HookZz/Android.mk
endif
include $(MAIN_LOCAL_PATH)/crashlogger/Android.mk
