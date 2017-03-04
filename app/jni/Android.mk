LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libjpeg
LOCAL_SRC_FILES := libjpeg.so
include $(PREBUILT_SHARED_LIBRARY)
include $(CLEAR_VARS)

LOCAL_MODULE    := libimgcompress
LOCAL_SRC_FILES := libimgcompress.c
LOCAL_SHARED_LIBRARIES := libjpeg
LOCAL_LDLIBS := -ljnigraphics -llog
LOCAL_C_INCLUDES := $(LOCAL_PATH) \
                    $(LOCAL_PATH)/jpeg \
                    $(LOCAL_PATH)/jpeg/android   

include $(BUILD_SHARED_LIBRARY)
