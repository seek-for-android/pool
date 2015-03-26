# 1 Introduction #


## 1.1 Smart Card based PKI (Public Key Infrastructure) ##


PKI is the common way to authenticate a message sender or to encrypt/decrypt messages in a secure way. It is typically used by eMails (S/MIME), data encryption and secure authentication (VPN, SSL/TLS).
It is based on public/private keys (typically RSA) and certificates, which bind a user identity to a public key.


PKI is based on the secrecy of the private key, so the private key is often stored on a smart card, where the smart card performs the cryptographic operations to avoid the risk that the private key can become public or copied in any way.


The most common API to access cryptographic smart card functions is the [PKCS#11](http://www.rsa.com/rsalabs/node.asp?id=2133) interface, published by [RSA Labs](http://www.rsa.com/rsalabs/node.asp?id=2124).
It is used in Mozilla Firefox, Thunderbird and many other applications, e.g. PGP.
Corresponding to the PKCS#11 interface, the [PKCS#15](http://www.rsa.com/rsalabs/node.asp?id=2141) specification defines a file structure and description syntax for keys and certificates on the smart card.


## 1.2 OpenSC ##
[OpenSC](http://www.opensc-project.org/opensc) is a widely-used Open Source development, which provides a set of libraries and tools for accessing smart cards for management and cryptographic operations.
It provides a PKCS#11 interface and already supports smart cards from different vendors.
OpenSC provides smart card access according to the PKCS#15 standard.
A good overview about OpenSC can be found [here](http://www.opensc-project.org/opensc/wiki/OverView)


## 1.3 Muscle Card Applet ##
The Muscle Card Applet is an Open Source Java Card applet, originally developed by the [M.U.S.C.L.E](http://www.musclecard.com/musclecard/) project
The Muscle Card applet can be loaded on smart cards with Java Card OS and allow to securely store keys and other objects, (e.g. certificates) on the smart card and use them for cryptographic functionality, like signature generation or decryption with RSA keys.
The Muscle Card Applet is supported by OpenSC (http://www.opensc-project.org/opensc/wiki/MuscleApplet)<br />




To provide PKI functionality on the Android OS, we use OpenSC with modifications for the Android platform. To store cryptographic keys on the smart card and perform cryptographic operations, we use the Muscle Card Applet for smart cards with Java Card OS. <br />


# 2 Add PKI support with OpenSC to Android #


## 2.1 Prerequisites ##


  * Android 2.3.3 (gingerbread) sources with smart card related patches (see [Building the system](http://code.google.com/p/seek-for-android/wiki/BuildingTheSystem))
  * Secure Element with Java Card OS in microSD format (_e.g. G&D Mobile Security Card_), for using it on a real hardware device.
  * Tool to load a Java Card applet onto the Secure Element (_e.g. G&D JLoad_ for _G&D Mobile Security Card_)


From the download page get the OpenSC for Android package [opensc\_android\_package\_v2.0.1.tgz](http://seek-for-android.googlecode.com/files/opensc_android_package_v2.0.1.tgz) <br><br>
This package contains:<br>
<ul><li>OpenSC 0.11.13: <i>external_opensc_0.11.13.tar.gz</i>
</li><li>Patch for OpenSC for Android: <i>opensc_android.patch</i>
</li><li>Muscle Card Applet 0.9.12: <i>MCardApplet-0912a.tar.gz</i>
</li><li>sample RSA key with certificate: <i>azur1024bit_password_12345678.pfx</i></li></ul>


<br>The system environment for porting the PKI functionality to the Android 2.3.3 (gingerbread) was an Ubuntu 10.04 with gcc 4.4.3.<br>
As Secure Element a <i>G&D Mobile Security Card</i> and for loading the Java card applet, the <i>G&D JLoad</i> was used, provided within the <i>G&D Mobile Security Developers Kit</i> <a href='https://www.cardsolutions-shop.com/shop/gi-de/'>https://www.cardsolutions-shop.com/shop/gi-de/</a>.<br>As phone, there was a HTC Magic (Sapphire) used.<br>




<h2>2.2 Add OpenSC to the Android sources</h2>


To add the support for OpenSC to your Android sources, you need to build your system with smart card support according to the description <a href='http://code.google.com/p/seek-for-android/wiki/BuildingTheSystem'>Building the system</a> and additionally add the patches for OpenSC to your sources.<br>
<br>
<br>
After you performed the step "Patching the source" as described in <a href='http://code.google.com/p/seek-for-android/wiki/BuildingTheSystem'>Building the system</a> add the patches for OpenSC.<br>
<br>
<br>
Apply the patches for OpenSC in the root directory of the froyo source then continue to build your system as described in <a href='http://code.google.com/p/seek-for-android/wiki/BuildingTheSystem'>Building the system</a>
<pre><code>$ tar xvzf  external_opensc_0.11.13.tar.gz<br>
$ patch -p1 &lt; &lt;path_to_patch&gt;/opensc_android.patch<br>
</code></pre>




Once the system was built successfully and flashed to the phone, there will be additional components available:<br>
<pre><code>directory: /system/bin<br>
 opensc-tool (command line utility for smart card operations, e.g. sending commands)<br>
 pkcs15-init (command line utility to initialise a smart card with the PKCS#15 structure)<br>
 pkcs15-tool (command line utility to explore/access PKCS#15 file structure)<br>
 pkcs11-tool (command line utlity to access smart cards using a PKCS#11 library, e.g. to perform a signature)<br>
</code></pre>
<pre><code>directory: /system/lib<br>
 opensc-pkcs11.so  (PKCS#11 library)<br>
 pkcs11-spy.so  (dummy PKCS#11 library, when used, it logs all calls to the PKS#11 library for debugging purposes)<br>
</code></pre>
<pre><code>directory: /etc<br>
 opensc.conf  (OpenSC config file)<br>
 pkcs15.profile  (.profile files contain information about the file structure to create during initialisation of the smart card)<br>
 jcop.profile<br>
 muscle.profile<br>
 oberthur.profile<br>
 cyberflex.profile<br>
</code></pre>


<h2>2.3 Load the Muscle Java Card Applet onto the smart card</h2>


Unpack the package containing the Muscle Card Applet.<br>
<pre><code>tar xvzf MCardApplet-0912a.tar.gz<br>
</code></pre>
The package contains the Muscle Java Card Applet 0.9.12, which consist of the sources of 0.9.11 and the <i>Ant</i> script from version 0.9.12 for easy compile and convert it.<br>
The original source of the Muscle Applet was slightly modified to accept CLA (Class-Byte) '90' additional to CLA='B0', because CLA='B0' will not be supported in Smartcard-API 2.3 as it is not coded according to Global Platform CardSpec 2.2.<br>
<br>
There is already a compiled and converted cap file added to the package, which can be directly loaded onto a smart card with Java Card OS.<br>
It is located in the following directory:<br>
<pre><code>/MCardApplet/GD/com/musclecard/CardEdge/javacard/CardEdge.cap<br>
</code></pre>
The Java card applet was converted using the  Sun Java Card Development Kit 2.2.1, so<br>
there is no need to perform the compilation and conversion for the Muscle Java Card applet. Nevertheless it is possible to perform this steps, therefore a Sun JDK and the Sun Java Card Development Kit is required.<br>
If you compile and convert the Muscle Java Card Applet, you need to download the Sun Java Card Development Kit 2.2.1 from the internet (due to licensing conditions it is not included) and copy it to the directory <i>/MCardApplet/depends/jc221</i>).<br>
Then you can perform the compile/convert process for the applet by running <i>Ant</i>:<br>
ant <code>&lt;target&gt;</code> (e.g. <i>"ant GD"</i>). The process is also described in the <i>INSTALL</i> file included in the package.<br>
<br>
<br>
For loading the cap-File <i>CardEdge.cap</i> onto the smart card you need to use an appropriate tool, which is typically provided from the manufacturer of the smart card (e.g. <i>G&D JLoad</i> for the <i>G&D Mobile Security Card</i>).<br>
<br>
<br>
<h2>2.4 Use OpenSC on the phone</h2>


Once you have flashed your phone with the modified sources and inserted the secure element (e.g. G&D Mobile Security Card) in the device, the first step is to intialise the applet and the PKCS#15 structure on the applet, which sets the initial PIN values and creates all the data structures required.<br>
<br>
<br>
Additionally there are further examples shown, how to use some of the functionality of the OpenSC Tools (e.g, import keys/certificates, create signature, logging of PKCS#11 calls).<br>
For a description of OpenSC Tools and its parameters refer to the OpenSC project <a href='http://www.opensc-project.org/opensc'>http://www.opensc-project.org/opensc</a>
Calling the commandline tools without a parameter shows the parameters available.<br>
<br>
<br>
<h3>2.4.1 Initialise Muscle Card Applet</h3>


To initialise the Muscle Card Applet there is required to send a proprietary command, setting the initial PIN values.<br>
Therefore you need to connect your Android phone to the computer and open a connection to the device with the Android <i>adb</i> tool.<br>
Then you can use the OpenSC <i>opensc-tool</i> utility to perform the initialisation of the JavaCard Applet.<br>
<pre><code>$ adb shell<br>
# cd /system/bin<br>
# opensc-tool -s 00:A4:04:00:06:A0:00:00:00:01:01 -s B0:2A:00:00:38:08:4D:75:73:63:6C:65:30:30:10:10:08:31:32:33:34:35:36:37:38:08:31:32:33:34:35:36:37:38:10:10:08:31:32:33:34:35:36:37:38:08:31:32:33:34:35:36:37:38:00:00:10:00:00:00:00<br>
</code></pre>
The initial values of all PINs/passwords are set to "12345678"<br>
<br>
<br>
A description of the initialisation command can be found here:<br>
<a href='http://www.opensc-project.org/opensc/wiki/MuscleApplet'>http://www.opensc-project.org/opensc/wiki/MuscleApplet</a>


<img src='http://seek-for-android.googlecode.com/svn/wiki/img/PKI2_snap1_opensc_init.png' />


<h3>2.4.2 Create PKCS#15 structure</h3>


To create the PKCS#15 structure on the smart card use the <i>pkcs15-tool</i>.<br>
The parameter <i>r</i> adresses the reader, here the value <i>0</i> is for the UICC and <i>1_for the MSC, which we use.<br>
<pre><code># pkcs15-init -r 1 -C<br>
</code></pre>
During initalisation process you will be asked to enter the values for PINs/passwords.</i><br>In our sample, we set the value "12345678" for all passwords.<br>
<br>Further information to the pkcs15-init tool can be found here: <a href='http://www.opensc-project.org/opensc/wiki/CardPersonalization'>CardPersonalisation</a>


<img src='http://seek-for-android.googlecode.com/svn/wiki/img/PKI2_snap2_init_p15.png' />


<h3>2.4.3  Dump PKCS#15 content on the card.</h3>


The <i>pkcs15-tool</i> allows to show the PKCS#15 content stored on the smart card.<br>
The content of the smart card can be listet by using the following command:<br>
<pre><code># pkcs15-tool --dump<br>
</code></pre>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/PKI2_snap3_p15_dump.png' />


<h3>2.4.4 Import a RSA key and certificate.</h3>


One way to put a key and certificate onto the smart card, is to import it from an encrypted <a href='http://www.rsa.com/rsalabs/node.asp?id=2138'>PKCS#12</a> container.<br>
A sample PKCS#12 container file can be downloaded here: <a href='http://seek-for-android.googlecode.com/files/azur1024bit_password_12345678.pfx'>azur1024bit_password_12345678.pfx</a>
It contains a 1024 bit RSA key and a corresponding certificate for a dummy user named "azur".<br>
To import it into the smart card, you need to copy the file to the phone by using the <i>adb push</i> command.<br>
then you can import it with the <i>pkcs15-init</i> utility. The import process requires you to enter the password of the container file,<br>
which is "12345678" and the password of the smartcard you set during intialisation.<br>
<pre><code>$ adb push azur1024bit_password_12345678.pfx /data<br>
$ adb shell<br>
# cd /system/bin<br>
# pkcs15-init -S /data/azur1024bit_password_12345678.pfx -f PKCS12 -a FF -i 10<br>
</code></pre>


<img src='http://seek-for-android.googlecode.com/svn/wiki/img/PKI2_snap4_import_key_from_p12.png' />


<h3>2.4.5 Dump smart card content</h3>


After successful import of the key and certificate you can see the new objects by dumping the content of the smart card<br>
<pre><code># pkcs15-tool --dump<br>
</code></pre>


<img src='http://seek-for-android.googlecode.com/svn/wiki/img/PKI2_snap5_p15_dump_key.png' />


<h3>2.4.6 List PKC#11 Slots</h3>


For operating with the PKCS#11 interface, it is required to know the PKCS#11 Slot, which contains the Secure Element.<br>
Here, in the slot 4 the Secure Element is available.<br>
<pre><code># pkcs11-tool -L<br>
</code></pre>

<img src='http://seek-for-android.googlecode.com/svn/wiki/img/PKI2_snap5a_p15_list_slots.png' />

<h3>2.4.7 Perform a signature using the PKCS#11 library.</h3>


With the <i>pkcs11-tool</i> it is possible to perform cryptographic operations with the PKCS#11 library <i>opensc-pkcs11.so</i>.<br>
The PKCS#11 library is the standard interface typically used by applications (e.g. Firefox) to perform PKI functionality.<br>
Here we use the <i>pkcs11-tool</i> to generate a digital signature for a file. The signature is created by the smart card with the private key stored onto it.<br>
<br>
<br>
Here we compute the digital signature for the file <i>azur1024bit_password_12345678.pfx</i>, but we can use any other file also.<br>
The result is stored in the output file. The output file created has a length of 128 Bytes and contains the digital signature computed by the smart card.<br>
(The signature has the same length as the Private key on the smart card, which is 1024 bit).<br>
As signature algorithm we use the SHA1-RSA-PKCS, which performs both, a SHA-1 hash and signature according to the RSA algorithm in one step.<br>
<pre><code># pkcs11-tool --sign --slot 4 -m SHA1-RSA-PKCS --input-file /data/azur1024bit_password_12345678.pfx --output-file /data/signature<br>
</code></pre>


<img src='http://seek-for-android.googlecode.com/svn/wiki/img/PKI2_snap6_create_singature.png' />


<h3>2.4.8 Logging the PKCS#11 calls</h3>


To see the PKCS#11 calls sent to the PKCS#11 library it is possible to load the <i>pkcs11-spy.so</i> libary and log the calls before forwarding it to the real PKCS#11 library <i>opensc-pkcs11.so</i>.<br>
<br>
<br>
In this example we call the digital signature operation as described before, but we explicitely load the <i>pkcs11-spy.so</i> library to log the PKCS#11 calls.<br>
With the environment variable <code>PKCS11SPY</code> we advise the <i>pkcs11-spy.so</i> library to forward the calls to the real PKCS#11 library <i>opensc-pkcs11.so</i>.<br>
This will display all the PKCS#11 calls including sensitive information like PIN values entered. This is useful for debugging with applications using the PKCS#11 interface.<br>
<pre><code># PKCS11SPY=/system/lib/opensc-pkcs11.so ./pkcs11-tool --module /system/lib/pkcs11-spy.so ./pkcs11-tool --sign --slot 4 -m SHA1-RSA-PKCS --input-file /data/azur1024bit_password_12345678.pfx --output-file /data/signature<br>
</code></pre>


<img src='http://seek-for-android.googlecode.com/svn/wiki/img/PKI_snap7_p11spy.png' />


<h1>3 Future Perspective</h1>


Standard PKCS#11 interface with smart card access is one step to standard PKI infrastructure on Android.<br>
It requires also standard applications, e.g. Browsers, eMail-Clients supporting this interface on Android.<br>
<br>
<br>
Other PKI Open Source projects/developments that should be investigated:<br>
<ul><li>Firefox Browser for Android / "fennec" Project. Is there PKCS#11 Support like in Firefox available ?<br>
</li><li>Java Interface/Wrapper for access of PKI functionality from Java Applications (e.g IAIK PKC#11 Wrapper).</li></ul>

<h3>Version History</h3>

opensc_android_package_v2.0.1.tgz<br>
<br>
<ul><li>Muscle Applet was modified to accept CLA (Class-Byte) '90' additional to CLA='B0', because CLA='B0' will no longer accepted in Smartcard-API 2.3 as it is not coded according to Global Platform CardSpec 2.2.</li></ul>


opensc_android_package_v2.0.tgz<br>
<br>
<ul><li>initial version, based on OpenSc 0.11.13