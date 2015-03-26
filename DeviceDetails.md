As commercial devices are available supporting the SmartCard API out of the box this section will list the details/problems regarding APDU communication with the supported Secure Elements.

<br />

### Sony Xperia S ###
| Model | LT26i |
|:------|:------|
| OS build | 6.0.A.3.75 |
| Baseband | M8660-AAABQOLYM-314005T |
| SmartCard API | 2.3.4 |
| Secure Elements | SIM: UICC |

2.3.4 seems to be an extended 2.3.2 with modifications from Sony. However, it turns out that the 2.3.4 service integrated does not have the _getSelectResponse()_ command exported although it was introduced in SCAPI-2.3.0.

Android applications that should support this device have to omit using the _getSelectResponse()_ command, otherwise the application would crash (e.g. PerformanceTester-1.3.0 from SEEK)

The device is very recommended for SEEK development as all APDU commands with any command length work without problems.

It is required to deploy a PKCS#15 Access Control file system on the SIM card, otherwise no APDU access is granted.
<br />

### Samsung Galaxy S3 ###
| Model | GT-I9300 |
|:------|:---------|
| OS build | IMM76D.I9300XXALEF |
| Baseband | I9300XXLEF |
| SmartCard API | 2.3.2 |
| Secure Elements | SIM: UICC |

All APDU communication is working fine except Case-4 APDUs with 256bytes response data. Updates correcting this problem are available but not public yet.

Android applications that should support this device have to omit using 256byte responses in Case-4 APDUs, otherwise the application would receive no data (e.g. PerformanceTester-1.3.0 from SEEK)

Although the device has a MicroSD slot, the MSC PluginTerminal won't work. It seems like the internal access control implementation is not handling multiple Secure Elements properly?

The SmartCard API AddonTerminal.java seems to be modified compared with the original SCAPI-2.3.2 as a PluginTerminal is only instantiated if its implementing a _isChannelCanBeEstablished()_ method?

The device is recommended for SEEK development as most APDU commands work without problems.

It is required to deploy a PKCS#15 Access Control file system on the SIM card, otherwise no APDU access is granted.
<br />

### Samsung Galaxy S2 NFC ###
| Model | GT-I9100P |
|:------|:----------|
| OS build | GINGERBREAD.XXLA3 |
| Baseband | I9100PXXKI3 |
| SmartCard API | 2.2.2 |
| Secure Elements | SIM, UICC |

All APDU communication is working fine except Case-4 APDUs with 256bytes response data. Updates correcting this problem are available but not public yet.

Android applications that should support his device have to omit using 256byte responses in Case-4 APDUs, otherwise the application would receive no data.

The device seems to provide two terminals, SIM and UICC although both are the same.

Although the device has a MicroSD slot, the MSC PluginTerminal won't work. After installing the addon terminal the service does not show the new terminal is the getReaders() list.

It is required to deploy a PKCS#15 Access Control file system on the SIM card, otherwise no APDU access is granted.
<br />