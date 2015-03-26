# Introduction #

This project was developed as part of a bachelor thesis at the DHBW Ravensburg Campus Friedrichshafen in cooperation with Giesecke and Devrient.<br />

The SecureFileManager is a filemanager for the android platform, which is making use of the G&D secure microSD card, in order to cipher files.<br />
To spare the user to keep numerous complicated passwords, which are being used for ciphering, in mind, a secure key is being generated and saved, by the use of the Java Card Applet, which is running on the Mobile Security Card.<br />


# Android Application #

**The Android Application is for demonstration and test purposes only. Do not use in production environments!**<br />

To use this application, you need a Android platform with smart card access.<br />
For this purpose look [BuildingTheSystem](http://code.google.com/p/seek-for-android/wiki/BuildingTheSystem) or the introduction on the [Mobile Security Developer's Kit](https://www.cardsolutions-shop.com/shop/gi-de/).<br />

Checkout the [SecureFileManger source code](http://code.google.com/p/seek-for-android/source/browse/#svn/trunk/applications/SecureFileManager/SecurityFileManager) and install your own generated .apk file on your Android device.<br />
Run the SecureFileManager application on your Android smartphone.
<br /><br />
With inserted Mobile Security Card the encryption functions are available.<br />

<table>
<tr>
<td><img src='http://seek-for-android.googlecode.com/svn/wiki/img/secureFileManager_encrypt_menu.png' /></td>
<td></td>
<td><img src='http://seek-for-android.googlecode.com/svn/wiki/img/secureFileManager_encrypt_file.png' /></td>
</tr>
</table>
<br /><br />
# Java Card Applet #

**The Java Card applet is for demonstration and test purposes only. Do not use in production environments!**<br />

Download the filemanager.cap file from the [Download page](http://code.google.com/p/seek-for-android/source/browse/#svn/trunk/applications/SecureFileManager/JavaCardApplet/com/gieseckedevrient/javacard/filemanager/javacard).<br />
Install the filemanager.cap file on the Mobile Security Card. Therfore you can use JLoad or other Java Card compliant Global Platform loader tools.<br />
JLoad is included in the [Mobile Security Developer's Kit](https://www.cardsolutions-shop.com/shop/gi-de/).<br />
You can even compile and convert the [fileManager.java](http://code.google.com/p/seek-for-android/source/browse/#svn/trunk/applications/SecureFileManager/JavaCardApplet/com/gieseckedevrient/javacard/filemanager%3Fstate%3Dclosed) file to a Java Card Cap file. For this reason please check the [source code page](http://code.google.com/p/seek-for-android/source/browse/#svn/trunk/applications/SecureFileManager/JavaCardApplet).<br /><br />

With the help of the Java Card Applet a secure key is being established for a specific file-ID and a given password.
This triple is saved on the secure element of the Mobile Security Card.<br /><br />

![http://seek-for-android.googlecode.com/svn/wiki/img/secureFileManager_triple.png](http://seek-for-android.googlecode.com/svn/wiki/img/secureFileManager_triple.png)

Only if you are authenticated you have access to all methods. The default Pin is 0x31 0x32 0x33 0x34.<br />
After 3 times wrong pin entry the applet is blocked. A correct Pin resets the counter.

## APDU Interface ##
### Verify Pin ###
```
CLA:  90
INS:  10
P1:   00
P2:   00
P3:   04
data: <4 bytes pin>

APDU: 90 10 00 00 04 31 32 33 34
RESPONSE: 90 00
```

### Create Key ###
```
CLA:  90
INS:  20
P1:   00
P2:   00
P3:   <length of data>
data: <length of file-ID><file-ID><length of pwd><pwd>

APDU: 90 20 00 00 10 04 2F 02 38 3B 0A 31 32 33 34 35 36 37 38 39 40
RESPONSE: <key> 90 00
```

### Verify Key ###
```
CLA:  90
INS:  30
P1:   00
P2:   00
P3:   <length of data>
data: <length of file-ID><file-ID><length of pwd><pwd>

APDU: 90 30 00 00 10 04 2F 02 38 3B 0A 31 32 33 34 35 36 37 38 39 40
RESPONSE: <key> 90 00
```

### Delete Key ###
```
CLA:  90
INS:  40
P1:   00
P2:   00
P3:   <length of data>
data: <length of file-ID><file-ID><length of pwd><pwd>

APDU: 90 30 00 00 10 04 2F 02 38 3B 0A 31 32 33 34 35 36 37 38 39 40
RESPONSE: 90 00
```