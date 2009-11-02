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

#include <stdio.h>
#include <string.h>

#include "McexExceptions.h"

/*
 * throwMcexException
 *
 * Throws a McexException with the specified exception message.
 *
 * return: 0 for success, -1 if exception could not be raised
 */
int throwMcexException(JNIEnv *env, jobject obj, const char *message)
{
  // Prepare exception
  jclass exceptionClass = env->FindClass(MCEX_EXCEPTION_CLASS);
  if (exceptionClass == NULL)
    return -1;
  
  jmethodID constructorID = env->GetMethodID(exceptionClass, "<init>", "(Ljava/lang/String;)V");
  if (constructorID == NULL)
    return -1;
  
  jstring exceptionMessage = env->NewStringUTF(message);
  if ((exceptionMessage) == NULL)
    return -1;

  jobject exceptionInstance = env->NewObject(exceptionClass, constructorID, exceptionMessage);
  if (exceptionInstance == NULL)
    return -1;
  
  // throw exception
  if (env->Throw((jthrowable) exceptionInstance))
    return -1;
  
  return 0;
}

/*
 * throwMcexException
 *
 * Throws a McexException with the function name and return code information
 * of the I/O function that caused the exception.
 *
 * return: 0 for success, -1 if exception could not be raised
 */
int throwMcexException(JNIEnv *env, jobject obj, const char *functionName, int returnCode)
{
  // Prepare exception
  jclass exceptionClass = env->FindClass(MCEX_EXCEPTION_CLASS);
  if (exceptionClass == NULL)
    return -1;
  
  jmethodID constructorID = env->GetMethodID(exceptionClass, "<init>", "(ILjava/lang/String;)V");
  if (constructorID == NULL)
    return -1;
  
  jstring exceptionFunctionName = env->NewStringUTF(functionName);
  if ((exceptionFunctionName) == NULL)
    return -1;

  jobject exceptionInstance = env->NewObject(exceptionClass, constructorID, (jint) returnCode, exceptionFunctionName);
  if (exceptionInstance == NULL)
    return -1;
  
  // throw exception
  if (env->Throw((jthrowable) exceptionInstance))
    return -1;
  
  return 0;
}

/*
 * throwMcexBusyException
 *
 * Throws a McexBusyException with the specified exception message.
 *
 * return: 0 for success, -1 if exception could not be raised
 */
int throwMcexBusyException(JNIEnv *env, jobject obj)
{
  // Prepare exception
  jclass exceptionClass = env->FindClass(MCEX_BUSY_EXCEPTION_CLASS);
  if (exceptionClass == NULL)
    return -1;
  
  jmethodID constructorID = env->GetMethodID(exceptionClass, "<init>", "()V");
  if (constructorID == NULL)
    return -1;
  
  jobject exceptionInstance = env->NewObject(exceptionClass, constructorID);
  if (exceptionInstance == NULL)
    return -1;
  
  // throw exception
  if (env->Throw((jthrowable) exceptionInstance))
    return -1;
  
  return 0;
}

/*
 * throwMcexNotPresentException
 *
 * Throws a McexNotPresentException with the specified exception message.
 *
 * return: 0 for success, -1 if exception could not be raised
 */
int throwMcexNotPresentException(JNIEnv *env, jobject obj)
{
  // Prepare exception
  jclass exceptionClass = env->FindClass(MCEX_NOT_PRESENT_EXCEPTION_CLASS);
  if (exceptionClass == NULL)
    return -1;
  
  jmethodID constructorID = env->GetMethodID(exceptionClass, "<init>", "()V");
  if (constructorID == NULL)
    return -1;
  
  jobject exceptionInstance = env->NewObject(exceptionClass, constructorID);
  if (exceptionInstance == NULL)
    return -1;
  
  // throw exception
  if (env->Throw((jthrowable) exceptionInstance))
    return -1;
  
  return 0;
}

