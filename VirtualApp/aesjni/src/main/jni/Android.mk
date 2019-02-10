
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := twittermsg
LOCAL_SRC_FILES := JNIEncrypt.c	\
                    aes.c \
                    checksignature.c \
                    base64.c \
                    check_emulator.c \
                    debugger.c


#LOCAL_CFLAGS := -fvisibility=hidden   -mllvm -sub -mllvm -fla -mllvm -bcf
LOCAL_CFLAGS := -fvisibility=hidden   -mllvm -sub -mllvm -fla -mllvm -bcf



# LOCAL_SHARED_LIBRARIES := liblog libcutils
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)

# include $(BUILD_EXECUTABLE)
