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
/*
 * Contributed by: Giesecke & Devrient GmbH.
 */

package org.simalliance.openmobileapi.service;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Constructor;

/**
 * Smartcard service parameter class used to marshal exception information from
 * the smartcard service to clients.
 */
public class SmartcardError implements Parcelable {
    private String mClazz;

    private String mMessage;

    public static final Parcelable.Creator<SmartcardError> CREATOR = new Parcelable.Creator<SmartcardError>() {
        public SmartcardError createFromParcel(Parcel in) {
            return new SmartcardError(in);
        }

        public SmartcardError[] newArray(int size) {
            return new SmartcardError[size];
        }
    };

    /**
     * Creates an empty smartcard error container.
     */
    public SmartcardError() {
        this.mClazz = "";
        this.mMessage = "";
    }

    private SmartcardError(Parcel in) {
        mClazz = in.readString();
        mMessage = in.readString();
    }

    /**
     * Creates a smartcard error which creates the specified exception.
     * 
     * @param clazz the exception class. <code>null</code> to reset the error
     *            information.
     * @param message the exception message.
     */
    public SmartcardError(String clazz, String message) {
        this.mClazz = (clazz == null) ? "" : clazz;
        this.mMessage = (message == null) ? "" : message;
    }

    /**
     * Clears the error.
     */
    public void clear() {
        this.mClazz = "";
        this.mMessage = "";
    }

    /**
     * Creates the encoded exception. Returns <code>null</code> if empty. If the
     * encoded exception is neither a RuntimeException nor a CardException, it
     * is encapsulated in a RuntimeException.
     * 
     * @return the encoded exception or <code>null</code> if empty.
     */
    @SuppressWarnings("unchecked")
    public Exception createException() {
        try {
            if (mClazz.length() == 0) {
                return null;
            }
            if (mMessage.length() == 0) {
                return (Exception) Class.forName(mClazz).newInstance();
            }
            Constructor constructor = Class.forName(mClazz).getConstructor(String.class);
            return (Exception) constructor.newInstance(mMessage);
        } catch (Exception e) {
            return null;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        mClazz = in.readString();
        mMessage = in.readString();
    }

    /**
     * Sets the error information.
     * 
     * @param clazz the exception class. <code>null</code> to reset the error
     *            information.
     * @param message the exception message.
     */
    @SuppressWarnings("unchecked")
    public void setError(Class clazz, String message) {
        this.mClazz = (clazz == null) ? "" : clazz.getName();
        this.mMessage = (message == null) ? "" : message;
    }

    /**
     * Throws the encoded exception. Does not throw an exception if the
     * container is empty. If the encoded exception is neither a
     * RuntimeException nor a CardException, it is encapsulated in a
     * RuntimeException.
     * 
     * @throws RuntimeException if the encoded exception is not a CardException.
     * @throws CardException if a CardException is encoded.
     */
    public void throwException() throws CardException {
        Exception e = createException();
        if (e == null) {
            return;
        }
        if (e instanceof CardException) {
            throw (CardException) e;
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mClazz);
        out.writeString(mMessage);
    }
}
