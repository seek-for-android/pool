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

import android.smartcard.pcsc.PcscException.ErrorCode;

/**
 * PC/SC library JNI wrapper for card terminals.
 */
public final class PcscJni {
	
	/** Constant pool for PC/SC initialization and disposition constants. */
	public static abstract class Disposition {
		/** Eject the card on close. */
		public static final int Eject = 3;

		/** Don't do anything special on close. */
		public static final int Leave = 0;

		/** Reset the card on close. */
		public static final int Reset = 1;

		/** Power down the card on close. */
		public static final int Unpower = 2;
	}

	/** Constant pool for PC/SC transport protocol constants. */
	public static abstract class Protocol {
		/** RAW protocol. */
		public static final int Raw = 0x04;

		/** T=0 protocol. */
		public static final int T0 = 0x01;

		/** T=1 protocol. */
		public static final int T1 = 0x02;
	}

	/** Constant pool for PC/SC reader cardState constants. */
	public static abstract class ReaderState {
		/**
		 * This implies that there is a card in the reader with an ATR matching
		 * one of the target cards. If this bit is set, <code>Present</code>
		 * will also be set. This bit is only returned on the
		 * <code>LocateCard()</code> service (Not supported yet).
		 */
		public static final int AtrMatch = 0x00000040;

		/**
		 * This implies that there is a difference between the cardState
		 * believed by the application, and the cardState known by the Service
		 * Manager. When this bit is set, the application may assume a
		 * significant cardState change has occurred on this reader.
		 */
		public static final int Changed = 0x00000002;

		/**
		 * This implies that there is not card in the reader. If this bit is
		 * set, all the following bits will be clear.
		 */
		public static final int Empty = 0x00000010;

		/**
		 * This implies that the card in the reader is allocated for exclusive
		 * use by another application. If this bit is set, <code>Present</code>
		 * will also be set.
		 */
		public static final int Exclusive = 0x00000080;

		/**
		 * The application requested that this reader be ignored. No other bits
		 * will be set.
		 */
		public static final int Ingore = 0x00000001;

		/**
		 * This implies that the card in the reader is in use by one or more
		 * other applications, but may be connected to in shared mode. If this
		 * bit is set, <code>Present</code> will also be set.
		 */
		public static final int Inuse = 0x00000100;

		/**
		 * This implies that the card in the reader is unresponsive or not
		 * supported by the reader or software.
		 */
		public static final int Mute = 0x00000200;

		/**
		 * This implies that there is a card in the reader.
		 */
		public static final int Present = 0x00000020;

		/**
		 * This implies that the actual cardState of this reader is not
		 * available. If this bit is set, then all the following bits are clear.
		 */
		public static final int Unavailable = 0x00000008;

		/**
		 * The application is unaware of the current cardState, and would like
		 * to know. The use of this value results in an immediate return from
		 * cardState transition monitoring services. This is represented by all
		 * bits set to zero.
		 */
		public static final int Unaware = 0x00000000;

		/**
		 * This implies that the given reader name is not recognized by the
		 * Service Manager. If this bit is set, then <code>Changed</code> and
		 * <code>Ignore</code> will also be set.
		 */
		public static final int Unknown = 0x00000004;

		/**
		 * This implies that the card in the reader has not been powered up.
		 */
		public static final int Unpowered = 0x00000400;
	}

	/** Constant pool for PC/SC scope constants. */
	public static abstract class Scope {
		/**
		 * The context is the system context, and any database operations are
		 * performed within the domain of the system. (The calling application
		 * must have appropriate access permissions for any database actions.)
		 */
		public static final int System = 2;

		/**
		 * The context is that of the current terminal, and any database
		 * operations are performed within the domain of that terminal. (The
		 * calling application must have appropriate access permissions for any
		 * database actions.)
		 */
		public static final int Terminal = 1;

		/**
		 * The context is a user context, and any database operations are
		 * performed within the domain of the user.
		 */
		public static final int User = 0;

		/**
		 * Returns the scope value for the specified scope name. Scope.User is
		 * returned if the specified name is not known.
		 * 
		 * @param name
		 *            the name of the scope.
		 * @return the scope value for the specified scope name.
		 */
		public static int from(String name) {
			int scope = PcscJni.Scope.User;
			if ("Terminal".equalsIgnoreCase(name))
				scope = PcscJni.Scope.Terminal;
			else if ("System".equalsIgnoreCase(name))
				scope = PcscJni.Scope.System;
			return scope;
		}

