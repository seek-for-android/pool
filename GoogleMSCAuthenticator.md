# Introduction #

In <a href='http://googleenterprise.blogspot.com/2010/09/more-secure-cloud-for-millions-of.html'>this press release</a>, Google announced a new two-step verification using one-time passwords. The <a href='http://code.google.com/p/google-authenticator/'>Google Authenticator</a> is an application for mobile phones that allows Gmail users to use this two-step verification to log in using their mobile phones.

Seeing as Android supports smartcards such as the Mobile Security Card through the <a href='http://code.google.com/p/seek-for-android/wiki/SmartcardAPI'>SmartcardAPI</a>, an application similar to the Google Authenticator would be useful that generates the one-time passwords on a smartcard using a JavaCard applet. A simple application that generates OTPs was developed and uploaded to the seek-for-android project a while ago. (See <a href='http://code.google.com/p/seek-for-android/wiki/OtpAuthenticator'>OTPAuthenticator</a>)

However, this version was not compatible to the Google OTP verification, it supported neither time-based OTPs nor the automatic personalisation through QR codes. Therefore, a new application, the Google MSC Authenticator, based on the original OTPAuthenticator, was developed. It is to be regarded as merely a proof of concept for the time being and is only to be used for the purpose of testing.

# Background information on OATH #

OATH is a common standard for OTP password generation defined in [RFC 4226](http://www.ietf.org/rfc/rfc4226.txt). The OTP is unique for each user that personalized the calculation with a unique seed. A counter value is incremented after each calculation (event based OTP) or valid for a specific time (time based OTP).<br><br>
An OATH calculation in software is possible but discouraged as the seed could get lost or tampered after flashing or rooting the phone. For this purpose the OATH calculation takes place in the Mobile Security Card where the private seed is kept secure in the OtpAuthenticator applet.<br>
<br>
<h1>Prerequisites</h1>

If you wish to test the Google MSC Authenticator, you will require:<br>
<ul><li>An Android phone with smartcard support and a Mobile Security Card (see <a href='http://code.google.com/p/seek-for-android/wiki/'>BuildingTheSystem</a>)<br>
</li><li>A Gmail OTP account<br>
</li><li>A way to install applets on a JavaCard</li></ul>

<h1>Walkthrough</h1>

<b>The Android application is for demonstration and test purposes only. Do not use in production environments!</b>

<ul><li>Download the installation files from the <a href='http://seek-for-android.googlecode.com/files/otpauth.tar.gz'>Download page</a>
</li><li>Install the <code>oath.cap</code> file on the Mobile Security Card with JLoad or other Java Card compliant Global Platform loader tools.<br>
<b>Note</b>: JLoad is included in the <a href='https://www.cardsolutions-shop.com/shop/gi-de/'>Mobile Security Developer's Kit</a>
</li><li>Install with <code>OtpAuthenticator.apk</code> on your Android device equipped with a Mobile Security Card and <code>MSC_SmartcardService</code> installed.<br>
<b>Note</b>: Without <code>MSC_SmartcardService</code>, please check out the <a href='http://code.google.com/p/seek-for-android/source/browse/#svn/trunk/applications/OtpAuthenticator/OtpAuthenticator'>OtpAuthenticator source code</a> and recompile the APK according to <a href='http://code.google.com/p/seek-for-android/wiki/BuildingTheSystem'>BuildingTheSystem</a>
</li><li>Log into your Gmail OTP account, click "Change sign-in verification", make sure "Two-step verification" is enabled, click "Configure your mobile application" and "secret key". A QR code will be displayed.<br>
</li><li>Run the application on the Android phone, open the menu and click "Scan code". Use your phone's camera to scan the QR code displayed on the website. This will initialise the applet with the secret key that it needs to generate the passwords.<br>
</li><li>To test the generated passwords: Log out of your Gmail OTP account and log in again. You should be asked for an OTP in the second step of the two-step verification. Start the Google MSC Authenticator application and enter the code that is being displayed. It is valid for about one minute at most.<br>
<b>Note</b>: Gmail currently uses time-based OTPs. This means your mobile phone's time settings must be accurate. A time difference of only one minute is usually enough for the two-step authentication to fail. The best way to ensure your time is set accurately is to use network-provided time, which can be enabled in your phone's "Date & time settings".</li></ul>

<img src='http://seek-for-android.googlecode.com/svn/wiki/img/OtpAuthenticator.png' />

Please refer to the <a href='https://www.cardsolutions-shop.com/shop/gi-de/'>Mobile Security Developer's Kit</a> for an introduction how to develop Android applications with smart card access.<br />
See <a href='http://code.google.com/p/seek-for-android/source/browse/#svn/trunk/applications/GoogleMSCAuthenticator/'>source code</a> for more details.