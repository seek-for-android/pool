LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE     := pcsc
LOCAL_SRC_FILES  := PcscJni.cpp PcscExceptions.cpp
LOCAL_LDLIBS     := -lpcsclite -L$(LOCAL_PATH)/lib
LOCAL_CFLAGS     := -I$(LOCAL_PATH)/include
include $(BUILD_SHARED_LIBRARY)

