<table>
<tr>
<td valign='top'>
<h1>Introduction</h1>

For using the Android emulator with a real SIM or microSD card with integrated secure element, the emulator must be built with PC/SC support through applies the emulator patch.<br />

<h2>1. UICC Support in Android Emulator</h2>

The vanilla Android emulator supports a primitive SIM simulation which cannot be used to run applets or personalize files nor does it support Sim Toolkit functionality.<br />
On real devices, the proprietary RIL library is very restrictive in terms of APDU access to the SIM or STK support and (normally) needs to be extended. However, on the emulator a patch can be applied in order to provide full SIM access through the host PC/SC system.<br />
This extension enables the Android emulator to forward any APDU traffic (AT+CRSM, AT+CSIM, etc. commands) to a real SIM card that is connected through a PC/SC card reader and accessible with pcsc-lite running on the host. Additionally, this patch adds the missing support for the Sim Toolkit framework to the emulator's RIL implementation (reference-ril).<br>
</td>
<td>
</td>
<td>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/emulator-extensions.png' />
</td>
</tr>
</table>

### 1.1 Applying the patch to the android sources ###

  * Download the [smartcard-api-3\_2\_1.tgz](https://drive.google.com/file/d/0B63jMJOYc2l3UXJFWVdaQUlyeFk/edit?usp=sharing) archive
  * Unpack the patch archive:
```
$ tar xvzf smartcard-api-3_2_1.tgz
```
  * Apply the patches in the root folder of the Android source tree:
```
$ cd <ANDROID_ROOT_DIR>
$ patch -p1 < <path_to_my_patches>/smartcard-api.patch
$ patch -p1 < <path_to_my_patches>/uicc.patch
$ patch -p1 < <path_to_my_patches>/emulator.patch
```
  * Download pcsc-lite from the official Internet sources and build the modules. It is recommended to use the pcsc-lite source code and not the version provided from the package manager of the distribution since both 32bit and 64bit versions are required as Android 4.1 and above creates 32bit and 64bit emulator executables.
```
$ tar xvjf pcsc-lite-<version>.tar.bz2
$ cd <path_to_pcsc-lite_32bit>
$ CFLAGS="-m32" LDFLAGS="-m32" ./configure; make
$ cd <path_to_pcsc-lite_64bit>
$ CFLAGS="-m64" LDFLAGS="-m64" ./configure; make
```
> Most problems compiling the Android emulator arise from missing or invalid pcsc-lite libraries as they need to be provided twice. Please check with `readelf -h` if one `libpcsclite.so` is ELF32 and the other one ELF64!
  * Setup build environment:
```
$ cd <ANDROID_ROOT_DIR>
$ . build/envsetup.sh 
$ lunch full-eng
```
  * Update current.xml as the system needs to known the new IDs (if not already done):
```
$ make update-api
```
  * Build the emulator with PC/SC support within the product SDK:
```
$ PCSC_INCPATH=<path_to_pcsc-lite_32bit>/src/PCSC/ PCSC32_LIBPATH=<path_to_pcsc-lite_32bit>/src/.libs/ PCSC64_LIBPATH=<path_to_pcsc-lite_64bit>/src/.libs/ make -j32 PRODUCT-sdk-sdk
```
  * The sdk can then be found at
```
<ANDROID_ROOT_DIR>/out/host/linux-x86/sdk/
```

  * Start SDK Manager with:
```
$ ./out/host/linux-x86/sdk/android-sdk_eng.<username>_linux-x86/tools/android
```
> The full path name is necessary since you might have also an offical installation of the Android SDK in your PATH already.
  * If you want to develop applications using the Smartcard API the shared library approach is recommended. Therefore your self compiled sdk needs the Shared library addon which can be found at
```
http://seek-for-android.googlecode.com/svn/trunk/repository/<API-level>/addon.xml 
```
  * In case the SDK Manager wants to update SDK Tools from the Internet do not execute this action as otherwise the PCSC-enabled emulator image will be overwritten with the official Google release. It is recommended to use a separate Android SDK folder e.g. the official Version to download the addon and then copy it to your own SDK. The addons are located here
```
<ANDROID_ROOT_DIR>/out/host/linux-x86/sdk/android-sdk_eng.<username>_linux-x86/add-ons/
```
> > ![http://seek-for-android.googlecode.com/svn/wiki/img/ScreenshotAndroidSDKManager.png](http://seek-for-android.googlecode.com/svn/wiki/img/ScreenshotAndroidSDKManager.png)
  * Create an AVD with behalf of the AVD Manager "Tools->Manage AVD's..."
> > ![http://seek-for-android.googlecode.com/svn/wiki/img/ScreenshotAndroidVirtualDeviceManager.png](http://seek-for-android.googlecode.com/svn/wiki/img/ScreenshotAndroidVirtualDeviceManager.png)
  * Start the emulator
```
$ ./out/host/linux-x86/sdk/android-sdk_eng.<username>_linux-x86/tools/emulator @testAvd -pcsc 
```

> When `-pcsc` does not contain a valid PCSC card reader name the first available reader will be used.
> The SIM must be already inserted in the used PC/SC reader before launching the emulator.

  * In the emulator output a card reader will get listed, e.g.
```
sim_card.c: OMNIKEY AG CardMan 3121 00 00
sim_card.c: using card reader OMNIKEY AG CardMan 3121 00 00
```

  * To start the emulator from the eclipse ide, add the command line parameter "-pcsc" via _Run -> Debug Configurations/Run Configurations -> Additional Emulator Command Line Options_

![http://seek-for-android.googlecode.com/svn/wiki/img/EmulatorExtensionScreenshot.png](http://seek-for-android.googlecode.com/svn/wiki/img/EmulatorExtensionScreenshot.png)

### 1.2 Speed up the emulator ###
The default ARM image created with `make PRODUCT-sdk-sdk` might run a bit slow in `qemu` on the host machine but with KVM support and an x86 Android image the execution speed of the emulator can be enhanced A LOT.
See [here](http://developer.android.com/tools/devices/emulator.html)<br />
To create the x86 emulator image compile with
```
make PRODUCT-sdk_x86-sdk
```
and start the emulator with
```
emulator @testAvd -pcsc -gpu on -qemu -m 1024 -enable-kvm
```
Of course, KVM must be available in the host environment.
When problems occur `-verbose` as command line option might help to trace down the problem.<br /><br />

## 2. ASSD Support in Android Emulator (for microSD with integrated secure element) -- Not maintained ##

Enabling ASSD support in the Android emulator, allows to access a Secure SD Card with integrated Secure Element by using the ASSD interface (Advanced Security SD specification). Therefore it is required to additionally enable both, the kernel of the emulator and the kernel of the Host-PC with ASSD support.

### 2.1 Enabling ASSD Support in Android Emulator ###

  * Retrieve the kernel source for the emulator
```
$ git clone https://android.googlesource.com/kernel/goldfish
The repository is created in the subdirectory 'goldfish'
$cd <GOLDFISH_DIR>
<GOLDFISH_DIR>$git checkout android-goldfish-2.6.29 
```

  * Apply the patch
```
<GOLDFISH_DIR>$ patch -p1 < assd_kernel.patch
```

  * Build the kernel
```
<GOLDFISH_DIR>$export ARCH=arm
<GOLDFISH_DIR>$export CROSS_COMPILE=arm-eabi-
<GOLDFISH_DIR>$export PATH=<PATH_TO_ANDROID_SOURCE>/prebuilt/linux-x86/toolchain/arm-eabi-4.4.0/bin:$PATH
<GOLDFISH_DIR>$make goldfish_defconfig
<GOLDFISH_DIR>$make    
```

Once finished, the following message occurs
_'Kernel: arch/arm/boot/zImage is ready'_

### 2.2 ASSD Support in Host-PC ###

**Note:** The ASSD support requires an SD-Card slot with SD-Card controller on your host. It will not work with SD-Card readers connected via USB.

  * Create a new kernel for the Host-PC by adding the ASSD-Patch and reboot the host with the new kernel.
  * The patch to enable ASSD in the kernel of the host is identical to the patch applied to the emulator kernel source.
  * Once the kernel of the host is replaced, ensure that the device node `/dev/assd` is present and the user has write permissions to it.

### 2.3 Run the emulator with ASSD Support ###

  * Create an AVD and launch the emulator:
```
android create avd -n assdAvd -t 1 -c 10M
ANDROID_SDK_ROOT=<path_to_android-sdk-linux> ./out/host/linux-x86/bin/emulator    
    -avd assdAvd -system out/target/product/generic/system.img 
    -kernel goldfish/prebuilt/android-arm/kernel/zImage
```
  * The microSD must be already inserted in the SD Card-Reader before launching the emulator.
  * In the emulator output there will information shown regarding the status of ASSD support, e.g.
`goldfish_mmc: ASSD is ready`
`goldfish_mmc: ASSD available`
<br /><br />

## 3. Command line interface ##
After the emulator extensions patch is applied, these command-line options are available to the emulator.
```
Option                    Description
-pcsc <terminal name>     Enable access to a SIM card inserted to a PC/SC card reader. If <card reader name> is
                          omitted the emulator outputs a list of card reader names and uses the first card reader
                          from that list.

-no_dns                   Workaround for running the emulator on a host without configured DNS thus without network
                          connectivity.
```