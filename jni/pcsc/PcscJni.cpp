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

#include "PcscExceptions.h"
#include "PcscJni.h"
#include "winscard.h"

#define NULL 0

/* Smaller response buffers for required for PC/SC lite */
#ifdef _WINDOWS
    #define ATTRIB_RSP_BUF_SIZE 1024
    #define CONTROL_RSP_BUF_SIZE 1024
    #define TRANSMIT_RSP_BUF_SIZE 0x10010
#else
    #define ATTRIB_RSP_BUF_SIZE 264
    #define CONTROL_RSP_BUF_SIZE 264
    #define TRANSMIT_RSP_BUF_SIZE 264
#endif

/*
 * Helper method used to free the specified PC/SC reader states structure and associated resources.
 */
void cleanupReaderStates(JNIEnv *env, jobjectArray jreaderNames, SCARD_READERSTATE *readerStates)
{
  int numberOfReaders = env->GetArrayLength(jreaderNames);

  for (int i = 0; i < numberOfReaders; i++)
  {
    jstring jreaderName = (jstring) env->GetObjectArrayElement(jreaderNames, i);
    if (readerStates[i].szReader != NULL)
    {
        env->ReleaseStringUTFChars(jreaderName, readerStates[i].szReader);
    }
  }
  delete readerStates;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
  return JNI_VERSION_1_6;
}
/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    BeginTransaction
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_BeginTransaction
  (JNIEnv *env, jobject obj, jlong jhCard)
{
  int iRet = SCardBeginTransaction((SCARDHANDLE) jhCard);

  if (iRet != SCARD_S_SUCCESS)
  {
    throwPcscException(env, obj, "BeginTransaction", iRet);
    // return ...
  }
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Cancel
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Cancel
  (JNIEnv *env, jobject obj, jlong jcontext)
{
  int iRet = SCardCancel((SCARDCONTEXT) jcontext);

  if (iRet != SCARD_S_SUCCESS)
  {
    throwPcscException(env, obj, "Cancel", iRet);
    // return ...
  }
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Connect
 * Signature: (JLjava/lang/String;I[I)J
 */
JNIEXPORT jlong JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Connect
  (JNIEnv *env, jobject obj, jlong jcontext, jstring jreader, jint jshareMode, jintArray jprotocol)
{
  SCARDHANDLE hCard = 0;
  DWORD activeProtocol = 0;

  const char* reader = env->GetStringUTFChars(jreader, NULL);
  if (reader == NULL)
    return 0;

  jint* protocol = env->GetIntArrayElements(jprotocol, NULL);
  if (protocol == NULL)
  {
    env->ReleaseStringUTFChars(jreader, reader);
    return 0;
  }

  int iRet = SCardConnect((SCARDCONTEXT) jcontext, reader, (DWORD) jshareMode, (DWORD) protocol[0], &hCard, &activeProtocol);
  env->ReleaseStringUTFChars(jreader, reader);

  if (iRet != SCARD_S_SUCCESS)
  {
    env->ReleaseIntArrayElements(jprotocol, protocol, 0);
    throwPcscException(env, obj, "Connect", iRet);
    return 0;
  }

  protocol[0] = (jint) activeProtocol;
  env->ReleaseIntArrayElements(jprotocol, protocol, 0);
  return (jlong) hCard;
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Control
 * Signature: (JI[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Control
  (JNIEnv *env, jobject obj, jlong jhCard, jint jcontrolCode, jbyteArray jcommand)
{
  DWORD commandLength = 0;
  jbyte* command = NULL;
  if (jcommand != NULL)
  {
    commandLength = (DWORD) env->GetArrayLength(jcommand);
    command = env->GetByteArrayElements(jcommand, NULL);
    if (command == NULL)
      return NULL;
  }

  jbyte response[CONTROL_RSP_BUF_SIZE];
  DWORD responseLength = 0;

  int iRet = SCardControl((SCARDHANDLE) jhCard, (DWORD) jcontrolCode, command, commandLength, response, sizeof(response), &responseLength);
  if (command != NULL)
      env->ReleaseByteArrayElements(jcommand, command, 0);

  if (iRet != SCARD_S_SUCCESS)
  {
    throwPcscException(env, obj, "Control", iRet);
    return NULL;
  }

  jbyteArray jresponse = env->NewByteArray((jsize) responseLength);
  if (jresponse == NULL)
    return NULL;

  env->SetByteArrayRegion(jresponse, (jsize) 0, (jsize) responseLength, response);
  if (env->ExceptionOccurred() != NULL)
    return NULL;

  return jresponse;
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Disconnect
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Disconnect
  (JNIEnv *env, jobject obj, jlong jhCard, jint jdisposition)
{
  int iRet = SCardDisconnect((SCARDHANDLE) jhCard, (DWORD) jdisposition);

  if (iRet != SCARD_S_SUCCESS)
  {
    throwPcscException(env, obj, "Disconnect", iRet);
    // return ...
  }
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    EndTransaction
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_EndTransaction
  (JNIEnv *env, jobject obj, jlong jhCard, jint jdisposition)
{
  int iRet = SCardEndTransaction((SCARDHANDLE) jhCard, (DWORD) jdisposition);

  if (iRet != SCARD_S_SUCCESS)
  {
    throwPcscException(env, obj, "EndTransaction", iRet);
    // return ...
  }
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    EstablishContext
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_EstablishContext
  (JNIEnv *env, jobject obj, jint jscope)
{
  SCARDCONTEXT context;
  int iRet = SCardEstablishContext((DWORD) jscope, NULL, NULL, &context);

  if (iRet != SCARD_S_SUCCESS)
  {
    throwPcscException(env, obj, "EstablishContext", iRet);
    return 0;
  }

  return (jlong) context;
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    GetAttrib
 * Signature: (JI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_GetAttrib
  (JNIEnv *env, jobject obj, jlong jhCard, jint jattrId)
{
  jbyte attr[ATTRIB_RSP_BUF_SIZE];
  DWORD attrLength = ATTRIB_RSP_BUF_SIZE;

  int iRet = SCardGetAttrib((SCARDHANDLE) jhCard, (DWORD) jattrId, (LPBYTE) attr, &attrLength);

  if (iRet != SCARD_S_SUCCESS)
  {
    throwPcscException(env, obj, "GetAttrib", iRet);
    return NULL;
  }

  jbyteArray jattr = env->NewByteArray((jsize) attrLength);
  if (jattr == NULL)
    return NULL;

  env->SetByteArrayRegion(jattr, (jsize) 0, (jsize) attrLength, attr);
  if (env->ExceptionOccurred() != NULL)
    return NULL;

  return jattr;
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    GetStatus
 * Signature: (JJ[Ljava/lang/Object;[I[I)Z
 */
JNIEXPORT jboolean JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_GetStatus
  (JNIEnv *env, jobject obj, jlong jcontext, jlong jtimeout, jobjectArray jreaderNames, jintArray jcurrentStatus, jintArray jeventStatus)
{
  int numberOfReaders = env->GetArrayLength(jreaderNames);

  jint* currentStatus = env->GetIntArrayElements(jcurrentStatus, NULL);
  if (currentStatus == NULL)
    return JNI_TRUE;

  SCARD_READERSTATE *readerStates = new SCARD_READERSTATE[numberOfReaders];
  if (readerStates == NULL)
  {
    env->ReleaseIntArrayElements(jcurrentStatus, currentStatus, 0);
    return JNI_TRUE;
  }

  for (int i = 0; i < numberOfReaders; i++)
  {
    jstring jreaderName = (jstring) env->GetObjectArrayElement(jreaderNames, i);
    readerStates[i].szReader = env->GetStringUTFChars(jreaderName, NULL);
    if (readerStates[i].szReader == NULL)
    {
      cleanupReaderStates(env, jreaderNames, readerStates);
      env->ReleaseIntArrayElements(jcurrentStatus, currentStatus, 0);
      return JNI_TRUE;
    }
    readerStates[i].pvUserData = NULL;
    readerStates[i].dwCurrentState = currentStatus[i];
    readerStates[i].dwEventState = SCARD_STATE_UNAWARE;
    readerStates[i].cbAtr = 0;
  }
  env->ReleaseIntArrayElements(jcurrentStatus, currentStatus, 0);

  int iRet = SCardGetStatusChange((SCARDCONTEXT) jcontext, (DWORD) jtimeout, readerStates, numberOfReaders);

  jint* eventStatus = env->GetIntArrayElements(jeventStatus, NULL);
  if (eventStatus == NULL)
  {
    cleanupReaderStates(env, jreaderNames, readerStates);
    return JNI_TRUE;
  }

  for (int i = 0; i < numberOfReaders; i++)
  {
    eventStatus[i] = (iRet == SCARD_S_SUCCESS) ? readerStates[i].dwEventState : readerStates[i].dwCurrentState;
  }
  env->ReleaseIntArrayElements(jeventStatus, eventStatus, 0);
  cleanupReaderStates(env, jreaderNames, readerStates);

  if (iRet != SCARD_S_SUCCESS)
  {
    if (iRet == SCARD_E_TIMEOUT)
        return JNI_FALSE;
    throwPcscException(env, obj, "GetStatusChange", iRet);
    return JNI_TRUE;
  }

  return JNI_TRUE;
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    GetStatusChange
 * Signature: (JJLjava/lang/String;[I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_GetStatusChange
  (JNIEnv *env, jobject obj, jlong jcontext, jlong jtimeout, jstring jreaderName, jintArray jstatus)
{
  const char* readerName = env->GetStringUTFChars(jreaderName, NULL);
  if (readerName == NULL)
    return NULL;

  jint* status = env->GetIntArrayElements(jstatus, NULL);
  if (status == NULL)
  {
    env->ReleaseStringUTFChars(jreaderName, readerName);
    return NULL;
  }

  SCARD_READERSTATE readerState;
  readerState.szReader = readerName;
  readerState.pvUserData = NULL;
  readerState.dwCurrentState = status[0];
  readerState.dwEventState = SCARD_STATE_UNAWARE;
  readerState.cbAtr = 0;

  status[1] = 0;

  int iRet = SCardGetStatusChange((SCARDCONTEXT) jcontext, (DWORD) jtimeout, &readerState, 1);
  env->ReleaseStringUTFChars(jreaderName, readerName);

  jbyteArray jatr = NULL;
  if (iRet != SCARD_S_SUCCESS)
  {
    env->ReleaseIntArrayElements(jstatus, status, 0);
    if (iRet != SCARD_E_TIMEOUT)
    {
        throwPcscException(env, obj, "GetStatusChange", iRet);
        return NULL;
    }
  }
  else
  {
    status[0] = (jint) readerState.dwEventState;
    status[1] = 1;
    env->ReleaseIntArrayElements(jstatus, status, 0);

    if (readerState.cbAtr > 0)
    {
      jatr = env->NewByteArray((jsize) readerState.cbAtr);
      if (jatr == NULL)
        return NULL;

      env->SetByteArrayRegion(jatr, (jsize) 0, (jsize) readerState.cbAtr, (const jbyte*) readerState.rgbAtr);
      if (env->ExceptionOccurred() != NULL)
        return NULL;
    }
  }
  return jatr;
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    ListReaders
 * Signature: (JLjava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_ListReaders
  (JNIEnv *env, jobject obj, jlong jcontext, jstring jgroups)
{
  DWORD readerLength = 0;

  const char* groups = NULL;
  if (jgroups != NULL)
  {
    groups = env->GetStringUTFChars(jgroups, NULL);
    if (groups == NULL)
      return NULL;
  }

  int iRet = SCardListReaders((SCARDCONTEXT) jcontext, groups, NULL, &readerLength);
  if (iRet != SCARD_S_SUCCESS)
  {
    if (jgroups != NULL)
      env->ReleaseStringUTFChars(jgroups, groups);
    throwPcscException(env, obj, "ListReaders", iRet);
    return NULL;
  }

  char* readers = new char[readerLength];
  if (readers == NULL)
  {
    env->ReleaseStringUTFChars(jgroups, groups);
    throwPcscException(env, obj, "out of memory");
    return NULL;
  }

  iRet = SCardListReaders((SCARDCONTEXT) jcontext, groups, readers, &readerLength);
  env->ReleaseStringUTFChars(jgroups, groups);

  if (iRet != SCARD_S_SUCCESS)
  {
    delete readers;
    throwPcscException(env, obj, "ListReaders", iRet);
    return NULL;
  }

  // Convert multistring separators to '\n'
  for (DWORD i = 0; i < readerLength - 1; i++)
  {
    if (readers[i] == '\0')
      readers[i] = '\n';
  }

  jstring jreaders = env->NewStringUTF(readers);
  delete readers;
  return jreaders;
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Reconnect
 * Signature: (JII[I)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Reconnect
  (JNIEnv *env, jobject obj, jlong jhCard, jint jshareMode, jint jinitialization, jintArray jprotocol)
{
  DWORD activeProtocol = 0;

  jint* protocol = env->GetIntArrayElements(jprotocol, NULL);
  if (protocol == NULL)
    return;

  int iRet = SCardReconnect((SCARDHANDLE) jhCard, (DWORD) jshareMode, (DWORD) protocol[0], (DWORD) jinitialization, &activeProtocol);

  if (iRet != SCARD_S_SUCCESS)
  {
    env->ReleaseIntArrayElements(jprotocol, protocol, 0);
    throwPcscException(env, obj, "Reconnect", iRet);
    return;
  }

  protocol[0] = (jint) activeProtocol;
  env->ReleaseIntArrayElements(jprotocol, protocol, 0);
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    ReleaseContext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_ReleaseContext
  (JNIEnv *env, jobject obj, jlong jcontext)
{
  int iRet = SCardReleaseContext((SCARDCONTEXT) jcontext);

  if (iRet != SCARD_S_SUCCESS)
  {
    throwPcscException(env, obj, "ReleaseContext", iRet);
    // return ...
  }
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Status
 * Signature: (J[I[I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Status
  (JNIEnv *env, jobject obj, jlong jhCard, jintArray jstatus, jintArray jprotocol)
{
  char reader[1024];
  DWORD readerLength = 1024;
  jbyte atr[128];
  DWORD atrLength = 128;
  DWORD state = 0;
  DWORD protocol = 0;

  int iRet = SCardStatus((SCARDHANDLE) jhCard, reader, &readerLength, &state, &protocol, (LPBYTE) atr, &atrLength);

  if (iRet != SCARD_S_SUCCESS)
  {
    throwPcscException(env, obj, "Status", iRet);
    return NULL;
  }

  if (jstatus != NULL)
  {
    jsize statusLength = env->GetArrayLength(jstatus);
    jint* statusArray = env->GetIntArrayElements(jstatus, NULL);
    if (statusArray == NULL)
      return NULL;

    if (statusLength > 0)
    {
#ifndef _WINDOWS
      if (state & SCARD_SPECIFIC)
        state = 6;
      else if (state & SCARD_NEGOTIABLE)
        state = 5;
      else if (state & SCARD_POWERED)
        state = 4;
      else if (state & SCARD_SWALLOWED)
        state = 3;
      else if (state & SCARD_PRESENT)
        state = 2;
      else if (state & SCARD_ABSENT)
        state = 1;
      else
        state = 0;
#endif
      statusArray[0] = (jint) state;
    }
    env->ReleaseIntArrayElements(jstatus, statusArray, 0);
  }

  if (jprotocol != NULL)
  {
    jsize protocolLength = env->GetArrayLength(jprotocol);
    jint* protocolArray = env->GetIntArrayElements(jprotocol, NULL);
    if (protocolArray == NULL)
      return NULL;

    if (protocolLength > 0)
    {
      protocolArray[0] = (jint) protocol;
    }
    env->ReleaseIntArrayElements(jprotocol, protocolArray, 0);
  }

  jbyteArray jatr = env->NewByteArray((jsize) atrLength);
  if (jatr == NULL)
    return NULL;

  env->SetByteArrayRegion(jatr, (jsize) 0, (jsize) atrLength, atr);
  if (env->ExceptionOccurred() != NULL)
    return NULL;

  return jatr;
}

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Transmit
 * Signature: (JI[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Transmit
  (JNIEnv *env, jobject obj, jlong jhCard, jint jprotocol, jbyteArray jcommand)
{
  SCARD_IO_REQUEST sendPci;
  sendPci.dwProtocol = (DWORD) jprotocol;
  sendPci.cbPciLength = sizeof(SCARD_IO_REQUEST);

  DWORD commandLength = (DWORD) env->GetArrayLength(jcommand);
  jbyte* command = env->GetByteArrayElements(jcommand, NULL);
  if (command == NULL)
    return NULL;

  jbyte response[TRANSMIT_RSP_BUF_SIZE];
  DWORD responseLength = TRANSMIT_RSP_BUF_SIZE;

  int iRet = SCardTransmit((SCARDHANDLE) jhCard, &sendPci, (LPCBYTE) command, commandLength, NULL, (LPBYTE) response, &responseLength);
  env->ReleaseByteArrayElements(jcommand, command, 0);

  if (iRet != SCARD_S_SUCCESS)
  {
    throwPcscException(env, obj, "Transmit", iRet);
    return NULL;
  }

  jbyteArray jresponse = env->NewByteArray((jsize) responseLength);
  if (jresponse == NULL)
    return NULL;

  env->SetByteArrayRegion(jresponse, (jsize) 0, (jsize) responseLength, response);
  if (env->ExceptionOccurred() != NULL)
    return NULL;

  return jresponse;
}