		/**
		 * Returns the name of the specified scope. An empty string is returned
		 * for an unknown scope.
		 * 
		 * @param scope
		 *            the scope for which to return a name.
		 * @return the name of the specified scope.
		 */
		public static String toString(int scope) {
			switch (scope) {
			case User:
				return "User";
			case Terminal:
				return "Terminal";
			case System:
				return "System";
			default:
				return "";
			}
		}
	}

	/** Constant pool for PC/SC access mode constants. */
	public static abstract class ShareMode {
		/**
		 * This application demands direct control of the reader, so it is not
		 * available to other applications.
		 */
		public static final int Direct = 3;

		/**
		 * This application is not willing to share this card with other
		 * applications.
		 */
		public static final int Exclusive = 1;

		/**
		 * This application is willing to share this card with other
		 * applications.
		 */
		public static final int Shared = 2;
	}

	/** Constant pool for PC/SC status constants. */
	public static abstract class Status {
		/** This value implies there is no card in the reader. */
		public static final int Absent = 1;

		/**
		 * This value implies the card has been reset and is awaiting PTS
		 * negotiation.
		 */
		public static final int Negotiable = 5;

		/**
		 * This value implies there is power is being provided to the card, but
		 * the Reader Driver is unaware of the mode of the card.
		 */
		public static final int Powered = 4;

		/**
		 * This value implies there is a card is present in the reader, but that
		 * it has not been moved into position for use.
		 */
		public static final int Present = 2;

		/**
		 * This value implies the card has been reset and specific communication
		 * protocols have been established.
		 */
		public static final int Specific = 6;

		/**
		 * This value implies there is a card in the reader in position for use.
		 * The card is not powered.
		 */
		public static final int Swallowed = 3;

		/**
		 * This value implies the driver is unaware of the current cardState of
		 * the reader.
		 */
		public static final int Unknown = 0;
	}

	/**
	 * PC/SC library load exception; <code>null</code> if the library was loaded
	 * successfully.
	 */
	private static Throwable loadException;

	/**
	 * Returns the PC/SC library load exception or <code>null</code> if the
	 * library was loaded successfully.
	 * 
	 * @return the PC/SC library load exception or <code>null</code> if the
	 *         library was loaded successfully.
	 */
	public static Throwable getLoadError() {
		return loadException;
	}

	/**
	 * Returns <code>true</code> if the PC/SC native library was loaded
	 * successfully or <code>false</code> if the PC/SC JNI is not available.
	 * 
	 * @return <code>true</code> if the PC/SC native library was loaded
	 *         successfully or <code>false</code> if the PC/SC JNI is not
	 *         available.
	 */
	public static boolean isLoaded() {
		return (loadException == null);
	}

	static {
		try {
			Runtime.getRuntime().loadLibrary("pcsc");
		} catch (Throwable t) {
			loadException = t;
		}
	}

	private PcscJni() {
	}

