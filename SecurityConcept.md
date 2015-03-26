### Introduction ###
Introducing a SmartCard API in the Android platform enables security related services for application developers. Access to secure elements should be as open as possible - _the Android way_ - and not protected per default with certificate checks.<br />
However, it needs to be ensured that access to sensitive services can still be controlled and/or protected to hinder malicious applications to interfere with the system.<br />
For SIM cards it's up to the MNO whether card applications should be accessible or not whereas access to the Mobile Security Card could still be open for any developer thus the Secure Element itself is the best entity to define the access control.<br /><br />

### Android Security ###
Android is a multi-process system, where each application (and parts of the system) runs in its own process. Most security between applications and the system is enforced at the process level via standard Linux facilities, such as user and group IDs that are assigned to applications. Additional finer-grained security features are provided via a _permission_ mechanism.<br />
This mechanism enforces restrictions on the specific operations which a particular process can perform, and per-URI permissions for granting ad-hoc access to specific pieces of data.
<br />For more details, see http://developer.android.com/guide/topics/security/security.html.<br />

##### Permissions #####
The SmartCard API uses the Android permission scheme to protect access to secure elements. Therefore it defines a permission `android.permission.SMARTCARD` that a client application must request in its manifest in order to obtain access to the API.<br />
At install time, the user is asked whether or not the application should receive access to his or her secure element.<br />
Access to the lower layer components such as the secure element specific terminal implementation will be protected with the standard Android mechanisms.

##### Root Access #####
The whole Android security scheme is based on standard UID/GID checks, therefore applocations with _root access_ can overcome the security mechanism.<br />
For example, it is possible that a _root_ user replaces APKs or native system services with modified versions that contain tracers or or hooks for other applications. This means that the security concept of the SmartCard API can be broken on rooted phones but this is not an issue of the API design but a general issue on Linux based systems where _root_ can do everything.<br /><br />

### SmartCard API System Security ###
<table>
<tr>
<td width='35%' valign='top'>
The SmartCard API system security discussion explains the mechanism needed so client applications cannot overcome the SmartCard API interface by using the lower level components directly.<br /><br />
In order to protect the low level components, the SmartCard API remote process is installed and registered with a unique UID/GID: <b>smartcard</b><br /><br />
All low level components and methods check for the caller UID and - if not <code>smartcard</code> - throw an exception.<br /><br />
</td>
<td width='5%'>
</td>
<td width='60%'>
<a href='http://code.google.com/p/seek-for-android/wiki/SCAPI_hacking_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/SCAPI_hacking.png' width='250' height='235' />
</a>
<br />
Click to enlarge<br>
</td>
</tr>
</table>

### SmartCard API Access Control Scheme ###
The access control scheme of the SmartCard API is a protection mechanism that ensures that only allowed (or certified) Android applications are able to access specific Java Card applets depending on the APK certificate.<br />
An additional APDU filter scheme (based on an APDU whitelist) is established to have a more detailed control over the access mechanism of an application.<br />
The implementation relies on an extension of the handset integration in combination with a access control application running on the Secure Element.<br />
See [AccessControlIntroduction](AccessControlIntroduction.md) for more details