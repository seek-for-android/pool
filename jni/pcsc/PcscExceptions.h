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

/*
 * JNI PC/SC exception handling implementation:
 *  PC/SC API function error codes are converted into exceptions
 *  of class McexException.
 */

#include <jni.h>

#define PCSC_EXCEPTION_CLASS "android/smartcard/libraries/smartcard/pcsc/PcscException"

/*
 * throwPcscException
 *
 * Throws a PcscException with the specified exception message.
 *
 * return: 0 for success, -1 if exception could not be raised
 */
int throwPcscException(JNIEnv *env, jobject obj, const char *message);

/*
 * throwPcscException
 *
 * Throws a PcscException with the function name (without SCard) and return code information
 * of the PC/SC function that caused the exception.
 *
 * return: 0 for success, -1 if exception could not be raised
 */
int throwPcscException(JNIEnv *env, jobject obj, const char *functionName, int returnCode);
