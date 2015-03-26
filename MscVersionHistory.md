#### v3.1.0 - 13.12.2013 ####
  * Version string 3.1 - Version code 6
  * Maintenance release for JB 4.3 & 4.2
  * Reference device is GT-I9505 with Android 4.3
  * code merge with SmartCardAPI 3.1.0 including
    * SIMalliance Open Mobile API 2.04 compliant
    * Global Platform Secure Element Access Control compliant
  * Removed RECEIVE\_BOOT\_COMPLETED event to disable automatic startup
  * fixed dead lock in MSC Keep-Alive thread

#### v3.0 - 31.10.2012 ####
  * tested on JB
  * packages are renamed from org.simalliance.`*` to com.mobilesecuritycard.`*` (see notes.txt)
  * native library renamed to libmobilesecuritycard.so (see notes.txt)
  * Activity added that shows the current state of the Mobile Security Card

#### v2.4.0.1 - 11.09.2012 ####
  * ACE will be disabled if ARA can not be selected by the SELECT command (according to GP spec)
  * Improvement of the mechanism to detect the mount point of the Mobile Security Card
    * first we use a list of known SD card paths
    * if the SD card path is not in this list, we search in the file /proc/mounts for the correct path to the SD card
    * as a last option, we execute the 'mount' command and parse the result for the path

#### v2.4.0 - 19.07.2012 ####
  * SmartCard API 2.4.0 feature compliant (Global Platform Access Control integrated)
  * ICS support
  * MscTerminal & libmsc.so extensions by G&D SFS
    * better mount point detection
    * APDU buffer size limited to protect potential buffer overflow
    * Keep-alive thread integrated
    * removal of communication file after session gets closed

#### v2.3.0 - 06.12.2011 ####
  * Compliant to the SIMalliance Open Mobile API Specification V2.02 (transport layer only)
  * The namespace "android.smartcard" was completely removed
  * Access Control was completely removed

#### v2.2.2 - 21.10.2011 ####
  * Compliant to the latest SIMalliance Open Mobile API Specification (V1.01)
  * Support for PluginTerminals
  * Speed enhancement with file access
  * Improved recognition of SD card mount point
  * Changed permission name according to Open Mobile API

#### v2.1.1 - 15.04.2011 ####
  * Major update: integration of SIMalliance Open Mobile API interface
  * Bug fix: improved retry-loop to receive response with some MSC cards

#### v1.5 - 11.11.2010 ####
  * Bug fix: 'response too small' with some MSC cards
  * Bug fix: JNI library cleanup
  * Corrections for Samsung Galaxy S

#### v1.4 - 02.11.2010 ####
  * Aligned _ISmartcardService_ interface to SmartCard API
  * Detection of MSC card versus standard SD card
  * Detection of mount path
  * APDU command and response size fix (for APDUs with more than 250 data bytes)
  * simplified JNI interface
  * Sample application & android.smartcard.jar in download file

#### v1.0 - v1.3 ####
  * no public release of the first implementations