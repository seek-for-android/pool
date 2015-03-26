# Introduction #

The SmartCard API is a reference implementation of the SIMalliance Open Mobile API specification that will enable Android applications to communicate with Secure Elements, e.g. SIM card, embedded Secure Elements, Mobile Security Card or others.<br />
The interface of the library is kept platform independent whereas the SmartcardService implementation is Android specific.<br />
With SmartCard API version 1 release a proprietary interface was introduced to provide access to Secure Elements. Version 2 included the Open Mobile API interface parallel to the proprietary one. Further releases considered the proprietary interface as deprecated and version 2.3.0 completely removes the old one and only supports the Open Mobile API.<br />
Previous versions extended the Android framework with SmartCard API functionality which caused difficulties for application developers as modified platform SDK libraries were required. Since version 2.1.2 the shared library approach was introduced which does not extend the official framework but provides a library extension instead. Since version 2.3.0 the shared library approach is the only supported concept for the integration.<br />
The SmartCard API provides functionality to list and select the supported Secure Elements, open a communication channel to a dedicated Secure Element application and transfer APDUs to such. No applet selection or channel management APDU functions are allowed as this might cause security problems. Also, no card on/off functions are available in contrast to PC/SC.<br /><br />


## SmartCard API modules ##
<table>
<tr>
<td width='55%' valign='top'>
The SmartCard API consists of several software layers<br>
<ul><li><b>SmartcardService</b> Android remote service as the core of the smart card access<br />Integration in <code>packages/apps/SmartcardService/src</code><br />
</li><li><b>SEService</b> wrapper classes to hide the service binding specifics and deal as the main interface for application developers<br />Integration in <code>packages/apps/SmartCardService/openmobileapi</code><br />
</li><li><b>xxxTerminal</b> Implementation of a Secure Element specific terminal, e.g. <code>UiccTerminal</code> or <code>ASSDTerminal</code>, ...<br />Integration in <code>packages/apps/SmartCardService/src/org/simalliance/openmobileapi/service/terminals</code><br />
</li><li><b>Access Control</b> Implementation of the Access Control Enforcer to control APDU access from Android applications<br />Integration in <code>packages/apps/SmartCardService/src/org/simalliance/openmobileapi/service/security</code>
</td>
<td width='5%'>
</td>
<td width='40%'>
<a href='http://code.google.com/p/seek-for-android/wiki/SCAPI_modules_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/SCAPI_modules.png' width='250' height='235' />
</a>
<br />
Click to enlarge<br>
</td>
</tr>
</table>
<br /></li></ul>

## Architecture ##
The core of the SmartCard API is encapsulated in a remote Android service. Having a single service instance instead of a pure framework library ensures that security checks (who is accessing the service) and resource management (free a logical channel if a client dies) can be guaranteed - even if a client application hangs or dies unexpectedly.<br />
However, using an Android service for an API interface is not typical for smart card applications thus additional framework classes wrap the service specifics, e.g. Binding to a Remote Service object. No further logic is kept in the framework classes, clients are permitted to access the service interface directly (but need to deal with the Service specifics by themselves).<br />
The framework classes around `SEService` are just wrapping the service specifics for application developers.<br />
The card channel is used for actual card communication by transferring APDUs. All channel management for logical card channels is encapsulated by the channel objects.<br /><br />

## System integration ##
The `smartcard-api.patch` includes access to all Secure Elements currently introduced. However, not all phones have SD card slots or embedded Secure Elements. System integrators should remove the builtin terminals the platform does not support (e.g. removing ASSDTerminal on phones that have no SD card slot like Nexus S).<br />
The SmartCard API will enumerate the terminals at run time which means that removing a xxxTerminal.java file is enough to remove a specific SE.<br />
In addition, the addon-terminal concept provides the possibility to extend the supported terminals through additional libraries.<br /><br />

## Using the SmartCard API ##
Download and apply the patch files according to [Building building the system](BuildingTheSystem.md)<br />
Compile the Android platform, build and flash a [device](Devices.md) or start a new [emulator](EmulatorExtension.md) instance with SmartCard API support.<br />
Start working on your own project with a quick start from [Using the SmartCard API](UsingSmartCardAPI.md).<br />
[Eclipse](http://developer.android.com/sdk/installing.html) is recommended to develop smart card applications but not required for Android development.<br />
Import the [samples](http://code.google.com/p/seek-for-android/source/browse/#svn%2Ftrunk%2Fsamples) and run them on the phone or in the emulator. They provide a good starting point.<br />
See SmartCard API [JavaDoc](http://seek-for-android.googlecode.com/svn/trunk/doc/index.html) for further documentation.
<br />