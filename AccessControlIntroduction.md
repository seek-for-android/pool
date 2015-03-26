# Introduction #

<table>
<tr>
<td width='35%' valign='top'>
Since Version 2.4.0 the SmartCard API supports access control specified by Global Platform. It is a standard that enables several parties to independently and securely manage their stakes in a single Secure Element. It can be ensured that only allowed Android applications are able to access specific Java Card applets depending on the device APK certificate.<br>
<br>
An additional APDU filter scheme (based on an APDU whitelist) is established to have a more detailed control over the access mechanism of an application.<br>
<br>
The implementation relies on an extension of the handset integration in combination with a access rule application running on the Secure Element.<br>
<br>
The specification can be found at Global Platform:<br>
<a href='http://www.globalplatform.org/specificationsdevice.asp'>Secure Element Access Control</a>
</td>
<td width='5%'>
</td>
<td width='60%'>
<a href='http://code.google.com/p/seek-for-android/wiki/ARA_overview_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/ARA_overview.png' height='300' />
</a>
<br />
Click to enlarge<br>
</td>
</tr>
</table>

## Components ##

### Access Control Enforcer ###
The Access Control Enforcer (ACE) is the module that is integrated in the SmartCard API, see `org.simalliance.openmobileapi.service. security` in the SmartcardService package.<br />
The module reads the access rule from the ARA-M according to the applications certificate and AID of the applet to be accessed when a communication channel is opened. Access is either granted according to access rule or denied if no rule is found.<br />
When the communication channel is established all APDU transfer is checked against the APDU filter list if available.<br />
No access policy is stored in the ACE itself, all data is read from the Secure Element, the ARA-M.

### Access Rule Application ###
The Access Rule Application (ARA) on the Secure Element stores the Access Rules. The ARA consists at least of the unique Access Rule Application Master (ARA-M) and may contain several Access Rule Application Clients (ARA-C). All Access Rules stored in the ARA are accessed by the Access Control Enforcer via the ARA-M.

### Algorithm for Applying Rules ###
The Access Control enforcer retrieves the rules that shall be applied by checking for rules associated
with the device’s certificate according to the algorithm defined below. If the AR is not found within the
cache of the the Access Control enforcer, it may have to issue several ```
GET DATA  [specific]``` commands to the ARA-M
to ensure that the right rule is retrieved.
The Access Control enforcer uses the following algorithm to retrieve an access rule for the device application
(identified by its certificate) and the SE application (identified by its AID):
  1. Search for a rule that is specific to the device application and to the SE application with AID:
```
SearchRuleFor(DeviceApplicationCertificate, AID) 
```
> > If a rule exists, then apply this rule and stop the rule search.


> 2)  If  no rule fits condition A:  Search for a rule that applies to all device applications not otherwise covered by a specific rule and is specific to the SE application with AID:
```
SearchRuleFor(<AllDeviceApplications>, AID)
```
> > If a rule exists, then apply this rule and stop the rule search.


> 3)  If no rule fits condition A or B:  Search for a rule that is specific to the device application and applies to all SE applications not otherwise covered by a specific rule :
```
SearchRuleFor(DeviceApplicationCertificate, <AllSEApplications>) 
```
> > If a rule exists, then apply this rule and stop the rule search.


> 4)  If no rule fits condition A, B, or C:  Search for a rule that applies to all device applications and to all SE applications not otherwise covered by a specific rule :
```
SearchRuleFor(<AllDeviceApplications>, <AllSEApplications>) 
```
> > If a rule exists, then apply this rule.

### Access Rule ###
The Access Rule (AR) is stored in the ARA and can be updated over the air with standardized Global Platform Secure Messaging or Remote Applet Management functionality.<br />
The AR consists of a set of data objects (DO). The AR is identified by the AID of the applet to be accessed (AID-REF-DO) and the hash (SHA-1) of the applications certificate (Hash-REF-DO).<br /> The Access Rule DO contains an APDU access rule (APDU-AR-DO) and/or a NFC access rule (NFC-AR-DO). <br />The APDU-AR-DO contains either a general flag (allowed / not allowed) or an APDU filter list. An APDU filter consists of 4-byte APDU header and a 4-byte APDU filter mask. An APDU filter is applied to the header of the APDU being checked as follows:
```
if((APDUHeader & APDU_filter_mask) == APDU_filter_header) 
   then allow APDU.
```
The NFC-AR-DO contains an NFC event access rule flag (allowed / not allowed).
<br /><br />

## How does it work ##
Scenario: a client application wants to communicate with a secure element application.
<table>
<tr>
<td width='40%' valign='top'>
<a href='http://code.google.com/p/seek-for-android/wiki/ARA_diagram_png'>
<img src='http://seek-for-android.googlecode.com/svn/wiki/img/ARA_diagram.png' width='374' height='357' />
</a>
<br />
Click to enlarge<br>
</td>
<td width='15'>
</td>
<td valign='top'>
1. A client application signed with a unique key tries to access a specific application through its AID on a Secure Element<br />
2. ACE reads the AR for the specific AID and the applications certificate hash.<br />
3. Grant access to the client application according to the access rule or deny access if no rule is found.<br />
4. Client application can communicate with the SE applet if the command APDUs match the filter list (if given) checked by the ACE<br />
</td>
</tr>
</table>