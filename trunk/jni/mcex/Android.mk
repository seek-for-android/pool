LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE     := mcex
LOCAL_SRC_FILES  := McexJni.cpp McexExceptions.cpp
LOCAL_LDLIBS     := -llog -L$(SYSROOT)/usr/lib 
include $(BUILD_SHARED_LIBRARY)

