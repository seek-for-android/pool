# SmartCard API #

| Version | Date | Code | Android | OMAPI |
|:--------|:-----|:-----|:--------|:------|
| 3.2.1 | 15.07.2014 | 6 | 4.4.4 | 2.05 |
| 3.1.0 | 27.09.2013 | 5 | 4.3 | 2.04 |
| 3.0.0 | 07.04.2013 | 4 | 4.2.2 | 2.03 |
| 2.4.0 | 05.07.2012 | 3 | 4.0.3 | 2.03 |
| 2.3.2 | 06.02.2012 | 2 | 4.0.3 | 2.02 |
| 2.3.1-rc1 | 20.01.2012 | 0 | 4.0.3 | 2.02 |
| 2.3.0 | 05.12.2011 | 1 | 2.3.7 | 2.02 |
| 2.2.2 | 23.08.2011 | - | 2.3.5 | 1.01 |
| 2.2.1 | 15.07.2011 | - | 2.3.4 | 1.01 |
| 2.2.0 | 01.07.2011 | - | 2.3.4 | 1.01 |
| 2.1.2 | 21.06.2011 | - | 2.3 |  |
| 2.1.1 | 15.04.2011 | - | 2.3 |  |
| 2.0 | 28.02.2011 | - | 2.3 |  |
| 1.2 | 12.11.2010 | - | 2.2.1 |  |
| 1.1 | 25.10.2010 | - | 2.2 |  |
| 1.0 | 09.07.2010 | - | 2.2 |  |
| 0.1 | 18.01.2010 | - | 2.1 |  |

<br />

### v3.2.1 ###
  * Based on Android 4.4.4 (KitKat)
  * Adaptation to Open Mobile API v2.05
  * Adaptation to Open Mobile Test Spect 1.0 + Errata Document
  * Changelog:
    * Fix getSelectResponse()
    * Fix selectNext()
    * Implement openBasicChannel()
    * Implement openLogicalChannel(null)
    * Accept warning SW in openBasicChannel() and openLogicalChannel()
    * Fix getAtr()
    * Support T=1 protocol
    * Other bug fixes

### v3.1.0 ###
  * JB (Android-4.3) reference
  * SIMalliance Open Mobile API 2.04 Transport Layer (selectNext support)
  * Issues.57/59/63/64 resolved
  * Enhancements/Improvements:
    * default NFC access now compliant with GP SEAC spec
    * SCservice unregisters all broadcast receivers
    * reset of ACE when new UICC is inserted
    * implicit path replaced with absolute path in SIM\_IO
    * unmasking of channel number in CLA byte in UiccTerminal
    * ignore MissingRessourceException in case SE is a UICC
    * fallback to ARF each time the ARA selection fails
    * cleanup duplicate AIDL files in build environment
    * added dump and debug functionalties in debug build
    * initialization of AC rules in a background handler

### v3.0.0 ###
  * GlobalPlatform ARF support
  * JB (Android-4.2.2) reference
  * AIDL interface to service reworked
  * Access Control performance:
    * service starts at boot time and caches all rules (get all rules)
    * refresh tag evaluation in session instead of channel
  * Issues.33/34/35/38/41/46/50 resolved

### v2.4.0 ###
  * GlobalPlatform ARA support
  * ACA removed
  * ICS (Android-4.0.3) reference
  * compatible with SCAPI-2.3.2
  * Issues.5/6/7/11/13/16/20/21/22/23/26 resolved


### v2.3.2 ###
  * ICS support (Android-4.0.3)
  * compatible with SCAPI-2.3.0
  * all open issues from 2.3.1-rc1 resolved


### v2.3.1-rc1 ###
  * Pre-release
  * ICS support (Android-4.0.3)
  * compatible with SCAPI-2.3.0


