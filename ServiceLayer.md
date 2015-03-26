# Introduction #

This page describes how to build android including the Service Layer APIs, as well as how to build android applications using it.

The Service Layer is based on SIM Alliance's Open Mobile API v2.04, and include the following components:
  * Discovery API
  * Authentication API
  * File Management API
  * Secure Storage API
  * PKCS#15 API
As of right now, Crypto API is not implemented.


# Building Android #

The current Service Layer addon works with Androd 4.3 and SmartCard API 3.1. So, before using the Service Layer, make sure that you can compile Android 4.3 with SmartCardAPI 3.1. In order to do that, follow the steps in [BuildingTheSystem](BuildingTheSystem.md), [EmulatorExtension](EmulatorExtension.md) and [UsingSmartCardAPI](UsingSmartCardAPI.md).

Once you are sure everything works, you can start patching the Service Layer files. In order to include the Open Mobile API Service Layer in the android build, download and extract [ServiceLayerAddon.zip](http://seek-for-android.googlecode.com/files/ServiceLayerAddon.zip), and apply the patch:
```
$ cd <ANDROID_ROOT_DIR>
$ patch -p1 < <path_to_my_patches>/servicelayer.patch
```

You should now be ready to build android with the Service Layer included. The commands to compile are the same as in [BuildingTheSystem](BuildingTheSystem.md).

<img src='http://seek-for-android.googlecode.com/svn/wiki/img/ServiceLayerTesterSnapshot.png' align='right' width='50%' />

# Using the Service Layer #

## Installing the shared library on the Android SDK ##
The same shared library approach as in Transport Layer is adopted to work with the Service Layer.

In order to install the shared library, follow the steps on [Setting up the Environment](https://code.google.com/p/seek-for-android/wiki/UsingSmartCardAPI#Setting_up_the_Environment), but select _Open Mobile API (with Service Layer)_ instead of _Open Mobile API_.

Once it is installed, just set your project's target platform to point to the new shared library (notice that it also includes the transport layer).

## Testing with Servicie Layer Tester ##
You can find a test application on [the seek SVN repository](https://code.google.com/p/seek-for-android/source/checkout), called ServiceLayerTester. It is under the "samples" folder. Notice that, in order to be able to compile it, you will need the shared library installed; and, in order for tests to run propperly, you will need the test applets installed on your secure element.

These are the test cases included in ServiceLayerTester, together with the required JavaCard applets:

  * Discovery API: requires the APDU tester applet (you should have it installed, as it is the Transport Layer test applet).

  * Authentication API: requires the Authentication Test applet.

  * File Management API: requires the File Management Test applet.

  * Secure Storage API: requires the Secure Storage Test applet.

  * PKCS#15 API: requires a valid PKCS#15 file structure present on the Secure Element.

In order to get the test applets, you can either checkout the code from the SVN repo and compile them, or download them from the Downloads section. You will also find a compiled version of the Service Layer Tester.

**Note:** Please notice that this JC Applets are only intended for testing purposes and that they are not a commercial product. Specifically, for the Secure Storage Test Applet, it does not support all the functionalities specified by the Open Mobile API (v2.03 onwards).

Once the required applets are installed on the SE, you can run the tests. Test cases are independent, so you need to install only the dependencies for the parts you want to test. If everything went fine, you should see something like the image on the right.