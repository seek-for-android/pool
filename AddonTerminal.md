# Introduction #

The SmartCard API provides the possibility to add additional terminals without re-flashing the system. The concept is supported within the _smartcard-api.patch_ system integration as well as the _MSC_ _SmartcardService_.<br />
This concept allows a post installation of Secure Element terminals like a Bluetooth, USB smart card reader or any other Secure Element connected to the device.<br />


# Requirements #
The installation of an Addon Terminal requires the following conditions:
  * the name of the package is starting with `org.simalliance.openmobileapi.service.terminals.`
  * implementation of the complete interface is required with the correct signature
  * class name within the correct package has to end with `Terminal`
  * the package has to get registered before the SmartCard API process is running<br />


# Sample #
The sample code referenced here can also be found in the SVN repository.
  * Using the Eclipse project wizard, create a new Android project
> > _File -> New -> Project_
  * Select _Android Project_ and click _Next_
  * To create the project, fill in the required fields
> > Project name: _PluginTerminal_<br />
> > Application name: _PluginTerminalSample_<br />
> > Package name: _org.simalliance.openmobileapi.service.terminals.SamplePluginTerminal_<br />
> > Create Activity: _deselect_
  * Click _Finish_ to create the body of the (empty) Android application<br />

Create a new class in the default namespace called `DummyTerminal`
  * Using the Eclipse project wizard, create a new Java class
> > _File -> New -> Class_
  * To create the class, fill in the required fields
> > Package: _org.simalliance.openmobileapi.service.terminals.SamplePluginTerminal_<br />
> > Application name: _PluginTerminal_
  * Click _Finish_ to create the body of the (empty) Android application<br />

Implement the required public method:
  * `public byte[] getAtr();`
  * `public String getType();`
  * `public boolean isCardPresent();`
  * `public void internalConnect();`
  * `public void internalDisconnect();`
  * `public byte[] getSelectResponse();`
  * `public byte[] internalTransmit(byte[] command);`
  * `public int internalOpenLogicalChannel();`
  * `public int internalOpenLogicalChannel(byte[] aid);`
  * `public void internalCloseLogicalChannel(int iChannel);`

Install the APK after ensuring that the SmartCard API Service is not running - e.g. after a reboot or kill the process manually:
```
# ps
...
app_130   10737 1025  177176 15052 ffffffff afd0ebd8 S org.simalliance.openmobileapi.service:remote
...
# kill 10737
```
Run the _OpenMobileApiSample_ application, _PerformanceTester_ or similar to check the Addon Terminal is properly installed.<br />


