LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := jnidemo
LOCAL_SRC_FILES := NatvieUtils.cpp
LOCAL_LDFLAGS := -L$(LOCAL_PATH)/vvw/libs/$(TARGET_ARCH_ABI)
LOCAL_LDLIBS := \
   -lz \
   -lm \
   -lvvw\
#LOCAL_SHARED_LIBRARIES := vvw
include $(BUILD_SHARED_LIBRARY)