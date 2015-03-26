## Bearer Independent Protocol (BIP) ##

## Introduction ##

Modern (U)SIM cards have been enhanced by many more functionalities than simple user authentication. One of those enhanced functionalities is the smart card web server (SCWS).
The SCWS delivers web pages using the HTTP protocol. These web pages (e.g. the phone book) can be shown within the mobile phone's web browser, and so offer an enhanced user experience to the mobile phone user.

These data are transported between browser and UICC via Bearer Independent Protocol.

Basically BIP consists of five proactive commands (OPEN CHANNEL, CLOSE CHANNEL, SEND DATA, RECEIVE DATA and GET CHANNEL STATUS) and two events (Data available and channel status).
It allows the UICC to exchange data to a remote Server in the Network or serve data to a local browser.

This BIP extension patch based on a proof of concept which enabled the SCWS for Android. With the aid of ST Ericsson the SCWS patch was re-designed and extended to the BIP extension.

## Features ##

  * Enables Android to support BIP with default (network) bearer and BIP in server mode.
  * Supports BIP client with TCP/UDP connection (local and remote).
  * Supports BIP in TCP Server mode.
  * Extends the STK framework with support of the STK commands SET\_POLL\_INTERVAL, SET\_UP\_EVENT\_LIST, OPEN\_CHANNEL, CLOSE\_CHANNEL, SEND\_DATA, RECEIVE\_DATA and GET\_CHANNEL\_STATUS.
  * Supports CHANNEL\_STATUS and DATA\_AVAILABLE Event with crosscheck to event list provided by SET\_UP\_EVENTLIST.
  * Uses data connections in PDP context.(PDP = Packet Data Protocol e.g. IP or X25 or PPP)


## Restrictions ##

  * Supports only IPv4 connections.
  * Only immediate link establishment is supported not background or on-demand.
  * Alpha identifier is currently only supported for OPEN\_CHANNEL, and not for CLOSE\_CHANNEL or SEND/RECEIVE\_DATA.
  * No support for Other Address (Local Address) TLV in OPEN\_CHANNEL, value will if present be ignored and no Local Address TLV will be included in the TERMINAL\_RESPONSE either.
  * Baseband has to support multiple PDP.
  * Runs currently only in Emulator, if you don't have a baseband which forwards the BIP STK commands.


## Patch ##
[BIP Extensions Patch](http://seek-for-android.googlecode.com/files/bip-extensions-0_9.tar.gz)

You need to extract the archive, then you will get three patches to be applied to the android sources.

  1. STK enabling patch. This patch is optional, it is necessary to enable STK support within android. You don't have to apply this patch if you already use the STK application in Android.

Apply patch with
```
patch -p1 -i bip-extensions-stk-0_9.patch
```

> 2. STK Application patch (mandatory). This patch extends the STK application packages with code for user interaction (e.g. confirming OPEN CHANNEL command.)

Apply patch with
```
patch -p1 -i bip-extensions-apps-0_9.patch
```

> 3. STK framework patch (mandatory). This patch contains almost all functionality of the BIP extension.

Apply patch with
```
patch -p1 -i bip-extensions-frameworks-0_9.patch
```


within the root directory of your android source.

## Environment ##
  * Built and developed on Android 2.3.5\_r1.
  * [Emulator extension patch](http://code.google.com/p/seek-for-android/wiki/EmulatorExtension) from SmartCardAPI 2.2.2.


## Using SCWS ##
The bip extension patch enables support for SCWS to the Android framework. Once the patches have been applied, web pages on an SCWS enabled SIM card can be accessed under the URL http://localhost:3516/.

![http://seek-for-android.googlecode.com/svn/wiki/img/SmartcardWebserverScreenshot.png](http://seek-for-android.googlecode.com/svn/wiki/img/SmartcardWebserverScreenshot.png)

## With Contribution of ##
  * Johan Hellman former ST Ericsson

  * Teddie Stenvi at ST Ericsson teddie.xx.stenvi@stericsson.com

## Support ##
For support please use the SEEK mailing list seek-for-android@googlegroups.com

## Related Specification ##

  * ETSI 102.223 Card Application Toolkit (CAT)