### v2.3.0 ###
  * Compliant to the latest [SIMalliance](http://simalliance.org/) Open Mobile API Specification V2.02 (transport layer only)
  * SmartCard API is only based on the Open Mobile API SIMalliance
  * The namespace "android.smartcard" was completely removed
  * SmartCard API is only provided as shared library.


### v2.2.2 ###
  * SmartCard API is only provided as shared library.
  * SmartCard API is only based on the Open Mobile API [SIMalliance ](http://simalliance.org/)
  * The SmartCard API can be extended with the old API (V 1.X) with the backward compatibilty add on patch.
  * The permission for using the SmartCardAPI was changed from "android.permission.SMARTCARD" to "org.simalliance.openmobileapi.SMARTCARD".


### v2.2.1 ###
  * Fully compliant to the latest [SIMalliance ](http://simalliance.org/)Open Mobile API Specification (V1.01)
  * New SE Access Control Enforcer can restrict the access to Secure Elements by predefined policies. See the SE Access Control description.
  * CTS extension for SmartCard API. (Some corrections were made to enable CTS also for the shared library based SmartCard API)
  * Support of [ASSD](http://www.sdcard.org/developers/tech/ASSD/) to access SD memory cards with an embedded security system  (Note: The also provided ASSD kernel patch for creating the ASSD kernel driver has to be used for enabling the ASSD support)
  * The ASSD solution replaces the interim proprietary solution "MSC" for accessing a Secure SD cards.
  * SmartCard API is also provided as a shared library. A new alternative (but experimental) solution.


### v2.2.0 ###
  * Fully compliant to SIMalliance Open Mobile API Specification V1.01.
  * New SE Access Control Enforcer can restrict the access to Secure Elements by predefined policies. See the SE Access Control description.
  * CTS extension for SmartCard API. (as provided in v2.1.2)
  * Patch for ASSD (Advanced Security SD) was removed again as the needed ASSD kernel driver is not available yet (but will coming soon).
  * SmartCard API is also provided as a shared library. A new alternative (but experimental) solution (as provided in v2.1.2)
  * UICC and Emulator patch files are now compliant to Android 2.3.4r1 (UICC/Emulator patch files in v2.1.2 cause a version mismatch with Android 2.3.4r1)

### v2.1.2 ###
  * Fully compliant to SIMalliance Open Mobile API Specification V1.01.
  * New SE Access Control unit can restrict the access to Secure Elements by predefined policies. See the SE Access Control description.
  * CTS test suite for SmartCard API.
  * Support of ASSD (Advanced Security SD).
  * API is now also provided as a shared library.

### v2.1.1 ###
  * fully SIM Alliance Open Mobile API compliant
  * added Terminal provider interface
  * minor bugfixes and extensions in the terminal implementation:
    * MscTerminal is connected at later time if initial connect failed
    * SmartMxTerminal was adapted to the latest interface
    * UiccTerminal was improved in terms of error handling

### v2.0 ###
  * Gingerbread support
  * API redesign to provide a better abstraction of the SE sevices

### v1.2 ###
  * MscTerminal redesign
  * Msc native JNI: better APDU waiting loop
  * dynamic card terminal instantiation:
    * add or remove supported XvyTerminal.java implementations by adding or removing files
    * no further source code adaptions required
  * internal cleanup: moved all terminal implementation into terminal namespace
  * bugfix: APDU size of 255 bytes with MscTerminal

### v1.1 ###
  * getAtr() removed from ISmartcardService
  * major internal redesign
    * PC/SC transport system replaced by Terminal implementations on the Java layer
    * PC/SC moved to a separate package that accesses the SmartCard API with a single IFD Handler
  * UICC handler added with reference implementation for OEMs and emulator support
    * AT commands AT+CCHO, AT+CGLA, AT+CCHC and AT+CSIM supported
  * new sample application _ApduTester_ available

### v1.0 ###
  * SmartCardService as the final architecture for a smart card library for Android
  * minor bugfixes
    * mapping of logical channels to CLA bytes
    * compilation issues
  * renaming of seek service to SmartcardService
  * shutdown() function added
  * integrated in Android build system
  * Channel management not ISO conform to allow non ISO CLA bytes to be used for channel management

### v0.1 ###
  * initial **alpha** release
  * first release of a _Service-based_ approach for the main instance of the library instead of plain framework extensions