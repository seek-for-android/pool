# Introduction #

Pcscdroid is an Android application that provides pcsc-lite support for other applications. Without needing root privileges one can read and write through pcsc to MSC-Cards. If the phone is rooted and the "su" command is available the application provides ccid-usb-reader support through pcsc-lite with libccid and libusb.

The Application uses a ported version of pcsc-lite for Android with a custom IFDHandler to support MSC-Cards and a ported version of the CCID-IFDHandler.


# Details #

Pcscdroid starts a ported version of pcsc-lite daemon in its userspace. The application comes with a custom IFDHandler to support MSC-Cards and a ported version of CCID. You will only be able to use the CCID interface if your phone is rooted. On unrooted phones Pcscdroid currently only works with MSC-Cards.
Pcscdroid provides access to pcsc-lite over an IPC-Interface located at:

```
$ /data/data/com.gieseckedevrient.android.pcscdroid/files/ipc/
```

The application starts a Android service that runs in the background to manage pcscd - also when the app is backgrounded.
Within the app you are able to control the daemon (start/stop) and see the log output of pcscd. Also the path of the MSC-Card and its ATR is provided. If an USB-Reader is connected and supported you will find the reader name as well as the card ATR. In the settings tab one can filter the logs for APDUs and/or DEBUG output.
All the files needed for execution and communication are located in the apps _files_ directory and its _lib_ directory (for JNI).
To support libusb and libccid and libpcsclite these libraries will be copied to the _/system/lib/_ folder.


We also provide a custom _libpcsclite.so_ and _libpcsclite.a_ for dynamicly and staticly linking the library. It provides the standard libpcsclite interface.
A sample of _testpcsc_ ist also provided to show you how to compile the test programm with the custom library provided.

The application was tested and works on Samsung Galaxy S3, Samsung Galaxy S2, Samsung Galaxy Nexus (No MSC; USB-Only), Motorola Xoom.


# Installation #

To install Pcscdroid on your device simply download the Package that includes the APK here: **[Pcscdroid](http://code.google.com/p/seek-for-android/downloads/detail?name=Pcscdroid-01.tar.gz)**

Next install it with

```
$ adb install Pcscdroid.apk
```

onto your device.
Now the app should be available in you application launcher. After launching the app press start to install and run the pcsc-lite daemon.
To stop the daemon simply press the same toggle button again.
In the log tab you are able to see the log output of pcscd.
The settings tab lets you filter the log ouput and clean the log. Here you can also remove the files installed by Pcscdroid.

To Remove the application stop the service and remove it in your program manager.

To test if pcscd is running and working execute the _testpcsc_ program delivered within the apk after starting the service by running following command from your phones console:

```
$ /data/data/com.gieseckedevrient.android.pcscdroid/files/testpcsc
```


# Source Code and Compiling #

First get the provided source code (in eclipse project) from our repository at _/trunk/applications/pcscdroid/_

Before importing the project into Eclipse make sure you have the Android SDK and NDK setup and installed into eclipse.
If Eclipse is setup correctly you should be able to import and build the project straight forward.
In the _jni_ Folder you will find the source and Android.mk files to compile and build libusb, libccid and libpcsclite.
If you are only interested in the binaries then you can build them from command line with the command _"ndk-build"_.
Then you will find your shared/dynmic libraries and executables in the _libs_ folder and the static libraries in the _obj_ folder.