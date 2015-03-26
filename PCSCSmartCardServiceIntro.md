# Introduction #

To provide the PCSC Interface on the Android device, additional to the SmartCard API, we implemented a smartcard reader driver (ifdhandler) for PCSC-Lite, which routes the APDU communication through a SmartCard system service to the SmartCard API. PCSC Support for the SmartCard API is an optional component which is typically not required, as the SmartCard API already allow to communicate with any Secure Element.
But the PCSC-Interface is the standard interface used for many PC applications accessing smartcards. So this implementation will provide the PCSC interface for already existing native applications (e.g. OpenSC), which are ported to Android and requiring the PCSC interface.
Whenever possible, it is recommended to directly use the SmartCard API.

## Apply the patches ##

The patch for PC/SC support on the android device is only required for (native) Android applications relying on the PC/SC interface.

```
$ cd <ANDROID_SRC_ROOT_DIR>
$ patch -p1 < <path_to_my_patches>/pcsc_systemservice_v098.patch
```

Apart from adding the PC/SC support, building the Android platform is according to the description in [Building the System](http://code.google.com/p/seek-for-android/wiki/BuildingTheSystem)

# Details #

The current version of the ifdhandler is a prototype, which allow to route the APDU communication from the PCSC interface to the SmartCard API.

![http://seek-for-android.googlecode.com/svn/wiki/img/struktur_pcsc_for_smartcard_api.jpg](http://seek-for-android.googlecode.com/svn/wiki/img/struktur_pcsc_for_smartcard_api.jpg)

## Version history ##

pcsc-system-service-0\_9\_8.tgz

  * prototype
  * patch updated for SmartCard API 2.3.0 and Android 2.3.7r1
  * updated reader names in reader.conf corresponding to reader names in SmartCard API 2.3.0


pcsc\_system\_service\_v097.tgz

  * prototype
  * patch updated for SmartCard API 2.2.2 and Android 2.3.5r1
  * no change in functionality compared to previous version


pcsc\_system\_service\_v096.tgz

  * prototype
  * patch updated for SmartCard API 2.2.1 and Android 2.3.4r1
  * no change in functionality compared to previous version

pcsc\_system\_service\_v095a.tgz

  * prototype
  * ifdhandler included in source code.
  * no change in functionality compared to previous version

pcsc\_system\_service\_v095.tgz

  * prototype
  * minor adaptions for support of SmartCard API 2.1.1
  * minor corrections in errorhandling and logging
  * tested with Mobile Security Card on Nexus One with Android 2.3.3 [r1](https://code.google.com/p/seek-for-android/source/detail?r=1) (gingerbread)

pcsc\_system\_service\_v094.tgz

  * prototype
  * Support of SmartCard API 2.0
  * Support of basic and logical channels of the SmartCard API
  * Patch for Android 2.3.3 (gingerbread)
  * tested with Mobile Security Card on Nexus One with Android 2.3.3 (gingerbread)


pcsc\_system\_Service\_v093.tgz

  * first prototype
  * only basic channel of the SmartCard API used
