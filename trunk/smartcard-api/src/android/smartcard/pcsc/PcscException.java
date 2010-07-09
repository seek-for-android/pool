/*
 * Copyright 2010 Giesecke & Devrient GmbH.
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

package android.smartcard.pcsc;

/**
 * A <code>PcscException</code> is thrown when any type of error occurs in the
 * PC/SC subsystem. The PC/SC error code is provided as exception data.
 */
public class PcscException extends Exception {
	private static final long serialVersionUID = 6981777740454627772L;

	/**
	 * Constant pool interface for PC/SC error codes.
	 */
	public static abstract class ErrorCode {
		public static final int S_SUCCESS = 0x00000000;
		public static final int F_INTERNAL_ERROR = 0x80100001;
		public static final int E_CANCELLED = 0x80100002;
		public static final int E_INVALID_HANDLE = 0x80100003;
		public static final int E_INVALID_PARAMETER = 0x80100004;
		public static final int E_INVALID_TARGET = 0x80100005;
		public static final int E_NO_MEMORY = 0x80100006;
		public static final int F_WAITED_TOO_LONG = 0x80100007;
		public static final int E_INSUFFICIENT_BUFFER = 0x80100008;
		public static final int E_UNKNOWN_READER = 0x80100009;
		public static final int E_TIMEOUT = 0x8010000A;
		public static final int E_SHARING_VIOLATION = 0x8010000B;
		public static final int E_NO_SMARTCARD = 0x8010000C;
		public static final int E_UNKNOWN_CARD = 0x8010000D;
		public static final int E_CANT_DISPOSE = 0x8010000E;
		public static final int E_PROTO_MISMATCH = 0x8010000F;
		public static final int E_NOT_READY = 0x80100010;
		public static final int E_INVALID_VALUE = 0x80100011;
		public static final int E_SYSTEM_CANCELLED = 0x80100012;
		public static final int F_COMM_ERROR = 0x80100013;
		public static final int F_UNKNOWN_ERROR = 0x80100014;
		public static final int E_INVALID_ATR = 0x80100015;
		public static final int E_NOT_TRANSACTED = 0x80100016;
		public static final int E_READER_UNAVAILABLE = 0x80100017;
		public static final int P_SHUTDOWN = 0x80100018;
		public static final int E_PCI_TOO_SMALL = 0x80100019;
		public static final int E_READER_UNSUPPORTED = 0x8010001A;
		public static final int E_DUPLICATE_READER = 0x8010001B;
		public static final int E_CARD_UNSUPPORTED = 0x8010001C;
		public static final int E_NO_SERVICE = 0x8010001D;
		public static final int E_SERVICE_STOPPED = 0x8010001E;
		public static final int E_UNEXPECTED = 0x8010001F;
		public static final int E_ICC_INSTALLATION = 0x80100020;
		public static final int E_ICC_CREATEORDER = 0x80100021;
		public static final int E_UNSUPPORTED_FEATURE = 0x80100022;
		public static final int E_DIR_NOT_FOUND = 0x80100023;
		public static final int E_FILE_NOT_FOUND = 0x80100024;
		public static final int E_NO_DIR = 0x80100025;
		public static final int E_NO_FILE = 0x80100026;
		public static final int E_NO_ACCESS = 0x80100027;
		public static final int E_WRITE_TOO_MANY = 0x80100028;
		public static final int E_BAD_SEEK = 0x80100029;
		public static final int E_INVALID_CHV = 0x8010002A;
		public static final int E_UNKNOWN_RES_MNG = 0x8010002B;
		public static final int E_NO_SUCH_CERTIFICATE = 0x8010002C;
		public static final int E_CERTIFICATE_UNAVAILABLE = 0x8010002D;
		public static final int E_NO_READERS_AVAILABLE = 0x8010002E;
		public static final int E_COMM_DATA_LOST = 0x8010002F;
		public static final int E_NO_KEY_CONTAINER = 0x80100030;
		public static final int E_SERVER_TOO_BUSY = 0x80100031;
		public static final int W_UNSUPPORTED_CARD = 0x80100065;
		public static final int W_UNRESPONSIVE_CARD = 0x80100066;
		public static final int W_UNPOWERED_CARD = 0x80100067;
		public static final int W_RESET_CARD = 0x80100068;
		public static final int W_REMOVED_CARD = 0x80100069;
		public static final int W_SECURITY_VIOLATION = 0x8010006A;
		public static final int W_WRONG_CHV = 0x8010006B;
		public static final int W_CHV_BLOCKED = 0x8010006C;
		public static final int W_EOF = 0x8010006D;
		public static final int W_CANCELLED_BY_USER = 0x8010006E;
		public static final int W_CARD_NOT_AUTHENTICATED = 0x8010006F;
		public static final int WINAPI_ERROR_INVALID_FUNCTION = 0x00000001;
		public static final int WINAPI_ERROR_INVALID_HANDLE = 0x00000006;
		public static final int WINAPI_ERROR_NOT_SUPPORTED = 0x00000032;
		public static final int WINAPI_ERROR_INVALID_PARAMETER = 0x00000057;
		public static final int WINAPI_ERROR_DEVICE_REMOVED = 0x00000651;
		/** Undefined error code */
		public static final int Undefined = -1;
	}

