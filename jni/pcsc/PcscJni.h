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

#include <jni.h>
/* Header for class android_smartcard_libraries_smartcard_pcsc_PcscJni */

#ifndef _Included_android_smartcard_libraries_smartcard_pcsc_PcscJni
#define _Included_android_smartcard_libraries_smartcard_pcsc_PcscJni
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    BeginTransaction
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_BeginTransaction
  (JNIEnv *, jobject, jlong);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Cancel
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Cancel
  (JNIEnv *, jobject, jlong);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Connect
 * Signature: (JLjava/lang/String;I[I)J
 */
JNIEXPORT jlong JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Connect
  (JNIEnv *, jobject, jlong, jstring, jint, jintArray);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Control
 * Signature: (JI[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Control
  (JNIEnv *, jobject, jlong, jint, jbyteArray);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Disconnect
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Disconnect
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    EndTransaction
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_EndTransaction
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    EstablishContext
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_EstablishContext
  (JNIEnv *, jobject, jint);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    GetAttrib
 * Signature: (JI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_GetAttrib
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    GetStatus
 * Signature: (JJ[Ljava/lang/Object;[I[I)Z
 */
JNIEXPORT jboolean JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_GetStatus
  (JNIEnv *, jobject, jlong, jlong, jobjectArray, jintArray, jintArray);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    GetStatusChange
 * Signature: (JJLjava/lang/String;[I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_GetStatusChange
  (JNIEnv *, jobject, jlong, jlong, jstring, jintArray);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    ListReaders
 * Signature: (JLjava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_ListReaders
  (JNIEnv *, jobject, jlong, jstring);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Reconnect
 * Signature: (JII[I)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Reconnect
  (JNIEnv *, jobject, jlong, jint, jint, jintArray);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    ReleaseContext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_ReleaseContext
  (JNIEnv *, jobject, jlong);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Status
 * Signature: (J[I[I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Status
  (JNIEnv *, jobject, jlong, jintArray, jintArray);

/*
 * Class:     android_smartcard_libraries_smartcard_pcsc_PcscJni
 * Method:    Transmit
 * Signature: (JI[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_pcsc_PcscJni_Transmit
  (JNIEnv *, jobject, jlong, jint, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
