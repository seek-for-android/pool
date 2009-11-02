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
 * JNI MCEX exception handling implementation:
 *  I/O function error codes are converted into exceptions
 *  of class McexException.
 */

#include <jni.h>

#define MCEX_EXCEPTION_CLASS "android/smartcard/libraries/smartcard/mcex/McexException"
#define MCEX_BUSY_EXCEPTION_CLASS "android/smartcard/libraries/smartcard/mcex/McexBusyException"
#define MCEX_NOT_PRESENT_EXCEPTION_CLASS "android/smartcard/libraries/smartcard/mcex/McexNotPresentException"

/*
 * throwMcexException
 *
 * Throws a McexException with the specified exception message.
 *
 * return: 0 for success, -1 if exception could not be raised
 */
int throwMcexException(JNIEnv *env, jobject obj, const char *message);

/*
 * throwMcexException
 *
 * Throws a McexException with the function name and return code information
 * of the I/O function that caused the exception.
 *
 * return: 0 for success, -1 if exception could not be raised
 */
int throwMcexException(JNIEnv *env, jobject obj, const char *functionName, int returnCode);

/*
 * throwMcexBusyException
 *
 * Throws a McexBusyException.
 *
 * return: 0 for success, -1 if exception could not be raised
 */
int throwMcexBusyException(JNIEnv *env, jobject obj);

/*
 * throwMcexNotPresentException
 *
 * Throws a McexNotPresentException.
 *
 * return: 0 for success, -1 if exception could not be raised
 */
int throwMcexNotPresentException(JNIEnv *env, jobject obj);

