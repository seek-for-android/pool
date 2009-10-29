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
 * MCEX library JNI wrapper for card terminals.
 */
public final class McexJni {
	
	/** Constant pool for MCEX open mode constants. */
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
			Runtime.getRuntime().loadLibrary("mcex");
		} catch (Throwable t) {
			loadException = t;
		}
	}

	/**
	 * Closes the device file.
	 * @param fd
	 *         the file descriptor assigned by the <code>open</code> method.
	 * @throws McexException
	 *          if the close operation failed.
	 */
	public static void close(int fd) throws McexException {
		try {
			Close(fd);
		} catch (McexException e) {
			throw e;
		} catch (Exception e) {
			throw new McexException(e);
		}
	}

	private static native void Close(int fd) throws McexException;

	/**
	 * Returns the MCEX library load exception or <code>null</code> if the
	 * library was loaded successfully.
	 * 
	 * @return the MCEX library load exception or <code>null</code> if the
	 *         library was loaded successfully.
	 */
	public static Throwable getLoadError() {
		return loadException;
	}

	/**
	 * Returns <code>true</code> if the MCEX native library was loaded
	 * successfully or <code>false</code> if the MCEX JNI is not available.
	 * 
	 * @return <code>true</code> if the MCEX native library was loaded
	 *         successfully or <code>false</code> if the MCEX JNI is not
	 *         available.
	 */
	public static boolean isLoaded() {
		return (loadException == null);
	}

	/**
	 * Opens the MCEX device file in the specified mode.
	 * Open mode constants are defined in the inner <code>OpenMode</code> class.
	 * @param mode
	 *          open mode.
	 * @return the file descriptor of the device file.
	 *          In shared mode 0 will be returned.
	 * @throws McexException
	 *          if the open operation failed.
	 */
	public static int open(int mode) throws McexException {
		while (true) {
			try {
				return Open(mode);
			} catch (McexBusyException e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
				}
			} catch (McexException e) {
				throw e;
			} catch (Exception e) {
				throw new McexException(e);
			}
		}
	}

	private static native int Open(int mode) throws McexException;

	/**
	 * Returns the status of the device file.
	 * @return 0 if a device is present, -1 a device is absent.
	 * @throws McexException
	 *        if the status operation failed.
	 */
	public static int stat() throws McexException {
		try {
			return Stat();
		} catch (McexException e) {
			throw e;
		} catch (Exception e) {
			throw new McexException(e);
		}
	}

	private static native int Stat() throws McexException;

	/**
	 * Transmits the specified command and returns the response.
	 * @param fd
	 *         the file descriptor assigned by the <code>open</code> method.
	 * @param command
	 *         the command to be written to the device file.
	 * @return the response read from the device file.
	 * @throws McexException
	 *         if the transmit operation failed.
	 */
	public static byte[] transmit(int fd, byte[] command) throws McexException {
		if (command == null)
			throw new McexException("illegal command parameter");

		while (true) {
			try {
				return Transmit(fd, command);
			} catch (McexBusyException e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
				}
			} catch (McexException e) {
				throw e;
			} catch (Exception e) {
				throw new McexException(e);
			}
		}
	}

	private static native byte[] Transmit(int fd, byte[] command) throws McexException;

	private McexJni() {
	}
}
