# Introduction #

The SIM card is connected to the modem or baseband processor, but not to the application processor. The Android system runs on the application processor, thus has limited or no access to the SIM card. Communication with the SIM card is provided by the baseband processor over AT commands (e.g. AT+CPIN, AT+CRSM) defined by 3GPP 27.007 specification or through an proprietary IPC interface.<br />
As the (more and more common) IPC interface is not standardized we define the requested extensions in the _AT command language_ that needs to be mapped to proprietary function calls if AT commands are no longer supported.
<br /><br />


# Details #

In order to access card applications on the SIM card like any other Secure Element using the [SmartcardAPI](SmartcardAPI.md) the baseband processor must provide some additional AT commands to enable transparent APDU exchange. For security reasons the APDU exchange must be limited to logical channels between the Android world and the card applications. The basic channel of the UICC must be reserved to the baseband processor.

The [3GPP TS 27.007 Technical Specification](http://www.3gpp.org/ftp/Specs/html-info/27007.htm) describes all required AT commands.
  * **AT+CSIM (Generic SIM access)**
> ```
+CSIM=<length>,<command>
+CSIM: <length>,<response>```
> The AT+CSIM command transmits the _command_ APDU to the SIM card, and returns the _response_ APDU to the caller. This command allows exchange of any APDU, and should be handled with great care. The baseband must limit the range of _command_ APDUs to the essential needs.
> The blocks or filters:
    * SELECT by NAME commands
    * CHANNEL MANAGEMENT commands
    * CLA bytes that contain logical channel numbers (CLA = CLA & 0xFC)
> The channel management and applet selection functionality should be implemented by the following AT commands.

  * **AT+CCHO (Open Logical Channel)**
> ```
+CCHO=<dfname>
<sessionid>```
> The AT+CCHO commands opens a logical channel to the card application _dfname_, and returns a _sessionid_. The _sessionid_ is to be used when sending commands with the AT+CGLA command.<br />
> _dfname_ matches the applet AID that needs to be selected or can be _null_ if no applet needs to be selected on a new logical channel<br />
> _sessionid_ is from 1 to 4294967295 in case of success. The value 0 is reserved.

> In case of an error the baseband returns an error code.
> ```
+CME ERROR: <err>```
> The upper layers need to distinguish between certain error cases. For this reason the baseband shall return one of the following values for _err_.<br />
> _memory full_ in case no logical channel was available<br />
> _not found_ in case the card application was not found<br />
> _unknown_ in case of any other error

  * **AT+CCHC (Close Logical Channel)**
> ```
+CCHC=<sessionid>```
> This command asks the baseband processor to close a communication session with the UICC.

  * **AT+CGLA (Generic UICC Logical Channel Access)**
> ```
+CGLA=<sessionid>,<length>,<command>
+CGLA: <length>,<response>```
> The AT+CGLA command transmits the _command_ APDU to the selected card application, and returns the _response_ APDU to the caller.

The _uicc.patch_ adds support for SIM card access to the telephony framework. The additional telephony framework methods shall be available for the [SmartCard API](SmartcardAPI.md) exclusively thus no malicious application can use the telephony extensions directly.

Once the baseband processor supports the required AT commands, and the UICC patch has been applied, the [SmartCard API](SmartcardAPI.md) supports access to the SIM card with mentioned limitations. The RIL implementation on current Android phones do not support the required AT commands. For this reason the UICC patch includes an [emulator extension](EmulatorExtension.md) (emulator.patch), which adds support for the required AT commands to the emulator's RIL implementation (reference-ril).<br />
Refer to [RIL\_Extensions.tgz](http://code.google.com/p/seek-for-android/downloads/detail?name=RIL_Extensions.tgz) for more information.
<br /><br />


# Error handling #
Multiple error conditions can arise where the system needs to be able to handle them. For this case the SmartCard API defines in addition to the proposed changes a `getLastError()` function to determine the error condition.
| **getLastError** | **definition** |
|:-----------------|:---------------|
| 0 | no error |
| 1 | general error (communication error, no further diagnosis) |
| 2 | no resources (no logical channel available on SE)  |
| 3 | not found (applet selection failed) |
| 4 | close failed (MANAGE CHANNEL CLOSE error) |
| 5 | sessionid mismatch (invalid session id provided) |

Mapping to the corresponding AT commands:
| **getLastError** | **CSIM** | **CCHO** | **CGLA** | **CCHC** | **interpretation** |
|:-----------------|:---------|:---------|:---------|:---------|:-------------------|
| 0 | + | + | + | + | no error occurred while processing the command |
| 1 | + | + | + | + | an undefined error occurred, e.g. communication error |
| 2 |   | + |   |   | card does not support logical channels or no logical channel available |
| 3 |   | + |   |   | applet selection failed with 6A 82 (file/application not found) |
| 4 |   |   |   | + | an error occurred while closing the logical channel |
| 5 |   |   | + | + | session ID mismatch or tampered |

Transmit functions like +CSIM or +CGLA always return SW1SW2 if available or empty data when no SW1SW2 was received and indicate **1** in `getLastError`