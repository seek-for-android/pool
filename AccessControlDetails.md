# Details of the Access Control based security scheme #

To authorize and control the access to a certain Applet on an SE the SmartCard API has to be extended with a security scheme as described as following. Besides the different Applets the SE contains a special Access Control Applet (ACA). This ACA contains a set of Access Conditions (ACs) which can be assigned to certain Android Applications (App). These Access Conditions (ACs) define how an Android application can access a certain Applet. Moreover the ACA includes a Certificate List (CL) which contains all certificates of the Providers who installed an Applet on the SE (shortened with AplCerts). As common each Android Application (App) is signed by its provider and contains its signature as well as the App provider’s certificate which is needed by the Package installer for the App signature verification during the installation process.

<br><br>

<img src='http://seek-for-android.googlecode.com/svn/wiki/img/SecuritySchemeOverview_wl.png' />

<br><br>

<h2>The security scheme works in following steps:</h2>
<p>
1. The Applet Provider installs its Applet on the Secure Element (SE). To define the access privileges to the installed Applet some entries has to be made within preinstalled Access Control Applet (ACA). Firstly the Certificate List has to be updated with the applet provider’s certificate if not existing there yet. Thus the SmartCard API can verify if an App certificate was signed by an Applet Provider. Moreover the Applet Provider can assign different Access Conditions to each Android Application to restrict the access to the Applet. Therefore to each defined Access Condition an Android Application identifier is stored in an Access Condition List. The unique identifier is a SHA-1 hash value of the Android Application’s certificate. To associate the<br>
defined Access Conditions and Applet Provider certificates with the corresponding Applet an entry has to be added to the AID List in the ACA which contains a set of Applet AIDs. These AIDs are bound to stored certificates and Access Conditions.<br>
</p>
<p>
2. The Android Application Provider deploys its Application to the Android Marked. Therefore he signs it with its private key. If an access to a certain Applet on the SE is needed the Android Application Provider’s certificate has to be signed by the corresponding Applet provider. The Android Application Provider’s certificate is finally added to the Android Application (App) container before the application is signed and deployed.<br>
</p>
<p>
3. The Android owner installs the Android Application (App) via the Android Package Installer into her/his Android system. During the installation process the App signature is verified with its including certificate to assure its integrity. (It has to be kept in mind that an installed Android Application can not be modified in any way because of the lack of write permissions. This means there is no need to check the<br>
integrity any more within the Android system).<br>
</p>
<p>
4. After the successful installation into the Android system the Android Application can be started by the user to perform an action. </p>
<p>
5. If the Android Application tries to access the Secure Element (SE) the SmartCard API checks the authorization and access conditions before granting an access.<br>
</p>
<p>
6. The SmartCard API verifies the App certificate which is included within the App container. Therefore the SmartCard API fetches the applet provider certificate (AplCert) from the Certificate List (CL) on the Access Control Applet (ACA) provided a certificate can be found an. If no certificate exists the App certificate check will be omitted. </p>
<p>
7. After a successful verification of the App Certificate with the help of the Applet Provider Certificate the access to the Secure Element (SE) is granted by the SmartCard API according to the access conditions stored in the ACA. Therefore the SmartCard API fetches the access conditions which are assigned to the asking Android Application. This is done by referencing these with the App identifier consisting of the SHA-1 hash value of the App certificate. Finally the SmartCard API evaluates the received access conditions and applies these as access filter. If the access conditions to the asking App can not be found in the ACA the access is completely denied.<br>
</p>
<p>
8. Finally the Android application is authorized and can perform operations on the referenced Applet within the Secure Element (SE) according to the policies defined in the access conditions for this SE Applet or Android Application.<br>
</p>