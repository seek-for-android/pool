# Introduction #

Flashing the system is not supported on every device, thus using the SmartCard API is limited to development phone only.<br />
However, access to Secure Elements does not always require flashing the system image. Also, application developers can use the SmartcardService as a quick way to develop their applications as well as end users who just want to use an application with smart card support.<br />
_MSC SmartcardService_ enables (any) Android phone with SD card slot to use SmartCard API without flashing the system or rooting the phone.<br />
The _APK_ inside the archive can be installed on the device like on any other Android application.<br />
**Note**<br>
<ul><li>The <i>MSC SmartcardService</i> will only support the G&D Mobile Security Card<br>
</li><li>The source code of <i>MSC SmartcardService</i> is equal to the SmartCard API provided on SEEK for system integration with the exception of <code>libmsc.so</code> which will not be provided Open Source.<br>
<br /></li></ul>

<h1>Details</h1>
To access the SE on the MSC there are two interfaces available. The Advanced Security SD (ASSD) interface is specified by the SD Association and allows the host to access the SE over a set of standardized SD commands. If the host does not support the additional ASSD commands or it is not possible to include the ASSD (kernel) driver, there is a non-standard possibility to access the SE over special reads and writes to the file system. This requires that the card is accessible through the hosts file system or on a block-level without caching in between (O_DIRECT required). The standardized ASSD interface should be the preferred solution if the host is capable to provide ASSD functionality.<br>
<br>
The MSC drivers uses the host operating file system calls to transfer the APDUs between card and host. It appears to the host as if standard read and write commands were performed on a specific file on the SD card.<br>
<br>
It is required that no caching mechanism is in between that block direct file access, e.g. when a crypto layer is in between.<br>
<br>
<table>
<tr>
<td width='55%' valign='top'>
<h1>Installation</h1>
To test the <i>MSC SmartcardService</i>
<ul><li>delete older version of MSC SmartcardService.apk and OpenMobileAPISample.apk on the phone</li></ul>

<ul><li>download the <a href='https://code.google.com/p/seek-for-android/downloads/detail?name=MSC_SmartCardService-3_1_0.tgz'>MSC-SmartcardService-3_1_0.tgz</a> and extract the content</li></ul>

<ul><li>ensure that <i>USB debugging</i> is enabled on the phone<br />(<i>Settings</i> - <i>Applications</i> - <i>Development</i> - <i>USB debugging</i>)</li></ul>

<ul><li>execute <code>adb install MSC_SmartcardService.apk</code></li></ul>

<ul><li>execute <code>adb install OpenMobileAPISample.apk</code></li></ul>

<ul><li>ensure a G&D Mobile Security Card is inserted in the phone</li></ul>

<ul><li>on the phone, run the OpenMobileAPISample application and see the log output</li></ul>

<ul><li>a GlobalPlatform compliant Access Control applet is optional for the Mobile Security card. If it is installed, the MSC Smartcard Service will grant access to the Mobile Security Card based on the rules that the applet defines. When no applet is install full access for all applications is granted.<br>
</td>
<td width='5%'>
</td>
<td width='40%'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/MscSmartcardService_screenshot.png' width='250' />
</td>
</tr>
</table></li></ul>

<h1>Development</h1>
The <i>MSC SmartcardService</i> provides the <b>same</b> interface as the SmartCard API so application developers can write and test their applications with the installable version and have no migration issues later on. Note that the package names are different for the <i>MSC SmartcardService</i> (com.mobilesecuritycard.<code>*</code>) and the SmartCard API (org.simalliance.<code>*</code>).<br />
Use the provided com.mobilesecuritycard.openmobileapi.jar and the sample project to develop Android applications.<br>
<br>
<h4>Access Control</h4>
As the <a href='AccessControlIntroduction.md'>GlobalPlatform Secure Element Access Control</a> is also included in the <i>MSC SmartcardService</i>, a corresponding Access Control application can optionally be installed in the MSC and personalized with the proper rules.<br />
For development purposes, the <a href='AllowAll.md'>dummy-ARA applet</a> can be used that grants access to the MSC for any application.<br>
In case no Access Control application can be selected on the MSC, Access control will be disabled.<br>
<br />

<h4>Write permission to the MSC</h4>
Since AOSP 3.x application will not have write access to MicroSD cards out of the box which will also hinder the <i>MSC SmartcardService</i> to work. Most commercial device have implemented a <i>work around</i> for this issue but it is up to the OEM to implement such.<br />
<i>Background</i>: the new <code>WRITE_MEDIA_STORAGE</code> permission for the <code>MEDIA_RW</code> gid is protected with the <code>signatureOrSystem</code> level hindering all user application to retrieve such. See <code>frameworks/base/core/res/AndroidManifest.xml, frameworks/base/data/etc/platform.xml</code> and <code>system/vold/Volume.cpp</code> for more details.