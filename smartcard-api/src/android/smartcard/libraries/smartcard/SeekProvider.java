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

package android.smartcard.libraries.smartcard;

import java.security.Provider;

/**
 * SEEK provider implementation.
 * The SEEK provider currently supports 'PC/SC' and 'Native' terminal factory types.
 * 
 * TODO SEEK card terminal factories and providers are not yet registered with VM. 
 */
public class SeekProvider extends Provider {

	private static final long serialVersionUID = 5013142283632324700L;

	/**
	 * Constructs a new SEEK provider instance.
	 */
	public SeekProvider() {
		super("SeekProvider", 0.1, "SEEK terminal provider");
		put("NativeTerminalFactory", "android.smartcard.libraries.smartcard.NativeTerminalFactory");
		put("PcscTerminalFactory", "android.smartcard.libraries.smartcard.PcscTerminalFactory");
	}
}
