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

package android.smartcard;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TerminalFactory {

	private static final class NoneCardTerminals extends CardTerminals {

		static final NoneCardTerminals STATIC_INSTANCE = new NoneCardTerminals();

		private NoneCardTerminals() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<CardTerminal> list(State state) throws CardException {
			return Collections.EMPTY_LIST;
		}

		@Override
		public boolean waitForChange(long timeout) throws CardException {
			throw new IllegalStateException("no card terminals");
		}
	}

	private static final class NoneProvider extends Provider {
		private static final long serialVersionUID = -6126436159055513301L;

		private NoneProvider() {
			super("NoneProvider", 0.9, "None terminal provider");
		}
	}

	private static final class NoneTerminalFactory extends TerminalFactorySpi {
		@Override
		protected CardTerminals engineTerminals() {
			return NoneCardTerminals.STATIC_INSTANCE;
		}
	}

	public static final String PCSC_TYPE = "PC/SC";
	public static final String NATIVE_TYPE = "Native";
	public static final String NONE_TYPE = "None";

	private static final Map<String, TerminalFactory> factories = new HashMap<String, TerminalFactory>();

	private static final String DEFAULT_TYPE;

	static {
		factories.put(NONE_TYPE,
				new TerminalFactory(new NoneTerminalFactory(), new NoneProvider(), NONE_TYPE));
		String type = NONE_TYPE;

		try {
			TerminalFactory nativeFactory = createFactory(NATIVE_TYPE,
					"android.smartcard.libraries.smartcard.NativeTerminalFactory",
					"android.smartcard.libraries.smartcard.SeekProvider");
			factories.put(NATIVE_TYPE, nativeFactory);
			type = NATIVE_TYPE;
		} catch (Exception ignore) {
		}

		try {
			TerminalFactory pcscFactory = createFactory(PCSC_TYPE,
					"android.smartcard.libraries.smartcard.PcscTerminalFactory",
					"android.smartcard.libraries.smartcard.SeekProvider");
			factories.put(PCSC_TYPE, pcscFactory);
			type = PCSC_TYPE;
		} catch (Exception ignore) {
		}

		DEFAULT_TYPE = type;
	}

	private static TerminalFactory createFactory(String type, String spiClassName, String providerClassName)
			throws NoSuchAlgorithmException {

		try {
			Class<?> clazz = Class.forName(spiClassName);
			TerminalFactorySpi spi = (TerminalFactorySpi) clazz.newInstance();
			clazz = Class.forName(providerClassName);
			Provider provider = (Provider) clazz.newInstance();
			return new TerminalFactory(spi, provider, type);
		} catch (Exception e) {
			throw new NoSuchAlgorithmException(e);
		}
	}

	public static TerminalFactory getDefault() {
		try {
			return getInstance(DEFAULT_TYPE, null);
		} catch (NoSuchAlgorithmException e) {
			// should not happen with defaultType
		}
		return null;
	}

	public static String getDefaultType() {
		return DEFAULT_TYPE;
	}

	public static TerminalFactory getInstance(String type, Object params) throws NoSuchAlgorithmException {
		if (type == null) {
			throw new NullPointerException("type cannot be null");
		}
		// TODO factories and providers are not yet registered with the VM.
		// In the meantime we use a static map.
		TerminalFactory factory = factories.get(type);
		if (factory == null) {
			throw new NoSuchAlgorithmException("type: " + type + " not supported");
		}
		return factory;
	}

	public static TerminalFactory getInstance(String type, Object params, Provider provider) {
		throw new UnsupportedOperationException("work in progress");
	}

	public static TerminalFactory getInstance(String type, Object params, String provider) {
		throw new UnsupportedOperationException("work in progress");
	}

	private final TerminalFactorySpi spi;

	private final Provider provider;

	private final String type;

	private TerminalFactory(TerminalFactorySpi spi, Provider provider, String type) {
		this.spi = spi;
		this.provider = provider;
		this.type = type;
	}

	public Provider getProvider() {
		return provider;
	}

	public String getType() {
		return type;
	}

	public CardTerminals terminals() {
		return spi.engineTerminals();
	}

	public String toString() {
		return "TerminalFactory:" + spi + ", type:" + type + ", provider:" + provider;
	}

}
