/*
 * Copyright 2009 Giesecke & Devrient GmbH.
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

#include "McexExceptions.h"
#include "McexJni.h"

#include <stdio.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/stat.h>
#include <android/log.h>

#define LOG_TAG "libmcex"
#define MCEX_DEVICE "/dev/mcex"

#define OPEN_INIT_DEVICE 0x01
#define OPEN_EXCLUSIVE   0x02


int GetStatus();

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
  return JNI_VERSION_1_6;
}

/*
 * Class:     android_smartcard_libraries_smartcard_mcex_McexJni
 * Method:    Close
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_mcex_McexJni_Close
  (JNIEnv *env, jobject obj, jint jfd)
{
  if (jfd != 0)
  {
    close(jfd);
  }
}

/*
 * Class:     android_smartcard_libraries_smartcard_mcex_McexJni
 * Method:    Open
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_android_smartcard_libraries_smartcard_mcex_McexJni_Open
  (JNIEnv *env, jobject obj, jint jmode)
{
  int fd = open(MCEX_DEVICE, O_RDWR);
  if (fd < 0)
  {
    if (GetStatus() == 0)
      throwMcexBusyException(env, obj);
    else
      throwMcexNotPresentException(env, obj);
    return 0;
  }

  if (jmode & OPEN_INIT_DEVICE)
  {
    if (ioctl(fd, 0))
    {
      throwMcexException(env, obj, "ioctl", errno);
      return 0;
    }
  }

  if ((jmode & OPEN_EXCLUSIVE) == 0)
  {
    close(fd);
    fd = 0;
  }

  return fd;
}

/*
 * Class:     android_smartcard_libraries_smartcard_mcex_McexJni
 * Method:    Stat
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_android_smartcard_libraries_smartcard_mcex_McexJni_Stat
  (JNIEnv *env, jobject obj)
{
  return GetStatus();
}

/*
 * Class:     android_smartcard_libraries_smartcard_mcex_McexJni
 * Method:    Transmit
 * Signature: (I[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_mcex_McexJni_Transmit
  (JNIEnv *env, jobject obj, jint jfd, jbyteArray jcommand)
{
  int commandLength = env->GetArrayLength(jcommand);
  jbyte* command = env->GetByteArrayElements(jcommand, NULL);
  if (command == NULL)
    return NULL;

  int fd = jfd;
  if (fd == 0)
  {
    fd = open(MCEX_DEVICE, O_RDWR);
    if (fd < 0)
    {
      env->ReleaseByteArrayElements(jcommand, command, 0);
      if (GetStatus() == 0)
        throwMcexBusyException(env, obj);
      else
        throwMcexNotPresentException(env, obj);
      return NULL;
    }
  }

  int writtenLength = write(fd, command, commandLength);
  env->ReleaseByteArrayElements(jcommand, command, 0);

  if (writtenLength != commandLength)
  {
    if (jfd == 0)
      close(fd);
    throwMcexException(env, obj, "write", writtenLength);
    return NULL;
  }

  if (ioctl(fd, 1))
  {
    if (jfd == 0)
      close(fd);
    throwMcexException(env, obj, "ioctl", errno);
    return NULL;
  }

  char response[512];
  int responseLength = read(fd, response, sizeof(response));
  if (jfd == 0)
    close(fd);

  if (responseLength == 0)
  {
    throwMcexException(env, obj, "read", responseLength);
    return NULL;
  }

  jbyteArray jresponse = env->NewByteArray((jsize) responseLength);
  if (jresponse == NULL)
    return NULL;

  env->SetByteArrayRegion(jresponse, (jsize) 0, (jsize) responseLength, (const jbyte*) response);
  if (env->ExceptionOccurred() != NULL)
    return NULL;

  return jresponse;
}

int GetStatus()
{
  struct stat s;
  return stat(MCEX_DEVICE, &s);
}
