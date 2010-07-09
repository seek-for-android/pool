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

package android.smartcard.libraries.smartcard.mcex;

/**
 * A <code>McexException</code> is thrown when any type of error occurs using
 * the MCEX device driver.
 */
public class McexException extends Exception {
	private static final long serialVersionUID = 6596489727149436823L;

	/** The error code of the I/O function that caused the exception. */
	private int _errorCode;

	/**
	 * Constructs a new MCEX exception with <code>null</code> as its detail
	 * message and zero error code.
	 */
	public McexException() {
		super();
	}

	/**
	 * Constructs a new MCEX exception with the specified detail message and
	 * zero error code.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later
	 *            retrieval by the <code>Throwable.getMessage()</code> method.
	 */
	public McexException(String message) {
		super(message);
	}

	/**
	 * Constructs a new MCEX exception with the specified cause and zero error code.
	 * 
	 * @param cause
	 *            the causing exception.
	 */
	public McexException(Exception cause) {
		super(cause);
	}

	/**
	 * Constructs a new MCEX exception with the specified detail message and
	 * I/O function error code.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later
	 *            retrieval by the <code>Throwable.getMessage()</code> method.
	 * @param errorCode
	 *            the I/O function error code.
	 */
	public McexException(String message, int errorCode) {
		super(message);
		this._errorCode = errorCode;
	}

	/**
	 * Constructs a new MCEX exception for the specified I/O function name
	 * and error code.
	 * 
	 * @param errorCode
	 *            the I/O function error code.
	 * @param functionName
	 *            the name of the I/O function that caused the
	 *            exception.
	 */
	public McexException(int errorCode, String functionName) {
		super(functionName + " returned error code " + errorCode);
		this._errorCode = errorCode;
	}

	/**
	 * Returns the error code of the I/O function that caused the exception.
	 * 
	 * @return the error code of the I/O function that caused the exception.
	 */
	public int errorCode() {
		return _errorCode;
	}
}