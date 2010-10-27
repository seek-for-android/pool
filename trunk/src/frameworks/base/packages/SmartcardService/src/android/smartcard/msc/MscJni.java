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

package android.smartcard.msc;

/**
 * MSC library JNI wrapper for card terminals.
 */
public final class MscJni {
	
	/** Constant pool for MSC open mode constants. */
	public static abstract class OpenMode {
		/**
		 * This application is willing to share this device with other
		 * applications.
		 */
		public static final int Shared = 1;

		/**
		 * This application is not willing to share this device with other
		 * applications.
		 */
		public static final int Exclusive = 3;

		/**
		 * This application has opened the device in shared mode and requests
		 * temporary exclusive access.
		 */
		public static final int BeginExclusive = 2;
	}

	/**
	 * MCEX library load exception; <code>null</code> if the library was loaded
	 * successfully.
	 */
	private static Throwable loadException;

	static {
		try {
			Runtime.getRuntime().loadLibrary("msc");
		} catch (Throwable t) {
			loadException = t;
		}
	}

	/**
	 * Closes the device file.
	 * @param fd
	 *         the file descriptor assigned by the <code>open</code> method.
	 * @throws MscException
	 *          if the close operation failed.
	 */
	public static void close(int fd) throws MscException {
		try {
			Close(fd);
		} catch (MscException e) {
			throw e;
		} catch (Exception e) {
			throw new MscException(e);
		}
	}

	private static native void Close(int fd) throws MscException;

	/**
	 * Returns the MSC library load exception or <code>null</code> if the
	 * library was loaded successfully.
	 * 
	 * @return the MSC library load exception or <code>null</code> if the
	 *         library was loaded successfully.
	 */
	public static Throwable getLoadError() {
		return loadException;
	}

	/**
	 * Returns <code>true</code> if the MSC native library was loaded
	 * successfully or <code>false</code> if the MCEX JNI is not available.
	 * 
	 * @return <code>true</code> if the MSC native library was loaded
	 *         successfully or <code>false</code> if the MCEX JNI is not
	 *         available.
	 */
	public static boolean isLoaded() {
		return (loadException == null);
	}

	/**
	 * Opens the MSC device file in the specified mode.
	 * Open mode constants are defined in the inner <code>OpenMode</code> class.
	 * @param mode
	 *          open mode.
	 * @return the file descriptor of the device file.
	 *          In shared mode 0 will be returned.
	 * @throws MscException
	 *          if the open operation failed.
	 */
	public static int open(int mode) throws MscException {
		while (true) {
			try {
				return Open(mode);
			} catch (MscBusyException e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
				}
			} catch (MscException e) {
				throw e;
			} catch (Exception e) {
				throw new MscException(e);
			}
		}
	}

	private static native int Open(int mode) throws MscException;

	/**
	 * Transmits the specified command and returns the response.
	 * @param fd
	 *         the file descriptor assigned by the <code>open</code> method.
	 * @param command
	 *         the command to be written to the device file.
	 * @return the response read from the device file.
	 * @throws MscException
	 *         if the transmit operation failed.
	 */
	public static byte[] transmit(int fd, byte[] command) throws MscException {
		if (command == null)
			throw new MscException("illegal command parameter");

		while (true) {
			try {
				return Transmit(fd, command);
			} catch (MscBusyException e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
				}
			} catch (MscException e) {
				throw e;
			} catch (Exception e) {
				throw new MscException(e);
			}
		}
	}

	private static native byte[] Transmit(int fd, byte[] command) throws MscException;

	private MscJni() {
	}
}
