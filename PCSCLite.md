# Introduction #

PC/SC is a widely used industry standard for accessing smart cards in a computing environment. A free implementation - called PCSC-Lite - supports various platforms so it can easily be ported to Android.
However, PC/SC has disadvantages on mobile phones that will be discussed here.<br /><br />


# Details #

### Plain APDU access for a SE ###
The PC/SC API interface allows sending plain and unfiltered APDU commands to the underlying Secure Element.<br />
Providing such an interface allows the client application to control anything on the Secure Element. This might not be a problem as long as no concurrent access is in place or a malicious application is interfering with the communication channel of another application.
On a mobile phone, the baseband processor (exclusively) occupies the basic channel to the SIM (=channel 0). With the PC/SC interface, it's no problem to mess up with the baseband communication to the GSM context which results in a USIM error until the next reboot.<br /><br />
A Secure Element based on [Java Card technology](http://java.sun.com/javacard/) can have multiple applications installed but only a single application (=Java Card Applet) can be active in a logical channel<br />
Having two Android applications running simultaneously, both requiring different Java Card Applets for secure operations might cause trouble as only one Applet is currently selected when both applications share the same channel.<br /><br />
The concept of **logical channels** is supported by (most) Secure Elements where each offline application can have its own independent communication channel with one dedicated card application. APDU communication has to be filtered (CLA byte with channel information) which is not in the scope of PC/SC today.<br />
The SmartCard API encapsulates the logical channel management and provides a filtered APDU API in contrast to PC/SC.<br /><br />

### PC/SC daemon background task ###
The PC/SC resource manager is a daemon that is running in the background that handles client requests (e.g. connect, transmit, ...) and synchronizes parallel access from multiple applications.<br /><br />
Having this daemon running all the time on a mobile phone requires resources even if no smartcard-aware application is active. Changes within the existing PC/SC lite implementation will create an Android specific branch from the very well maintained code by the PCSC lite developer(s).<br /><br />

### Why is a PC/SC interface required? ###
PC/SC is - as discussed - a widely used standard and a lot of existing native Linux applications have build-in support. WiFi access through **wpa\_supplicant** is an example or OpenSC, ...
When native Android components (wpa\_supplicant, ...) need smart card access the PC/SC standard defines a well known interface so we integrate an PC/SC IFDHandler for Android that connects to the Java-based SmartCard API, thus providing access for native smart card clients.<br /><br />

# Conclusion #
A PC/SC IFDHandler for Android that connects to the Java-based SmartCard API will be integrated in the overall concept, thus providing access for native smart card clients.
Due to security considerations, it needs to be ensured that only system modules can access the PC/SC daemon.


![http://seek-for-android.googlecode.com/svn/wiki/img/PCSC_Concept.png](http://seek-for-android.googlecode.com/svn/wiki/img/PCSC_Concept.png)