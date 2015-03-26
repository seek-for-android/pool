# Introduction #

OATH is a common standard for OTP password generation defined in [RFC 4226](http://www.ietf.org/rfc/rfc4226.txt). The OTP is unique for each user that personalized the calculation with a unique seed. A counter value is incremented after each calculation (event based OTP) or valid for a specific time (time based OTP).<br><br>
An OATH calculation in software is possible but discouraged as the seed could get lost or tampered after flashing or rooting the phone. For this purpose the OATH calculation takes place in the Mobile Security Card where the private seed is kept secure in the OtpAuthenticator applet.<br>
<br>
<h1>Walk through</h1>
<ul><li>Download the installation files from the <a href='http://seek-for-android.googlecode.com/files/otpauth.tar.gz'>Download page</a>
</li></ul><ul><li>Install the <code>oath.cap</code> file on the Mobile Security Card with JLoad or other Java Card compliant Global Platform loader tools.<br>
<b>Note</b>: JLoad is included in the <a href='https://www.cardsolutions-shop.com/shop/gi-de/'>Mobile Security Developer's Kit</a>
</li><li>Install with <code>OtpAuthenticator.apk</code> on your Android device equipped with a Mobile Security Card and <code>MSC_SmartcardService</code> installed.<br>
<b>Note</b>: Without <code>MSC_SmartcardService</code>, please check out the <a href='http://code.google.com/p/seek-for-android/source/browse/#svn/trunk/applications/OtpAuthenticator/OtpAuthenticator'>OtpAuthenticator source code</a> and recompile the APK according to <a href='http://code.google.com/p/seek-for-android/wiki/BuildingTheSystem'>BuildingTheSystem</a>
</li><li>Run the application on the Android phone, open the menu and personalize (=define OATH seed) the applet</li></ul>

<img src='http://seek-for-android.googlecode.com/svn/wiki/img/OtpAuthenticator.png' />
<br /><br /><br />

<h1>Java Card Applet</h1>
<b>The Java Card applet is for demonstration and test purposes only. Do not use in production environments!</b><br /><br />
The applet need to be compiled and converted to a Java Card CAP file with the following AIDs:<br>
<pre><code>PackageAID: 0xD2:0x76:0x00:0x01:0x18:0x00:0x03:0xFF:0x49:0x10:0x00:0x89:0x00:0x00:0x02:0x00<br>
Applet AID: 0xD2:0x76:0x00:0x01:0x18:0x00:0x03:0xFF:0x49:0x10:0x00:0x89:0x00:0x00:0x02:0x01<br>
</code></pre>
See <a href='http://code.google.com/p/seek-for-android/source/browse/#svn/trunk/applications/OtpAuthenticator/JavaCardApplet'>source code</a> for more details<br>
<br>
<h2>APDU Interface</h2>
Four methods are exported in the interface. The CLA byte is 00 but the applet can communicate on logical channels (01, 02 & 03).<br />
If the applet is not personalized yet (after installation) the OTP generation will generate a 6985. The counter value is incremented each time a OTP is calculated. After setting a new seed, the counter value is reset to 0x00.<br>
<br>
<h3>SET COUNTER</h3>
<pre><code>CLA:  00<br>
INS:  10<br>
P1:   00<br>
P2:   00<br>
P3:   08<br>
data: &lt;8 bytes counter&gt;<br>
<br>
APDU: 00 10 00 00 08 00 00 00 00 00 00 00 01<br>
RESPONSE: 90 00<br>
</code></pre>
<h3>GET COUNTER</h3>
<pre><code>CLA:  00<br>
INS:  11<br>
P1:   00<br>
P2:   00<br>
P3:   00<br>
data: n/a<br>
<br>
APDU: 00 11 00 00 00<br>
RESPONSE: 00 00 00 00 00 00 00 01 90 00<br>
</code></pre>
<h3>PERSONALIZE</h3>
<pre><code>CLA:  00<br>
INS:  12<br>
P1:   &lt;number of digits&gt;<br>
P2:   00 (if TOTP is used), 01 (if HOTP is used)<br>
P3:   14<br>
data: &lt;20 hex bytes seed&gt;<br>
<br>
APDU: 00 12 06 00 14 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36 37 38 39 30<br>
RESPONSE: 90 00<br>
</code></pre>
<h3>GET OATH OTP</h3>
<pre><code>CLA:  00<br>
INS:  13<br>
P1:   00<br>
P2:   00<br>
P3:   00<br>
data: n/a<br>
<br>
APDU: 00 13 00 00 00<br>
RESPONSE: 37 35 35 32 32 34 90 00<br>
</code></pre>
<h3>RESET</h3>
<pre><code>CLA:  00<br>
INS:  14<br>
P1:   00<br>
P2:   00<br>
P3:   00<br>
data: n/a<br>
<br>
APDU: 00 14 00 00 00<br>
RESPONSE: 90 00<br>
</code></pre>
<h3>GET HOTP STATUS</h3>
<pre><code>CLA:  00<br>
INS:  15<br>
P1:   00<br>
P2:   00<br>
P3:   00<br>
data: n/a<br>
APDU: 00 15 00 00 00<br>
RESPONSE: 00 90 00 (if TOTP is used, which is default)<br>
          01 90 00 (if HOTP is used)<br>
</code></pre>
<br /><br /><br />

<h1>Android Application</h1>
<b>The Android application is for demonstration and test purposes only. Do not use in production environments!</b><br /><br />
Please refer to the <a href='https://www.cardsolutions-shop.com/shop/gi-de/'>Mobile Security Developer's Kit</a> for an introduction how to develop Android applications with smart card access.<br />
See <a href='http://code.google.com/p/seek-for-android/source/browse/#svn/trunk/applications/OtpAuthenticator/OtpAuthenticator'>source code</a> for more details