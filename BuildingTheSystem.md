# Introduction #

This document describes how to build the system from scratch, apply the smart card related patches and flash a development device with the new images.<br />It is essential that the device supports the necessary RIL and baseband modifications to allow APDU access to the SIM card, otherwise SIM access will not work!<br />


## Getting the Android sources ##

A detailed description how to set up the local work environment can be found  [here](http://source.android.com/source/building.html).
```
$ mkdir seek-for-android && cd seek-for-android
$ repo init -u https://android.googlesource.com/platform/manifest -b android-4.4.4_r1
$ repo sync
```
After a successful sync it is recommended to build the Android platform once before applying the SEEK patch files to ensure the source code compiles properly.<br />

## Patching the sources ##
Apply the necessary patches in the root directory of the Android source tree to enable SmartCard API support.<br />
Please make sure which patch files should be applied as the different use cases (SmartCard API, SCWS/BIP, PC/SC support) are separated in different download packages. Do not apply patches multiple times!

#### SmartCard API support ####
Download the [smartcard-api-3\_2\_1.tgz](https://drive.google.com/file/d/0B63jMJOYc2l3UXJFWVdaQUlyeFk/edit?usp=sharing) patch and extract the content:
  * `smartcard-api.patch` - patch the Java sources for SmartCard API support - always required
  * `uicc.patch` - patch the Android Telephony framework with the required UICC methods and for the SmartCard API UiccTerminal support within the emulator - for emulator builds only
  * `emulator.patch` - patch the qemu module to support a UICC connected through the PC/SC host interface and ASSD support - for emulator builds only

#### BIP support - _currently not maintained_ ####
  * Patch the sources according to [BIP Extensions](http://code.google.com/p/seek-for-android/wiki/BIP_Extensions) - only for BIP support

#### PC/SC support - _currently not maintained_ ####
  * Patch the sources according to [PCSC Support](http://code.google.com/p/seek-for-android/wiki/PCSCSmartCardServiceIntro) - only for (native) Android applications requesting the PC/SC interface.<br />

#### Apply the patches ####
```
$ cd <ANDROID_ROOT_DIR>
$ patch -p1 < <path_to_my_patches>/smartcard-api.patch
$ patch -p1 < <path_to_my_patches>/uicc.patch
$ patch -p1 < <path_to_my_patches>/emulator.patch
```

After applying the patches the android source tree contains all SmartCard API source files in `packages/apps/SmartCardService`:
  * `SmartCardService` - contains the complete SmartCard API project
    * `jni` - contains the native sources for ASSD support
    * `src` - contains the sources of the SmartCard API Service
    * `openmobileapi` - contains the Open Mobile API shared library
In addition, the smartcard-api.patch extends `build/target/product/core.mk` to include the SmartCard API in the built.<br />
Also, the UID/GID smartcard/smartcard is introduced to have the SmartCardService installed with a unique ID.<br />
If the optional `uicc.patch` is not applied, the SmartCard API will not compile as the `UiccTerminal.java` connector requires those changes in the Android Telephony framework. In such case, remove the file `UiccTerminal.java` before compiling the system.<br />

Update `current.xml` as the system needs to known the new IDs - required only once:
```
$ make update-api
```
<br />

## Extract the vendor specific libraries ##
Development devices will not support all hardware modules without proprietary (closed-source) libraries. Make sure to have downloaded the correct [vendor specific libraries](https://developers.google.com/android/nexus/drivers) and extracted the files accordingly, e.g.
```
$ cd <ANDROID_ROOT_DIR>
$ ./extract-lge-mako.sh
$ ./extract-qcom-mako.sh
$ ./extract-broadcom-mako.sh
```
and follow the steps in the shell scripts.
<br /><br />

## Optional: _embedded SE support_ ##
Embedded SE access on Ice Cream Sandwich (and above) is protected with the Android permission `com.android.nfc.permission.NFCEE_ADMIN` in addition to the client whitelist file `/system/etc/nfcee_access.xml`.
This file claims to include _signatures_ of applications that can retrieve the NFCEE\_ADMIN permission. However, it seems to be the signer  certificate chain instead of the application signature.<br />
The content of `nfcee_access.xml` can be created with:
```
$ cd <ANDROID_ROOT_DIR>
$ tail -n+2 build/target/product/security/platform.x509.pem | head -n-1 | base64 -d | hexdump
```
Any system integrator who is including the SmartCard API in his own build system needs to adapt the access control file located under `device/<vendor>/<product>/nfcee_access.xml` with their own certificate to grant embedded SE access.<br />
<br />

## Building the system ##
Compile the sources for the target device by either using `lunch` or setting the environment variables manually. Replace `maguro` with `grouper`, `manta` or `mako` depending on the actual development device:
```
$ cd <ANDROID_ROOT_DIR>
$ . build/envsetup.sh 
$ lunch full_mako-eng
============================================
PLATFORM_VERSION_CODENAME=REL
PLATFORM_VERSION=4.4
TARGET_PRODUCT=full_mako
TARGET_BUILD_VARIANT=eng
TARGET_BUILD_TYPE=release
TARGET_BUILD_APPS=
TARGET_ARCH=arm
TARGET_ARCH_VARIANT=armv7-a-neon
TARGET_CPU_VARIANT=krait
HOST_ARCH=x86
HOST_OS=linux
HOST_OS_EXTRA=Linux-3.4.9-gentoo-x86_64-Intel-R-_Xeon-R-_CPU_E5-2687W_0_@_3.10GHz-with-gentoo-2.2
HOST_BUILD_TYPE=release
BUILD_ID=JSS15Q
OUT_DIR=out
============================================
```
Finally, compile the system with
```
$ make -jXX
```
where XX is the number of parallel jobs and have a (quick) break.
<br />
**Note.1** Using `lunch` without parameters provides a list of build variants with _userdebug_ enabled, it is recommended to use _eng_ instead for adb root access without su.<br />
**Note.2** The Internet might provide more up-to-date information about how to build the system for a real devices, especially when non SmartCard API related problems arise.<br />
<br />

## Flashing the device ##
**Note** You'll loose all data on the device if you proceed - ALL data!<br />
To flash the device, execute:
```
$ ./out/host/linux-x86/bin/adb reboot bootloader
$ ANDROID_PRODUCT_OUT=out/target/product/mako ./out/host/linux-x86/bin/fastboot -w flashall
```
Alternatively, flash each partition manually:
```
$ ./out/host/linux-x86/bin/fastboot erase userdata
$ ./out/host/linux-x86/bin/fastboot erase cache
$ ./out/host/linux-x86/bin/fastboot flash boot out/target/product/maguro/boot.img
$ ./out/host/linux-x86/bin/fastboot flash system out/target/product/maguro/system.img
$ ./out/host/linux-x86/bin/fastboot flash userdata out/target/product/maguro/userdata.img
```
Again, replace `mako` with `grouper`, `manta` or `maguro` depending on the development device. Skip flashing the boot partition if you don't want to replace the existing kernel and ramdisk by executing `fastboot boot boot.img` as the last command.<br />
Reboot into Android with:
```
$ ./out/host/linux-x86/bin/fastboot reboot
```
<br />

## Deprecated: _Building the SDK_ ##
Since SmartCard API provided as shared library it is not required to rebuild the SDK but just include the org.simalliance.openmobileapi shared library in the Android project as  described in [Using SmartCard API](UsingSmartCardAPI.md).<br />
If required, the SDK for Linux or Mac-OS X can still be build with
```
$ make PRODUCT-sdk-sdk -j32
```
The new SDK is located under `out/host/linux-x86/sdk` if required.
<br /><br />

## Generating the documentation ##
Execute
```
$ make docs
```
to find the offline documentation under `out/target/common/docs/offline-sdk/reference` or use the [ApiDoc](http://seek-for-android.googlecode.com/svn/trunk/doc/index.html) instead.
<br /><br />

## Continue ##
with the instructions as described in section [EmulatorExtension](EmulatorExtension.md) and/or [UsingSmartCardAPI](UsingSmartCardAPI.md)