	/** The error code of the PC/SC function that caused the exception. */
	private int _errorCode = ErrorCode.Undefined;

	/**
	 * Constructs a new PC/SC exception with <code>null</code> as its detail
	 * message and undefined error code.
	 */
	public PcscException() {
		super();
	}

	/**
	 * Constructs a new PC/SC exception with the specified detail message and
	 * undefined error code.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later
	 *            retrieval by the <code>Throwable.getMessage()</code> method.
	 */
	public PcscException(String message) {
		super(message);
	}

	/**
	 * Constructs a new PC/SC exception with the specified cause.
	 * 
	 * @param cause
	 *            the causing exception.
	 */
	public PcscException(Exception cause) {
		super(cause);
	}

	/**
	 * Constructs a new PC/SC exception with the specified detail message and
	 * PC/SC error code.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later
	 *            retrieval by the <code>Throwable.getMessage()</code> method.
	 * @param errorCode
	 *            the PC/SC error code.
	 */
	public PcscException(String message, int errorCode) {
		super(message);
		this._errorCode = errorCode;
	}

	/**
	 * Constructs a new PC/SC exception for the specified PC/SC function name
	 * and error code.
	 * 
	 * @param errorCode
	 *            the PC/SC error code.
	 * @param functionName
	 *            the PC/SC function name (without SCard prefix) that caused the
	 *            exception.
	 */
	public PcscException(int errorCode, String functionName) {
		super("PC/SC SCard" + functionName + " returned error code 0x"
				+ Long.toString(errorCode & 0xFFFFFFFFL, 16).toUpperCase() + ": "
				+ errorCodeMessage(errorCode));
		this._errorCode = errorCode;
	}

