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
/* Header for class android_smartcard_libraries_smartcard_mcex_McexJni */

#ifndef _Included_android_smartcard_libraries_smartcard_mcex_McexJni
#define _Included_android_smartcard_libraries_smartcard_mcex_McexJni
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     android_smartcard_libraries_smartcard_mcex_McexJni
 * Method:    Close
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_android_smartcard_libraries_smartcard_mcex_McexJni_Close
  (JNIEnv *, jobject, jint);

/*
 * Class:     android_smartcard_libraries_smartcard_mcex_McexJni
 * Method:    Open
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_android_smartcard_libraries_smartcard_mcex_McexJni_Open
  (JNIEnv *, jobject, jint);

/*
 * Class:     android_smartcard_libraries_smartcard_mcex_McexJni
 * Method:    Stat
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_android_smartcard_libraries_smartcard_mcex_McexJni_Stat
  (JNIEnv *, jobject);

/*
 * Class:     android_smartcard_libraries_smartcard_mcex_McexJni
 * Method:    Transmit
 * Signature: (I[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_android_smartcard_libraries_smartcard_mcex_McexJni_Transmit
  (JNIEnv *, jobject, jint, jbyteArray);


#ifdef __cplusplus
}
#endif
#endif