# Details #
The SVN [PluginTerminal](http://code.google.com/p/seek-for-android/source/browse/#svn%2Ftrunk%2Fsamples%2FPluginTerminal) sample can be used to study the functionality of the Addon Terminal implementation.
The PluginTerminal sample is a dummy terminal that uses a mock card.

## Access Control ##
An implementation of an Addon Terminal must consider some characteristics required
for smooth cooperation with the Access Control Enforcer.
The Access Control Enforcer tries to select an Access Control Applet on the particular secure element in order to retrieve access rules.
It's not required that such an applet is present on the secure element, but in that case the Acces Control Enforcer expects the terminal to throw a `NoSuchElementException`.
This behaviour must be implemented in method `internalOpenLogicalChannel(byte[] aid)` (see sample code).

Also the Addon Terminal should check the caller application to assert that the caller is entitled to use the Addon Terminal (see sample code).

## Log of sample Addon Terminal _DummyTerminal_ ##
The logcat output of the sample Addon Terminal _DummyTerminal_ after running the [OpenMobileApiSample](http://code.google.com/p/seek-for-android/source/browse/#svn%2Ftrunk%2Fsamples%2FOpenMobileApiSample) application will show
```
V/DummyTerminal(  819): internalConnect
V/DummyTerminal(  819): internalOpenLogicalChannel: AID = D2 76 00 01 18 AA FF FF 49 10 48 89 01
V/DummyTerminal(  819): internalOpenLogicalChannel: default applet
V/DummyTerminal(  819): internalDisconnect
V/DummyTerminal(  819): internalConnect
V/DummyTerminal(  819): internalTransmit: 80 CA 9F 7F 00
V/DummyTerminal(  819): internalOpenLogicalChannel: AID = D2 76 00 01 18 AA FF FF 49 10 48 89 01
V/DummyTerminal(  819): internalOpenLogicalChannel: default applet
V/DummyTerminal(  819): internalOpenLogicalChannel: default applet
V/DummyTerminal(  819): internalTransmit: 81 CA 9F 7F 00
V/DummyTerminal(  819): internalCloseLogicalChannel: 0
V/DummyTerminal(  819): internalCloseLogicalChannel: 1
V/DummyTerminal(  819): internalDisconnect
V/DummyTerminal(  819): internalConnect
V/DummyTerminal(  819): internalOpenLogicalChannel: AID = D2 76 00 01 18 AA FF FF 49 10 48 89 01
V/DummyTerminal(  819): internalOpenLogicalChannel: default applet
V/DummyTerminal(  819): internalDisconnect
V/DummyTerminal(  819): internalConnect
V/DummyTerminal(  819): internalTransmit: 00 A4 04 00 08 A0 00 00 00 03 00 00 00 00
V/DummyTerminal(  819): internalTransmit: 80 CA 9F 7F 00
V/DummyTerminal(  819): internalOpenLogicalChannel: AID = D2 76 00 01 18 AA FF FF 49 10 48 89 01
V/DummyTerminal(  819): internalOpenLogicalChannel: default applet
V/DummyTerminal(  819): internalOpenLogicalChannel: AID = A0 00 00 00 03 00 00 00
V/DummyTerminal(  819): internalOpenLogicalChannel: default applet
V/DummyTerminal(  819): internalTransmit: 81 CA 9F 7F 00
V/DummyTerminal(  819): internalTransmit: 00 A4 04 00 00
V/DummyTerminal(  819): internalTransmit: 00 A4 04 00 0D D2 76 00 01 18 AA FF FF 49 10 48 89 01 00
V/DummyTerminal(  819): internalCloseLogicalChannel: 0
V/DummyTerminal(  819): internalCloseLogicalChannel: 1
V/DummyTerminal(  819): internalDisconnect
```
<br />
Methods to be implemented:
#### public byte[.md](.md) getAtr() ####
Return the ATR if available or `null` if not present

#### public String getType() ####
Returns the logical Secure Element type (USB, Bluetooth...), that will be completed with an index (assigned by the system) to form the name.

#### public boolean isCardPresent() ####
Returns `true` if the SE is available, otherwise false

#### public void internalConnect() ####
Called before any other method is used for the first time. Initial setup procedures should be placed here like power on or similar

#### public void internalDisconnect() ####
Called when the SE is not used by clients anymore. Shutdown or cleanup procedures should be placed here like power off or similar

#### public byte[.md](.md) getSelectResponse() ####
Returns the applet SELECT response after calling openLogicalChannel(AID) or openBasicChannel(AID) if available

#### public byte[.md](.md) internalTransmit(byte[.md](.md) command) ####
Returns the response APDU or the corresponding command APDU

#### public int internalOpenLogicalChannel() ####
Returns the channel number after opening a logical channel without an AID selection. Selected applet is the current default applet on the corresponding logical channel

#### public int internalOpenLogicalChannel(byte[.md](.md) aid) ####
Returns the channel number after opening a logical channel with an AID selection. Selected applet is referenced by the AID.

#### public void internalCloseLogicalChannel(int iChannel) ####
Called when a channel gets closed (basic channel as well as logical channel). In case of a logical channel (iChannel > 0) the channel should be closed on the Secure Element
<br /><br /><br />