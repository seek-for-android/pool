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

package android.smartcard.service;

import java.lang.reflect.Constructor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * SEEK service parameter class used to marshal exception information
 * from the SEEK service to clients.
 */
public class SeekError implements Parcelable {
	private String clazz;
	private String message;

	public static final Parcelable.Creator<SeekError> CREATOR = new Parcelable.Creator<SeekError>() {
		public SeekError createFromParcel(Parcel in) {
			return new SeekError(in);
		}

		public SeekError[] newArray(int size) {
			return new SeekError[size];
		}
	};

	/**
	 * Creates an empty seek error container.
	 */
    public SeekError() {
		this.clazz = "";
		this.message = "";
	}

	private SeekError(Parcel in) {
		clazz = in.readString();
		message = in.readString();
	}

	/**
	 * Creates a seek error which creates the specified exception.
	 * @param clazz
	 *          the exception class. <code>null</code> to reset the error information.
	 * @param message
	 *          the exception message.
	 */
	public SeekError(String clazz, String message) {
		this.clazz = (clazz == null) ? "" : clazz;
		this.message = (message == null) ? "" : message;
	}
	
	/**
	 * Clears the error.
	 */
	public void clear() {
		this.clazz = "";
		this.message = "";
	}
	
	/**
	 * Creates the encoded exception. Returns <code>null</code> if empty.
	 * If the encoded exception is neither a RuntimeException nor a CardException, it is
	 * encapsulated in a RuntimeException.
	 * @return the encoded exception or <code>null</code> if empty.
	 */
	@SuppressWarnings("unchecked")
	public Exception createException() {
		try {
			if (clazz.length() == 0)
				return null;
			if (message.length() == 0)
				return (Exception) Class.forName(clazz).newInstance();
			Constructor constructor = Class.forName(clazz).getConstructor(String.class);
			return (Exception) constructor.newInstance(message);
		} catch (Exception e) {
			return null;
		}
	}
	
	public int describeContents() {
		return 0;
	}

	public void readFromParcel(Parcel in) {
		clazz = in.readString();
		message = in.readString();
    }
	
	/**
	 * Sets the error information.
	 * @param clazz
	 *          the exception class. <code>null</code> to reset the error information.
	 * @param message
	 *          the exception message.
	 */
	@SuppressWarnings("unchecked")
	void setError(Class clazz, String message) {
		this.clazz = (clazz == null) ? "" : clazz.getName();
		this.message = (message == null) ? "" : message;
	}
	
	/**
	 * Throws the encoded exception. Does not throw an exception if the container is empty.
	 * If the encoded exception is neither a RuntimeException nor a CardException, it is
	 * encapsulated in a RuntimeException.
	 * @throws RuntimeException
	 *           if the encoded exception is not a CardException.
	 * @throws CardException
	 *           if a CardException is encoded.
	 */
	public void throwException() throws CardException {
		Exception e = createException();
		if (e == null)
			return;
		if (e instanceof CardException)
			throw (CardException) e;
		if (e instanceof RuntimeException)
			throw (RuntimeException) e;
		throw new RuntimeException(e);
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(clazz);
		out.writeString(message);
	}
}