	/**
	 * Calls the PC/SC API function SCardBeginTransaction().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @throws PcscException
	 *             if SCardBeginTransaction() does not return SCARD_S_SUCCESS.
	 */
	public static void beginTransaction(long hCard) throws PcscException {
		try {
			BeginTransaction(hCard);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardBeginTransaction().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @throws PcscException
	 *             if SCardBeginTransaction() does not return SCARD_S_SUCCESS.
	 */
	private static native void BeginTransaction(long hCard) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardCancel().
	 * 
	 * @param context
	 *            established resource manager context.
	 * @throws PcscException
	 *             if SCardCancel() does not return SCARD_S_SUCCESS.
	 */
	public static void cancel(long context) throws PcscException {
		try {
			Cancel(context);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardCancel().
	 * 
	 * @param context
	 *            established resource manager context.
	 * @throws PcscException
	 *             if SCardCancel() does not return SCARD_S_SUCCESS.
	 */
	private static native void Cancel(long context) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardConnect().
	 * 
	 * @param context
	 *            established resource manager context.
	 * @param readerName
	 *            the name of the reader that contains the target card.
	 * @param shareMode
	 *            a flag that indicates whether other applications may form
	 *            connections to the card.
	 * @param protocol
	 *            on input protocol[0] encodes the preferred protocols; on
	 *            output protocol[0] encodes the active protocol.
	 * @return a handle that identifies the connection to the smart card in the
	 *         designated reader.
	 * @throws PcscException
	 *             if SCardConnect() does not return SCARD_S_SUCCESS.
	 */
	public static long connect(long context, String readerName, int shareMode, int[] protocol)
			throws PcscException {
		if (readerName == null)
			throw new PcscException("illegal readerName parameter");
		if (protocol == null || protocol.length < 1)
			throw new PcscException("illegal protocol parameter");
		try {
			return Connect(context, readerName, shareMode, protocol);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardConnect().
	 * 
	 * @param context
	 *            established resource manager context.
	 * @param readerName
	 *            the name of the reader that contains the target card.
	 * @param shareMode
	 *            a flag that indicates whether other applications may form
	 *            connections to the card.
	 * @param protocol
	 *            on input protocol[0] encodes the preferred protocols; on
	 *            output protocol[0] encodes the active protocol.
	 * @return a handle that identifies the connection to the smart card in the
	 *         designated reader.
	 * @throws PcscException
	 *             if SCardConnect() does not return SCARD_S_SUCCESS.
	 */
	private static native long Connect(long context, String readerName, int shareMode, int[] protocol)
			throws PcscException;

	/**
	 * Calls the PC/SC API function SCardControl().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param controlCode
	 *            Control code for the operation. This value identifies the
	 *            specific operation to be performed.
	 * @param command
	 *            command data to be sent to the reader.
	 * @return the response data from the reader.
	 * @throws PcscException
	 *             if SCardControl() does not return SCARD_S_SUCCESS.
	 */
	public static byte[] control(long hCard, int controlCode, byte[] command) throws PcscException {
		try {
			return Control(hCard, controlCode, command);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardControl().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param controlCode
	 *            Control code for the operation. This value identifies the
	 *            specific operation to be performed.
	 * @param command
	 *            command data to be sent to the reader.
	 * @return the response data from the reader.
	 * @throws PcscException
	 *             if SCardControl() does not return SCARD_S_SUCCESS.
	 */
	private static native byte[] Control(long hCard, int controlCode, byte[] command) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardDisconnect().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param disposition
	 *            action to take on the card in the connected reader on close.
	 * @throws PcscException
	 *             if SCardDisconnect() does not return SCARD_S_SUCCESS.
	 */
	public static void disconnect(long hCard, int disposition) throws PcscException {
		try {
			Disconnect(hCard, disposition);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardDisconnect().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param disposition
	 *            action to take on the card in the connected reader on close.
	 * @throws PcscException
	 *             if SCardDisconnect() does not return SCARD_S_SUCCESS.
	 */
	private static native void Disconnect(long hCard, int disposition) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardEndTransaction().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param disposition
	 *            action to take on the card in the connected reader on close.
	 * @throws PcscException
	 *             if SCardEndTransaction() does not return SCARD_S_SUCCESS.
	 */
	public static void endTransaction(long hCard, int disposition) throws PcscException {
		try {
			EndTransaction(hCard, disposition);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardEndTransaction().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param disposition
	 *            action to take on the card in the connected reader on close.
	 * @throws PcscException
	 *             if SCardEndTransaction() does not return SCARD_S_SUCCESS.
	 */
	private static native void EndTransaction(long hCard, int disposition) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardEstablishContext().
	 * 
	 * @param scope
	 *            scope of the resource manager context.
	 * @return established resource manager context
	 * @throws PcscException
	 *             if SCardEstablishContext() does not return SCARD_S_SUCCESS.
	 */
	public static long establishContext(int scope) throws PcscException {
		try {
			return EstablishContext(scope);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardEstablishContext().
	 * 
	 * @param scope
	 *            scope of the resource manager context.
	 * @return established resource manager context
	 * @throws PcscException
	 *             if SCardEstablishContext() does not return SCARD_S_SUCCESS.
	 */
	private static native long EstablishContext(int scope) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardGetAttrib().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param attrId
	 *            identifier for the attribute to get.
	 * @return the binary value of the attribute.
	 * @throws PcscException
	 *             if SCardGetAttrib() does not return SCARD_S_SUCCESS.
	 */
	public static byte[] getAttrib(long hCard, int attrId) throws PcscException {
		try {
			return GetAttrib(hCard, attrId);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardGetAttrib().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param attrId
	 *            identifier for the attribute to get.
	 * @return the binary value of the attribute.
	 * @throws PcscException
	 *             if SCardGetAttrib() does not return SCARD_S_SUCCESS.
	 */
	private static native byte[] GetAttrib(long hCard, int attrId) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardGetStatusChange().
	 * 
	 * @param context
	 *            established resource manager context.
	 * @param timeout
	 *            maximum amount of time (in milliseconds) to wait for an
	 *            action. A value of zero causes the function to return
	 *            immediately. A value of -1 causes this function never to time
	 *            out.
	 * @param readerNames
	 *            the names of the readers to be monitored.
	 * @param currentStatus
	 *            the current states of the readers as seen by the application.
	 * @param eventStatus
	 *            the event states as known by the smart card resource manager.
	 * @return <code>true</code> if a change occurred within the specified
	 *         timeout or false if no change occurred.
	 * @throws PcscException
	 *             if SCardGetStatusChange() does not return SCARD_S_SUCCESS.
	 */
	public static boolean getStatus(long context, long timeout, String[] readerNames, int[] currentStatus,
			int[] eventStatus) throws PcscException {
		if ((readerNames.length != currentStatus.length) || (readerNames.length != eventStatus.length))
			throw new PcscException("inconsistent parameter array length");

		try {
			return GetStatus(context, timeout, readerNames, currentStatus, eventStatus);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardGetStatusChange().
	 * 
	 * @param context
	 *            established resource manager context.
	 * @param timeout
	 *            maximum amount of time (in milliseconds) to wait for an
	 *            action. A value of zero causes the function to return
	 *            immediately. A value of -1 causes this function never to time
	 *            out.
	 * @param readerNames
	 *            the names of the readers to be monitored.
	 * @param currentStatus
	 *            the current states of the readers as seen by the application.
	 * @param eventStatus
	 *            the event states as known by the smart card resource manager.
	 * @return <code>true</code> if a change occurred within the specified
	 *         timeout or false if no change occurred.
	 * @throws PcscException
	 *             if SCardGetStatusChange() does not return SCARD_S_SUCCESS.
	 */
	private static native boolean GetStatus(long context, long timeout, String[] readerNames,
			int[] currentStatus, int[] eventStatus) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardGetStatusChange().
	 * 
	 * @param context
	 *            established resource manager context.
	 * @param timeout
	 *            maximum amount of time (in milliseconds) to wait for an
	 *            action. A value of zero causes the function to return
	 *            immediately. A value of -1 causes this function never to time
	 *            out.
	 * @param readerName
	 *            the name of the reader.
	 * @param status
	 *            on input status[0] contains the current cardState as seen by
	 *            the application. On output status[0] contains the current
	 *            cardState as known by the smart card resource manager, and
	 *            status[1] is nonzero if the operation completed within the
	 *            specified timeout or zero if no change occurred.
	 * @return the ATR of the card or <code>null</code> if the ATR is not
	 *         available.
	 * @throws PcscException
	 *             if SCardGetStatusChange() does not return SCARD_S_SUCCESS.
	 */
	public static byte[] getStatusChange(long context, long timeout, String readerName, int status[])
			throws PcscException {
		if (readerName == null)
			throw new PcscException("illegal reader name parameter");
		if (status == null || status.length < 2)
			throw new PcscException("illegal status parameter");

		try {
			byte[] atr = GetStatusChange(context, timeout, readerName, status);
			if (status[1] != 0) {
				if ((status[0] & ReaderState.Unknown) == ReaderState.Unknown)
					throw new PcscException("Unknown PC/SC reader", PcscException.ErrorCode.E_UNKNOWN_READER);
			}
			return atr;
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardGetStatusChange().
	 * 
	 * @param context
	 *            established resource manager context.
	 * @param timeout
	 *            maximum amount of time (in milliseconds) to wait for an
	 *            action. A value of zero causes the function to return
	 *            immediately. A value of -1 causes this function never to time
	 *            out.
	 * @param readerName
	 *            the name of the reader.
	 * @param status
	 *            on input status[0] contains the current cardState as seen by
	 *            the application. On output status[0] contains the current
	 *            cardState as known by the smart card resource manager, and
	 *            status[1] is nonzero if the operation completed within the
	 *            specified timeout or zero if no change occurred.
	 * @return the ATR of the card or <code>null</code> if the ATR is not
	 *         available.
	 * @throws PcscException
	 *             if SCardGetStatusChange() does not return SCARD_S_SUCCESS.
	 */
	private static native byte[] GetStatusChange(long context, long timeout, String readerName, int status[])
			throws PcscException;

	/**
	 * Calls the PC/SC API function SCardListReaders().
	 * 
	 * @param context
	 *            established resource manager context.
	 * @param groups
	 *            array of reader group names defined to the system. Use a
	 *            <code>null</code> reference to list all readers in the system.
	 * @return a list of friendly reader names.
	 * @throws PcscException
	 *             if SCardListReaders() does not return SCARD_S_SUCCESS.
	 */
	public static String[] listReaders(long context, String[] groups) throws PcscException {
		try {
			String multiGroups = "\0";
			if (groups != null) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < groups.length; i++) {
					buf.append(groups[i]).append('\0');
				}
				buf.append('\0');
				multiGroups = buf.toString();
			}
			String readers = ListReaders(context, multiGroups);
			return readers.split("\n");
		} catch (PcscException e) {
			if (e.errorCode() == ErrorCode.E_NO_READERS_AVAILABLE)
				return new String[0];
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardListReaders().
	 * 
	 * @param context
	 *            established resource manager context.
	 * @param groups
	 *            names of the reader groups defined to the system, separated by
	 *            newline characters. Use a <code>null</code> reference to list
	 *            all readers in the system.
	 * @return a single string of friendly reader names separated by '\0'
	 *         characters.
	 * @throws PcscException
	 *             if SCardListReaders() does not return SCARD_S_SUCCESS.
	 */
	private static native String ListReaders(long context, String groups) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardReconnect().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param shareMode
	 *            a flag that indicates whether other applications may form
	 *            connections to the card.
	 * @param initialization
	 *            type of initialization that should be performed on the card.
	 * @param protocol
	 *            on input protocol[0] encodes the preferred protocols; on
	 *            output protocol[0] encodes the active protocol.
	 * @throws PcscException
	 *             if SCardReconnect() does not return SCARD_S_SUCCESS.
	 */
	public static void reconnect(long hCard, int shareMode, int initialization, int[] protocol)
			throws PcscException {
		if (protocol == null || protocol.length < 1)
			throw new PcscException("illegal protocol parameter");

		try {
			Reconnect(hCard, shareMode, initialization, protocol);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardReconnect().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param shareMode
	 *            a flag that indicates whether other applications may form
	 *            connections to the card.
	 * @param initialization
	 *            type of initialization that should be performed on the card.
	 * @param protocol
	 *            on input protocol[0] encodes the preferred protocols; on
	 *            output protocol[0] encodes the active protocol.
	 * @throws PcscException
	 *             if SCardReconnect() does not return SCARD_S_SUCCESS.
	 */
	private static native void Reconnect(long hCard, int shareMode, int initialization, int[] protocol)
			throws PcscException;

	/**
	 * Calls the PC/SC API function SCardReleaseContext().
	 * 
	 * @param context
	 *            established resource manager context to be released.
	 * @throws PcscException
	 *             if SCardReleaseContext() does not return SCARD_S_SUCCESS.
	 */
	public static void releaseContext(long context) throws PcscException {
		try {
			ReleaseContext(context);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardReleaseContext().
	 * 
	 * @param context
	 *            established resource manager context to be released.
	 * @throws PcscException
	 *             if SCardReleaseContext() does not return SCARD_S_SUCCESS.
	 */
	private static native void ReleaseContext(long context) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardStatus().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param status
	 *            on output, status[0] contains the current cardState of the
	 *            smart card in the reader.
	 * @param protocol
	 *            on output, protocol[0] contains the current protocol.
	 * @return the ATR of the card.
	 * @throws PcscException
	 *             if SCardStatus() does not return SCARD_S_SUCCESS.
	 */
	public static byte[] status(long hCard, int[] status, int[] protocol) throws PcscException {
		try {
			return Status(hCard, status, protocol);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardStatus().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param status
	 *            on output, status[0] contains the current cardState of the
	 *            smart card in the reader.
	 * @param protocol
	 *            on output, protocol[0] contains the current protocol.
	 * @return the ATR of the card.
	 * @throws PcscException
	 *             if SCardStatus() does not return SCARD_S_SUCCESS.
	 */
	private static native byte[] Status(long hCard, int[] status, int[] protocol) throws PcscException;

	/**
	 * Calls the PC/SC API function SCardTransmit().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param protocol
	 *            the active protocol.
	 * @param command
	 *            command data to be sent to the card.
	 * @return the response data from the card.
	 * @throws PcscException
	 *             if SCardTransmit() does not return SCARD_S_SUCCESS.
	 */
	public static byte[] transmit(long hCard, int protocol, byte[] command) throws PcscException {
		if (command == null)
			throw new PcscException("illegal command parameter");
		try {
			return Transmit(hCard, protocol, command);
		} catch (PcscException e) {
			throw e;
		} catch (Exception e) {
			throw new PcscException(e);
		}
	}

	/**
	 * Native call of the PC/SC API function SCardTransmit().
	 * 
	 * @param hCard
	 *            reference value obtained from a previous call to connect.
	 * @param protocol
	 *            the active protocol.
	 * @param command
	 *            command data to be sent to the card.
	 * @return the response data from the card.
	 * @throws PcscException
	 *             if SCardTransmit() does not return SCARD_S_SUCCESS.
	 */
	private static native byte[] Transmit(long hCard, int protocol, byte[] command) throws PcscException;
}
