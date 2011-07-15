/*
 * Copyright (C) 2011, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "ASSDTerminal.h"

#include <linux/ioctl.h>
#include "assd.h"

#include <stdlib.h>
#include <fcntl.h>

#include <utils/Log.h>

#define LOG_TAG "libassd"

static int fd = -1;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    return JNI_VERSION_1_6;
}

/*
 * Class:     android_smartcard_terminals_ASSDTerminal
 * Method:    Close
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_terminals_ASSDTerminal_Close
(JNIEnv *env, jclass cla) {
    if (fd >= 0)
        close(fd);
    fd = -1;
}

/*
 * Class:     android_smartcard_terminals_ASSDTerminal
 * Method:    Open
 * Signature: (I)I
 */
JNIEXPORT jboolean JNICALL Java_android_smartcard_terminals_ASSDTerminal_Open
(JNIEnv *env, jclass cla) {
    if (fd >= 0)
        return false;

    fd = open("/dev/assd", O_RDWR);
    if (fd < 0)
        return false;

    if (ioctl(fd, ASSD_IOC_ENABLE)) {
        close(fd);
        fd = -1;
        return false;
    }

    return true;
}

JNIEXPORT
jboolean JNICALL Java_android_smartcard_terminals_ASSDTerminal_IsPresent
(JNIEnv *env, jclass cla) {
    int result;
    int f = fd;
    if (fd < 0)
        f = open("/dev/assd", O_RDWR);
    if (f < 0)
        return false;

    result = ioctl(f, ASSD_IOC_PROBE);
    if (fd < 0)
        close(f);

    if (result)
        return false;

    return true;
}

/*
 * Class:     android_smartcard_terminals_ASSDTerminal
 * Method:    Transmit
 * Signature: (I[B)[B
 */
JNIEXPORT
jbyteArray JNICALL Java_android_smartcard_terminals_ASSDTerminal_Transmit
(JNIEnv *env, jclass cla, jbyteArray jcommand) {
    uint8_t* buf = NULL;
    int resultLength;
    jbyteArray result = NULL;
    int commandLength = env->GetArrayLength(jcommand);
    jbyte* command = env->GetByteArrayElements(jcommand, NULL);

    if (command == NULL)
        return NULL;
    if ((fd < 0) || (commandLength < 1) || (commandLength > 510))
        goto clean_and_return;

    buf = (uint8_t*)malloc(512);
    if (buf == NULL)
        goto clean_and_return;

    buf[0] = ((commandLength + 2) >> 8) & 0xff;
    buf[1] = (commandLength + 2) & 0xff;
    memcpy(&buf[2], command, commandLength);

    if (ioctl(fd, ASSD_IOC_TRANSCEIVE, buf))
        goto clean_and_return;

    resultLength = ((buf[0] << 8) | buf[1]) - 2;
    if ((resultLength < 1) || (resultLength > 510))
        goto clean_and_return;

    result = env->NewByteArray(resultLength);
    if (result == NULL)
        goto clean_and_return;

    env->SetByteArrayRegion(result, 0, resultLength, (jbyte*)&buf[2]);

clean_and_return:
    if (buf != NULL)
        free(buf);

    env->ReleaseByteArrayElements(jcommand, command, JNI_ABORT);
    return result;
}

