# Introduction #

The SmartCard API provides an APDU interface for Secure Elements thus every Android application has full access to each applet. This causes no danger in a restricted environment but the open approach of smartphones can danger the Secure Element if malicious applications are (unintentionally) installed.<br />
Providing a new Android SMARTCARD permission for Secure Element access gives the user the possibility to block applications from being installed but there is no fine grained control over which Android application can access which Secure Element applet.<br />
The Access Control Scheme will overcome this gap by providing
  * Access control based on the client certificate for each Secure Element application
  * APDU filter masks based on the client certificate to differentiate the list of allowed APDU commands
  * Interoperability: based on open standards the access control scheme can be deployed with any Java Card based Secure Element on the market
  * Secure over the air update mechanism based on Global Platform Secure Messaging
  * Access control is enabled or disabled depending on the configuration of each Secure Element separately
  * Support of multiple ACA profiles to support multiple ACD owners
  * Easy to implement with small memory footprint on the Secure Element
<br />

## Access Control ##
Without access control an application could easily block the SIM card by sending wrong VERIFY PIN commands or block the Card Manager with wrong EXTERNAL AUTHENTICATE commands on purpose.<br />
To omit such problems it is required to have the ACE in place to distinguish between an official application where full access to the applet is granted based on the certificate whereas third-party applications might only be allowed to use the same applet.<br />
All access control checks are transparently handled by the ACE thus the client application does not know anything about access control.<br />

## APDU filter mask ##
Beside the certificate checks when a channel is established to grant or deny access the ACE verifies each APDU command with the APDU filter mask to ensure only allowed commands are sent from the client application.<br />
With this check it is possible that the SE applet issuer is having full management access to his applet with all possible APDU commands whereas other client applications can only use read-only functionality.<br />

## Interoperable ##
The ACD of the access control scheme is stored in a Java Card applet, ACA, to ensure that each Secure Element ranging from SIM over eSE to MicroSD cards, ... can be supported in the same way.<br />
With Global Platform mechanisms to access and update the ACD (GET DATA & STORE DATA) a standardized and interoperable APDU interface can be used for the ACA.<br />

## Updates ##
It is critical to provide a secure update mechanism when ACD is modified over the air while the Secure Element is in the field. An applet with a Global Platform interface can ensure that is has received all update blocks before using the new access rule by supporting a transaction safe interface.<br />
OTA updates of a MicroSD card or embedded SE is not possible with baseband processor functionality as in the SIM case thus the access control scheme should provide the same update functionality for any Secure Element.<br />

## Enabling / disabling ##
Access control limits the possibilities for application developers to reuse SE security functionality thus the access control mechanism should also be flexible on which Secure Element is enabled or not. If the ACA is not installed on the SE, access control is disabled, if an ACA is installed the access rules are enforced. With this approach it is possible to i.e. have SIM card with strict access control enabled and a MicroSD card with full access in the same phone.<br />
Each Secure Element defines its own ACD.<br />

## Multiple profiles ##
It might not always be possible in the field to only have a single ACA installed on a SE and have only one owner who can update the ACD. This access control scheme supports multiple profiles where a master ACA acts as the interface to the handset but retrieves the ACD from multiple different client ACAs. Each ACA owner can update its ACD individually and the card issuer still controls the master ACA.<br />

## Efficiency ##
Additional overhead is required to check the ACD first before granting the access to a SE applet as the data cannot be cached without missing OTA updates. However, the ACA can return a _not modified_ code when the ACE retrieves the same ACD again to speed up the communication.<br />
The ACA can dynamically allocate the required buffer to store the ACD thus it is not required to allocate the complete memory initially.