# Introduction #

Modern SIM cards have been enhanced by many more functionalities than simple user authentication. One of those enhanced functionalities is the smart card web server (SCWS).<br />
The SCWS delivers web pages using the HTTP protocol. These web pages (e.g. the phone book) can be shown within the mobile phone's web browser, and so offer an enhanced user experience to the mobile phone user.


# Details #

The [smart card web server patch](http://seek-for-android.googlecode.com/files/smartcard-webserver-1_1.tgz) adds support for SCWS to the Android framework. Once the patches have been applied, web pages on an SCWS enabled SIM card can be accessed under the URL **_http://localhost:3516_**.<br />
Additionally, the radio interface layer (RIL) must support the bearer independent protocol (BIP). On current Android phones, the RIL does not support BIP.<br />
For this reason the smart card web server patch includes an emulator extension (emulator.patch), which adds BIP support to the emulator's RIL (reference-ril). See EmulatorExtension for how to build the emulator with extensions.

![http://seek-for-android.googlecode.com/svn/wiki/img/SmartcardWebserverScreenshot.png](http://seek-for-android.googlecode.com/svn/wiki/img/SmartcardWebserverScreenshot.png)