	/**
	 * Returns <code>true</code>, if the exception signals that the
	 * communication channel to the card is closed or <code>false</code>, if the
	 * communication channel to the card channel is still open.
	 * 
	 * @return <code>true</code>, if the exception signals that the
	 *         communication channel to the card is closed; <code>false</code>,
	 *         if the communication channel to the card channel is still open.
	 */
	public boolean isCardClosed() {
		if (isReaderClosed())
			return true;

		switch (_errorCode) {
		case ErrorCode.E_NO_SMARTCARD:
		case ErrorCode.W_UNRESPONSIVE_CARD:
		case ErrorCode.W_UNPOWERED_CARD:
		case ErrorCode.W_RESET_CARD:
		case ErrorCode.W_REMOVED_CARD:
		case ErrorCode.WINAPI_ERROR_INVALID_HANDLE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Returns <code>true</code>, if the exception signals that the reader
	 * device is no longer available or <code>false</code>, if the reader is
	 * still available.
	 * 
	 * @return <code>true</code>, if the exception signals that the reader
	 *         device is no longer available; <code>false</code>, if the reader
	 *         is still available.
	 */
	public boolean isReaderClosed() {
		switch (_errorCode) {
		case ErrorCode.E_NO_SERVICE:
		case ErrorCode.E_SERVICE_STOPPED:
		case ErrorCode.E_READER_UNAVAILABLE:
		case ErrorCode.WINAPI_ERROR_DEVICE_REMOVED:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Returns <code>true</code>, if a smart card is not available or
	 * <code>false</code>, for any other reason.
	 * 
	 * @return <code>true</code>, if a smart card is not available or
	 *         <code>false</code>, for any other reason.
	 */
	public boolean noSmartCard() {
		switch (_errorCode) {
		case ErrorCode.E_NO_SMARTCARD:
		case ErrorCode.W_REMOVED_CARD:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Returns the error code of the PC/SC function that caused the exception.
	 * 
	 * @return the error code of the PC/SC function that caused the exception.
	 */
	public int errorCode() {
		return _errorCode;
	}

	/**
	 * Returns an error message for the specified PC/SC error code.
	 * 
	 * @param errorCode
	 *            the PC/SC error code.
	 * @return an error message for the specified PC/SC error code.
	 */
	public static String errorCodeMessage(int errorCode) {
		switch (errorCode) {
		case ErrorCode.S_SUCCESS:
			return "OK";
		case ErrorCode.F_INTERNAL_ERROR:
			return "An internal consistency check failed";
		case ErrorCode.E_CANCELLED:
			return "The action was cancelled by an SCardCancel request";
		case ErrorCode.E_INVALID_HANDLE:
			return "The supplied handle was invalid";
		case ErrorCode.E_INVALID_PARAMETER:
			return "One or more of the supplied parameters could not be properly interpreted";
		case ErrorCode.E_INVALID_TARGET:
			return "Registry startup information is missing or invalid";
		case ErrorCode.E_NO_MEMORY:
			return "Not enough memory available to complete this command";
		case ErrorCode.F_WAITED_TOO_LONG:
			return "An internal consistency timer has expired";
		case ErrorCode.E_INSUFFICIENT_BUFFER:
			return "The data buffer to receive returned data is too small for the returned data";
		case ErrorCode.E_UNKNOWN_READER:
			return "The specified reader name is not recognized";
		case ErrorCode.E_TIMEOUT:
			return "The user-specified timeout value has expired";
		case ErrorCode.E_SHARING_VIOLATION:
			return "The smart card cannot be accessed because of other connections outstanding";
		case ErrorCode.E_NO_SMARTCARD:
			return "The operation requires a Smart Card, but no Smart Card is currently in the device";
		case ErrorCode.E_UNKNOWN_CARD:
			return "The specified smart card name is not recognized";
		case ErrorCode.E_CANT_DISPOSE:
			return "The system could not dispose of the media in the requested manner";
		case ErrorCode.E_PROTO_MISMATCH:
			return "The requested protocols are incompatible with the protocol currently in use with the smart card";
		case ErrorCode.E_NOT_READY:
			return "The reader or smart card is not ready to accept commands";
		case ErrorCode.E_INVALID_VALUE:
			return "One or more of the supplied parameters values could not be properly interpreted";
		case ErrorCode.E_SYSTEM_CANCELLED:
			return "The action was cancelled by the system, presumably to log off or shut down";
		case ErrorCode.F_COMM_ERROR:
			return "An internal communications error has been detected";
		case ErrorCode.F_UNKNOWN_ERROR:
			return "An internal error has been detected, but the source is unknown";
		case ErrorCode.E_INVALID_ATR:
			return "An ATR obtained from the registry is not a valid ATR string";
		case ErrorCode.E_NOT_TRANSACTED:
			return "An attempt was made to end a non-existent transaction";
		case ErrorCode.E_READER_UNAVAILABLE:
			return "The specified reader is not currently available for use";
		case ErrorCode.P_SHUTDOWN:
			return "The operation has been aborted to allow the server application to exit";
		case ErrorCode.E_PCI_TOO_SMALL:
			return "The PCI Receive buffer was too small";
		case ErrorCode.E_READER_UNSUPPORTED:
			return "The reader driver does not meet minimal requirements for support";
		case ErrorCode.E_DUPLICATE_READER:
			return "The reader driver did not produce a unique reader name";
		case ErrorCode.E_CARD_UNSUPPORTED:
			return "The smart card does not meet minimal requirements for support";
		case ErrorCode.E_NO_SERVICE:
			return "The Smart card resource manager is not running";
		case ErrorCode.E_SERVICE_STOPPED:
			return "The Smart card resource manager has shut down";
		case ErrorCode.E_UNEXPECTED:
			return "An unexpected card error has occurred";
		case ErrorCode.E_ICC_INSTALLATION:
			return "No Primary Provider can be found for the smart card";
		case ErrorCode.E_ICC_CREATEORDER:
			return "The requested order of object creation is not supported";
		case ErrorCode.E_UNSUPPORTED_FEATURE:
			return "This smart card does not support the requested feature";
		case ErrorCode.E_DIR_NOT_FOUND:
			return "The identified directory does not exist in the smart card";
		case ErrorCode.E_FILE_NOT_FOUND:
			return "The identified file does not exist in the smart card";
		case ErrorCode.E_NO_DIR:
			return "The supplied path does not represent a smart card directory";
		case ErrorCode.E_NO_FILE:
			return "The supplied path does not represent a smart card file";
		case ErrorCode.E_NO_ACCESS:
			return "Access is denied to this file";
		case ErrorCode.E_WRITE_TOO_MANY:
			return "The smartcard does not have enough memory to store the information";
		case ErrorCode.E_BAD_SEEK:
			return "There was an error trying to set the smart card file object pointer";
		case ErrorCode.E_INVALID_CHV:
			return "The supplied PIN is incorrect";
		case ErrorCode.E_UNKNOWN_RES_MNG:
			return "An unrecognized error code was returned from a layered component";
		case ErrorCode.E_NO_SUCH_CERTIFICATE:
			return "The requested certificate does not exist";
		case ErrorCode.E_CERTIFICATE_UNAVAILABLE:
			return "The requested certificate could not be obtained";
		case ErrorCode.E_NO_READERS_AVAILABLE:
			return "Cannot find a smart card reader";
		case ErrorCode.E_COMM_DATA_LOST:
			return "A communications error with the smart card has been detected.  Retry the operation";
		case ErrorCode.E_NO_KEY_CONTAINER:
			return "The requested key container does not exist on the smart card";
		case ErrorCode.E_SERVER_TOO_BUSY:
			return "The Smart card resource manager is too busy to complete this operation";
		case ErrorCode.W_UNSUPPORTED_CARD:
			return "The reader cannot communicate with the smart card, due to ATR configuration conflicts";
		case ErrorCode.W_UNRESPONSIVE_CARD:
			return "The smart card is not responding to a reset";
		case ErrorCode.W_UNPOWERED_CARD:
			return "Power has been removed from the smart card, so that further communication is not possible";
		case ErrorCode.W_RESET_CARD:
			return "The smart card has been reset, so any shared cardState information is invalid";
		case ErrorCode.W_REMOVED_CARD:
			return "The smart card has been removed, so that further communication is not possible";
		case ErrorCode.W_SECURITY_VIOLATION:
			return "Access was denied because of a security violation";
		case ErrorCode.W_WRONG_CHV:
			return "The card cannot be accessed because the wrong PIN was presented";
		case ErrorCode.W_CHV_BLOCKED:
			return "The card cannot be accessed because the maximum number of PIN entry attempts has been reached";
		case ErrorCode.W_EOF:
			return "The end of the smart card file has been reached";
		case ErrorCode.W_CANCELLED_BY_USER:
			return "The action was cancelled by the user";
		case ErrorCode.W_CARD_NOT_AUTHENTICATED:
			return "No PIN was presented to the smart card";
		case ErrorCode.WINAPI_ERROR_INVALID_FUNCTION:
			return "Incorrect function";
		case ErrorCode.WINAPI_ERROR_INVALID_HANDLE:
			return "The handle is invalid";
		case ErrorCode.WINAPI_ERROR_NOT_SUPPORTED:
			return "The request is not supported";
		case ErrorCode.WINAPI_ERROR_INVALID_PARAMETER:
			return "The parameter is incorrect";
		case ErrorCode.WINAPI_ERROR_DEVICE_REMOVED:
			return "The device has been removed";
		default:
			return "unknown error code";
		}
	}
}
