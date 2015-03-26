#### Introduction ####
  * [SmartCard API](SmartcardAPI.md)<br>An introduction about the architecture and concept of the SmartCard API for the Android platform<br><br>
<ul><li><a href='SecurityConcept.md'>Security Concept</a><br>Read about the system security of the SmartCard API integration in the Android platform<br><br>
</li><li><a href='Devices.md'>Device Support</a><br>(Incomplete) list of Android devices that have SmartCard API support<br><br>
</li><li><a href='VersionHistory.md'>Version History</a><br>Summary of the release notes of all SmartCard API versions<br><br></li></ul>

<h4>How To</h4>
<ul><li><a href='BuildingTheSystem.md'>BuildingTheSystem</a><br>For details on how to compile the Android sources with SmartCard API support<br><br>
</li><li><a href='EmulatorExtension.md'>Emulator Extension</a><br>Extend the Android emulator with a PC/SC and ASSD interface that enables access to a physical connected SIM and/or ASSD card<br><br>
</li><li><a href='WebScapi.md'>Browser based SmartCard API</a><br>Adds support of the OpenMobile API for the Android Browser<br><br>
</li><li><a href='UsingSmartCardAPI.md'>Using the SmartCard API</a><br>Writing Android applications with access to Secure Elements using the SmartCard API<br><br>
</li><li><a href='UsingCTS.md'>Using CTS</a><br>Extension for the Compatibility Test Suite (CTS) to be used SmartCard API verification<br><br>
</li><li><a href='AddonTerminal.md'>Create an AddonTerminal</a><br>SmartCard API and MSC SmartcardService can be extended with additional Secure Element (readers). This tutorial explain the details<br><br></li></ul>

<h4>Access Control</h4>
<ul><li><a href='AccessControlIntroduction.md'>Introduction</a><br>General introduction about the Access Control Scheme provided by the SmartCard API<br><br>
</li><li><a href='AccessControlDetails.md'>Details</a><br>Technical description about the Access Control implementation<br><br>
</li><li><a href='AccessControlBenefits.md'>Benefits</a><br>Open discussion about the benefits of providing an access control scheme for the SmartCard API<br><br></li></ul>

<h4>UICC Support</h4>
<ul><li><a href='UICCSupport.md'>AT Command Extensions</a><br>Specification of the AT Command Interface required for the SmartCard API support on real devices<br><br>
</li><li><a href='SmartcardWebserver.md'>Smart Card Web Server</a><br>Browse data located on the UICC with BIP extensions in the Android SIM Toolkit framework<br><br>
</li><li><a href='EapSimAka.md'>EAP-SIM/AKA</a><br>Enables Android Smartphones to authenticate in a WLAN with behalf of EAP-SIM/AKA protocol<br><br></li></ul>

<h4>ASSD Support</h4>
<ul><li><a href='Concept.md'>Concept</a><br>Description of the ASSD concept<br><br>
</li><li><a href='HowToBuild.md'>How to Build</a><br>Description how to integrate ASSD into the system<br><br></li></ul>

<h4>MSC Support</h4>
<ul><li><a href='MscSmartcardService.md'>MSC_SmartcardService</a><br>Installable MSC SmartcardService for Android phones without flashing a new system image<br><br>
</li><li><a href='MscPluginTerminal.md'>MSC_PluginTerminal</a><br>Installable MSC PluginTerminal for Android phones where the SmartCard API is already available but not MSC Terminal included<br><br>
</li><li><a href='MscVersionHistory.md'>Version History</a><br>Summary of the release notes of all MSC SmartcardService versions<br><br></li></ul>


<h4>PC/SC Interface</h4>
<ul><li><a href='PCSCLite.md'>PCSC-Lite Discussion</a><br>Discussion about the integration of the PC/SC interface in the SmartCard API stack for native code<br><br>
</li><li><a href='PCSCSmartCardServiceIntro.md'>PC/SC Support</a><br>PC/SC Lite system service integration in Android for native clients<br><br></li></ul>

<h4>Security Interface</h4>
<ul><li><a href='SmartCardPKI.md'>PKI Support on Android</a><br>Integration of a smart card based, standard PKI interface into  Android<br><br></li></ul>

<h4>Applications</h4>
<ul><li><a href='GoogleOtpAuthenticator.md'>GoogleOtpAuthenticator</a><br>See an example with a OATH application running on the Secure Element and the Android application is displaying the OTP calculated by the card for 2-step verification on a Google account.<br><br>
</li><li><a href='AndroidContainer.md'>LXC container for Android</a><br>Run multiple isolated Android user-space instances in LXC containers on a physical device within a (minimal) Debian environment<br><br>
</li><li><a href='AndroidBelgiumEid.md'>AndroidBelgiumEid</a><br>The COSIC Research Group within the Katholieke Universiteit Leuven, Belgium, developed a reference implementation for the Belgium eID card on the Android platform with the Mobile Security Card. (<a href='http://www.esat.kuleuven.be/cosic/'>K.U.Leuven, ESAT/COSIC</a>)<br><br>
</li><li><a href='BTPCSC.md'>BTPCSC</a><br>Enables Android phones to be used as regular PC/SC smartcard readers vis Bluetooth.<br><br>
</li><li><a href='SecureFileManager.md'>SecureFileManager</a><br>A filemanager for the Android platform to cipher files on the Mobile Security Card with a smart card applet.<br><br></li></ul>


<h4><a href='Faq.md'>FAQs</a></h4>
<h4><a href='Abbreviations.md'>Abbreviations</a></h4>