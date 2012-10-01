LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
 core.c \
 descriptor.c \
 io.c \
 sync.c \
 os/linux_usbfs.c

LOCAL_C_INCLUDES += \
$(LOCAL_PATH)/../ \
$(LOCAL_PATH)/os \
$(LOCAL_PATH)/

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/

LOCAL_MODULE_TAGS = eng
LOCAL_MODULE:= libusb
LOCAL_PRELINK_MODULE := false
include $(BUILD_STATIC_LIBRARY)
