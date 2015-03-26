# Introduction #

OATH is a common standard for OTP password generation defined in [RFC 4226](http://www.ietf.org/rfc/rfc4226.txt). The OTP is unique for each user that personalized the calculation with a unique seed. A counter value is incremented after each calculation (event based OTP) or valid for a specific time (time based OTP).<br><br>
An OATH calculation in software is possible but discouraged as the seed could get lost or tampered after flashing or rooting the phone. For this purpose the OATH calculation takes place in a Secure Element (like an UICC, eSE or Mobile Security Card) where the private seed is kept secure in the GoogleOtpAuthenticator applet.<br>
<br>
<h1>Walk through</h1>
<h2>Install Applet and APK</h2>

<table>
<tr>
<td width='50%' valign='top'>
<ul><li>Download the installation files from the <a href='http://seek-for-android.googlecode.com/files/GoogleOTPAuthenticator.tar.gz'>Download page</a>
</li></ul><ul><li>Install the <code>oath.cap</code> file on the Secure Element with JLoad or other Java Card compliant Global Platform loader tools.<br />
<b>Note</b>: JLoad is included in the <a href='https://www.cardsolutions-shop.com/shop/gi-de/'>Mobile Security Developer's Kit</a>
</li><li>Install the <code>GoogleOtpAuthenticator.apk</code> on your Android device equipped with the Secure Element and <code>SmartCardApi</code> installed.<br />
<b>Note</b>: Without <code>Smarcard API</code>, please check out how to   <a href='http://code.google.com/p/seek-for-android/wiki/BuildingTheSystem'>Building The System</a>
</li><li>Run the application on the Android phone, open the menu and personalize the applet<br>
<b>Note</b>: To scan the OR Code the Barcode Reader Application from zxing is necessary. You can either install it via Google Play or from their project site <a href='http://code.google.com/p/zxing/'>http://code.google.com/p/zxing/</a>.</li></ul>

</td>
<td width='60%'>
<a href='http://code.google.com/p/seek-for-android/wiki/GoogleOtpAuthenticator_1_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/GoogleOtpAuthenticator-1.png' height='300' />
</a>
</td>
</tr>
</table>

<h2>Activate 2-step verification</h2>
<table>
<tr>
<td width='50%' valign='top'>
<ul><li>Log in into your Google account and navigate to the Security Options of your account settings.<br />
</li><li>First it might be necessary to register your subscriber number of your mobile phone via SMS.<br />
</li><li>Now you can activate a mobile application to generate access codes. Click on Android to start the activation.<br />
</li><li>Scan the QR code with Google OTP Authenticator<br />
</li><li>Enter the generated OTP.<br />
</li><li>After successful confirmation the Google OTP Authenticator is now ready to use for 2-step verification with your Google account.<br />
</td>
<td width='60%'>
<a href='http://code.google.com/p/seek-for-android/wiki/GoogleTwoStepConfirmation_0_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/GoogleTwoStepConfirmation-0.png' height='100' />
</a>
<br />
<a href='http://code.google.com/p/seek-for-android/wiki/GoogleTwoStepConfirmation_1_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/GoogleTwoStepConfirmation-1.png' height='100' />
</a>
<br />
<a href='http://code.google.com/p/seek-for-android/wiki/GoogleTwoStepConfirmation_2_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/GoogleTwoStepConfirmation-2.png' height='100' />
</a>
<br />
<a href='http://code.google.com/p/seek-for-android/wiki/GoogleTwoStepConfirmation_3_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/GoogleTwoStepConfirmation-3.png' height='100' />
</a>
</td>
</tr>
</table></li></ul>


<h1>Android Application</h1>

<a href='http://code.google.com/p/seek-for-android/wiki/GoogleOtpAuthenticator_1_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/GoogleOtpAuthenticator-1.png' height='200' />
</a>
<a href='http://code.google.com/p/seek-for-android/wiki/GoogleOtpAuthenticator_2_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/GoogleOtpAuthenticator-2.png' height='200' />
</a>
<a href='http://code.google.com/p/seek-for-android/wiki/GoogleOtpAuthenticator_3_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/GoogleOtpAuthenticator-3.png' height='200' />
</a>
<a href='http://code.google.com/p/seek-for-android/wiki/GoogleOtpAuthenticator_4_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/GoogleOtpAuthenticator-4.png' height='200' />
</a>

<b>The Android application is for demonstration and test purposes only. Do not use in production environments!</b>

<h1>Java Card Applet</h1>
<b>The Java Card applet is for demonstration and test purposes only. Do not use in production environments!</b><br />
The applet within the Java Card CAP file uses the following AIDs:<br>
<pre><code>PackageAID: 0xD2:0x76:0x00:0x01:0x18:0x00:0x03:0xFF:0x49:0x10:0x00:0x89:0x00:0x00:0x02:0x00<br>
Applet AID: 0xD2:0x76:0x00:0x01:0x18:0x00:0x03:0xFF:0x49:0x10:0x00:0x89:0x00:0x00:0x02:0x01<br>
</code></pre>