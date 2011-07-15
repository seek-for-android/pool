LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PRELINK_MODULE := false
LOCAL_MODULE      := libassd
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES   := ASSDTerminal.cpp
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_SHARED_LIBRARIES := libutils

include $(BUILD_SHARED_LIBRARY)

