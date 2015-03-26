![http://seek-for-android.googlecode.com/svn/wiki/img/btpcsc_logo.png](http://seek-for-android.googlecode.com/svn/wiki/img/btpcsc_logo.png)

# Introduction #

There were some requests on the mailing list about using smartcard-enabled Android phones as smartcard readers. After phones are flashed with a kernel that supports smartcards, Micro SD smartcards can be accessed from Android using PC/SC. BTPCSC uses this and combines it with an Android service and a PC/SC reader library for Linux PCs (Windows support in development) to transfer smartcard communication over a Bluetooth connection.

The Linux library libbtpcsc.so implements an IFD handler and is simply added to pcscd's reader.conf, which causes pcscd to treat the Android phone like a normal smartcard reader, enabling any application that supports PC/SC to communicate with the Micro SD smartcard inside an Android phone.

On the Android side, BTPCSCServer is a service that is started at bootup and listens for incoming Bluetooth connections from a BTPCSC client. (the pcsc daemon) It will relay all incoming APDUs to the smartcard and send its response back.

So far, BTPCSC has been successfully tested with testpcsc, the HelloSmartcard applet and OpenSC in Firefox. (see [SmartCardPKI#2.4\_Use\_OpenSC\_on\_the\_phone](SmartCardPKI#2.4_Use_OpenSC_on_the_phone.md))


# Structure #

With a normal PC/SC device, the PC/SC daemon pcscd calls an IFD handler which does all communication with the device. BTPCSC acts as a custom IFD handler (libbtpcsc.so) which directs all communication through a Bluetooth connection to a BTPCSC server on an Android device, which in turn hands it to Android's PC/SC daemon.

<a href='http://seek-for-android.googlecode.com/svn/wiki/img/btpcsc_concept.png'><img src='http://seek-for-android.googlecode.com/svn/wiki/img/btpcsc_concept.png' width='600' height='729' /></a>


# Prerequisites #
  * A smartcard-enabled Android phone. For further information about this, see [BuildingTheSystem](BuildingTheSystem.md)
  * The PC/SD daemon pcscd, pcsc-lite and libbluetooth. They are all in the package sources on Ubuntu, so you can install them with sudo apt-get install pcscd libpcsclite1 libpcsclite-dev libbluetooth3 libbluetooth-dev, on other distributions you may have to download it manually.

# Installing BTPCSC #
  * Download the BTPCSC code either from the SVN repository (http://seek-for-android.googlecode.com/svn/trunk/applications/btpcsc) or as a Tarball <a href='http://seek-for-android.googlecode.com/files/btpcsc1.tar.gz'>here</a> and build it. (The Tarball already includes binaries for x86 Linux and Android 2.2, respectively)
  * x86 binaries are already included, you can, however, recompile by executing the compile script compile.sh
  * Move libbtpcsc.so from the libbtpcsc directory to your /usr/lib directory. This is the library that provides the IFD handler for PC/SC.
  * Create a config file for BTPCSC. There is a setup tool for this (btpcsc\_setup in the setup directory), but it is still in development and has only been tested on Ubuntu so far, therefore its use for anything other than scanning is not recommended. Instead, you should use the example config file (libbtpcsc/btpcsc.conf.example) to create your own config file. It consists of a list of device sections, each of which has the following entries:
    * A name (Which may consist of any displayable characters except quotation marks)
    * The bluetooth address of your android phone (if you don't know it, see [#btpcsc\_setup](#btpcsc_setup.md))
    * An ID (Any positive integer)
    * (optional) The name of the slot to be used. At the moment, the Android phones only support one slot, which is usually called "Mobile Security Card 00 00". If no slot is given, the default slot will be used.

The example config looks like this:
```
Section Device
  Name "My Phone"
  Address "12:34:56:78:9A:BC"
  ID "42"
  Slot "Mobile Security Card 00 00" # This is can be left out
EndSection
```

  * Let pcscd know to load the btpcsc library. First, you have to generate the necessary reader.conf entries. You can do this using btpcsc\_setup, which will generate a config file from the configuration you wrote into /etc/btpcsc.conf.
    * If you have Ubuntu, (or any other distribution that has a /etc/reader.conf.d directory and an update-readerconf.d tool) this is very simple. Just call "btpcsc\_setup -u -c /etc/reader.conf.d/libbtpcsc" and "update-reader.conf" (both as root) and you're ready to go.
    * If you want to make the changes permanent, you can generate a temporary reader.conf file by calling "btpcsc\_setup -u -c /tmp/reader.conf" and copying its contents to the end of your /etc/reader.conf
    * If you only want to test btpcsc once, you can generate a separate reader.conf by calling "btpcsc\_setup -u -c <any path and filename you want>" and then starting the pcsc daemon manually by calling "pcscd -c <the config file you generated before>" as root. You may have to kill pcscd first, ("killall pcscd", as root) and you may want to specify the options -f -a -d to see its debug output.

  * Install the BTPCSC server on your Android phone.
    * If you want it to run in the background, import BTPCSCServer\_Service into Eclipse, run it on your phone once and it will be started as a service on bootup. Instead, you can also install the apk from BTPCSCServer/bin. However, at the moment, it is probably easier not to use the service, as there still seem to be some minor problems with it.
    * If you want to start it manually and also see its output, import BTPCSCServer into Eclipse and run it from there. (or via the menu after you have installed it once) Instead, you can also install the apk from BTPCSCServer\_Service/bin.

  * Now you can just run the server, run the PC/SC daemon and you can use your Android phone as a regular PC/SC smartcard reader.

# btpcsc\_setup #

btpcsc\_setup is a configuration tool for BTPCSC. It is still in development it should be considered unstable. It supports the following options:
  * -l: Lists all configured BTPCSC devices
  * -u: Updates the reader.conf
  * -c: Specifies the path of the reader.conf (default: /etc/reader.conf.d/libbtpcsc)
  * -s: Scans the given address for slots (BTPCSC server must be running on it) or scans for BTPCSC devices in range if no address is given (only works if the device is discoverable and a BTPCSC server is running on it)
  * -addr: Sets the address for -s or -a
  * -a: Adds all slots of the device with the given address to the configuration. This should work, but has not been thoroughly tested, so adding the devices to /etc/btpcsc.conf manually is recommended.

A short example of how to use btpcsc\_setup and what output it gives:
```
manuel@icarus:~$ btpcsc_setup -s
Performing 10.24 s scan for BTPCSC devices...
SDP query on device 0.
SDP query on device 1.
 ID   Name                Address             Slot ID   Slot name
 0    Nexus_BTPCSC        90:21:55:1D:8F:E2   0         Mobile Security Card 00 00

manuel@icarus:~$ btpcsc_setup -s -addr 90:21:55:1d:8f:e2
 Name                Address             Slot ID   Slot name
 Nexus_BTPCSC        90:21:55:1d:8f:e2   0         Mobile Security Card 00 00

manuel@icarus:~$ sudo btpcsc_setup -a -addr 90:21:55:1d:8f:e2
Added reader Nexus_BTPCSC:Mobile Security Card 00 00
Wrote config file /etc/btpcsc.conf

manuel@icarus:~$ cat /etc/btpcsc.conf

# This file was automatically generated by btpcsc_setup. Please do not edit it by hand.

Section Device
  Name "Nexus_BTPCSC:Mobile Security Card 00 00"
  Address "90:21:55:1d:8f:e2"
  ID "1"
  Slot "Mobile Security Card 00 00"
EndSection


manuel@icarus:~$ btpcsc_setup -l
 Name                                      ID      Address             Slot
 Nexus_BTPCSC:Mobile Security Card 00 00   1       90:21:55:1d:8f:e2   Mobile Security Card 00 00


manuel@icarus:~$ btpcsc_setup -c test.conf -u
Wrote PCSC config file test.conf

manuel@icarus:~$ cat test.conf
# This file was automatically generated by btpcsc_setup. Please do not edit it by hand.

FRIENDLYNAME	"Nexus_BTPCSC:Mobile Security Card 00 00"
DEVICENAME	/dev/null
LIBPATH		/usr/lib/libbtpcsc.so
CHANNELID	1
```

# Testing BTPCSC #
So far, BTPCSC was tested on Ubuntu 9.10 and a Nexus One with smartcard-enabled Froyo with the following applications:
  * testpcsc, which is included in pcsc-lite (<a href='http://seek-for-android.googlecode.com/svn/wiki/img/btpcsc_testpcsc.png'>Screenshot</a>)
  * A JavaCard applet
  * OpenSC authentification in Firefox. This is described in detail here: [SmartCardPKI#2.4\_Use\_OpenSC\_on\_the\_phone](SmartCardPKI#2.4_Use_OpenSC_on_the_phone.md) However, do not execute the commands in an adb shell, but on a computer with a working BTPCSC installation. (<a href='http://seek-for-android.googlecode.com/svn/wiki/img/btpcsc_pkcsdump.png' title='Screenshot of a PKCS dump (contents of the SD card after the certificate was loaded on it)'>Screenshot 1</a>, <a href='http://seek-for-android.googlecode.com/svn/wiki/img/btpcsc_firefox.png' title='Firefox displaying the certificate from the smartcard after it has been requested by a SSL test site'>Screenshot 2</a>)

# Comments #
Please write any comments or testing results here.


# Links #
A similar project for Nokia phones: http://code.google.com/p/nfcbtpcsc/