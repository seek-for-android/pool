LOCAL_PATH := $(call my-dir)

common_cflags := \
	-DANDROID \
	-DHAVE_CONFIG_H
	

include $(CLEAR_VARS)
LOCAL_C_INCLUDES := \
	$(LOCAL_PATH) \
	$(LOCAL_PATH)/src \
	$(LOCAL_PATH)/../pcsc-lite-1.8.6/src/PCSC \
	$(LOCAL_PATH)/../pcsc-lite-1.8.6/src/ \
	$(LOCAL_PATH)/../libusb-1.0.3/libusb \
	$(LOCAL_PATH)/../libusb-1.0.3/libusb/os

LOCAL_SRC_FILES:= \
	src/ccid.c \
        src/ccid.h \
        src/ccid_ifdhandler.h \
        src/commands.c \
        src/commands.h \
        src/debug.h \
        src/defs.h \
        src/ifdhandler.c \
        src/utils.c \
        src/utils.h \
        src/ccid_usb.c \
        src/ccid_usb.h \
        src/towitoko/atr.c \
        src/towitoko/atr.h \
        src/towitoko/defines.h \
        src/towitoko/pps.c \
        src/towitoko/pps.h \
        src/openct/buffer.c \
        src/openct/buffer.h \
        src/openct/checksum.c \
        src/openct/checksum.h \
        src/openct/proto-t1.c \
        src/openct/proto-t1.h \
        src/tokenparser.c \
        src/parser.h \
        src/strlcpy.c \
        src/misc.h \
        src/strlcpycat.h \
        src/debug.c \
        src/debug.h

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/

LOCAL_MODULE_TAGS = eng
LOCAL_MODULE:= libccid
LOCAL_SHARED_LIBRARIES := libc libdl libpcsclite
LOCAL_STATIC_LIBRARIES := libusb
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)


