This introduction shows how the SmartCard API with ASSD support can be integrated into an Android device to access SD memory cards with an embedded security system (Advanced Security SD specification provided by the [SD Association](http://www.sdcard.org/developers/overview/ASSD/))

## The ASSD kernel driver can be built by building a custom kernel: ##

### Getting the Kernel sources: ###

follow the desciption on http://source.android.com/source/building-kernels.html


### Download the [20121005-assd-kernel.tgz](http://seek-for-android.googlecode.com/files/20121005-assd-kernel.tgz) patch and extract the content: ###

  * assd.patch - patches the Kernel sources for ASSD support


### Apply the patch: ###

```
<KERNEL_DIR>$ patch -p1 < <path_to_my_patches>/assd.patch
```


### Building the kernel: ###

follow the desciption on http://source.android.com/source/building-kernels.html


## The Android system can be built with ASSD support by using this custom built kernel: ##

### Download the [smartcard-api-2\_4\_0.tgz](http://seek-for-android.googlecode.com/files/smartcard-api-2_4_0.tgz) patch and extract the content: ###

  * smartcard-api.patch - patches the Java sources for SmartCard API support
  * uicc.patch - patches the Android Telephony framework with the required UICC methods for the SmartCard API UiccTerminal support
  * emulator.patch - patches the qemu module to support a UICC connected through the PC/SC host interface instead of the default Android SIM simulator
  * cts.patch - adds the compatibility testsuite for the SmartCard API.


### Apply the patches: ###

```
$ cd <ANDROID_ROOT_DIR> 
$ patch -p1 < <path_to_my_patches>/smartcard-api-2_4_0/smartcard-api.patch 
$ patch -p1 < <path_to_my_patches>/smartcard-api-2_4_0/uicc.patch 
$ patch -p1 < <path_to_my_patches>/smartcard-api-2_4_0/cts.patch 
```

(Note: `emulator.patch` is not needed for the build as the current emulator patch does not support ASSD)

### Apply the patches and update the API: ###

```
$ make update-api
```


### Building the system: ###

Compile the sources for the target device (use passion, sapphire or dream depending on the actual development phone) and specify the location of the custom kernel (built in the previous step).

```
$ cd <ANDROID_ROOT_DIR> 
$ cd device/htc/passion
$ ./extract-files.sh 
$ cd - 
$ TARGET_PRODUCT=full_passion TARGET_PREBUILT_KERNEL=<MSM_DIR>/arch/arm/boot/zImage make
```

Make sure to have properly connected the reference phone to extract the binary files or downloaded the corresponding ZIP archive to run unzip-files.sh instead. The Internet might also provide more up-to-date information about how to build the system for a real device.

### Flashing the phone: ###

Note: You'll loose all data on the phone if you proceed

```
$ <ANDROID_ROOT_DIR>./out/host/linux-x86/bin/adb reboot bootloader
$ <ANDROID_ROOT_DIR>./out/host/linux-x86/bin/fastboot flash boot out/debug/target/product/passion/boot.img 
$ <ANDROID_ROOT_DIR>./out/host/linux-x86/bin/fastboot flash system out/debug/target/product/passion/system.img 
$ <ANDROID_ROOT_DIR>./out/host/linux-x86/bin/fastboot flash userdata out/debug/target/product/passion/data.img
```

Again, replace passion with sapphire or dream depending on the actual development phone. Consider that the boot partition must also be flashed as the kernel is modified for ASSD support.

```
$ <ANDROID_ROOT_DIR>./out/host/linux-x86/bin/fastboot reboot
```

If there are problems while booting it's worth to try

```
$ ./out/host/linux-x86/bin/fastboot erase userdata
```

Instead of flashing the user partition.