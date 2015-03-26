**NOTE:** The described EAP patch will only work on a **real** Android Smartphone which has an up and running SmartCardAPI with UICC support and PCSC System Service.

# Introduction #
EAP-SIM is a mechanism for authentication and session key generation using a GSM authentication algorithm on client and network side.
EAP-AKA is like EAP-SIM but uses the authentication algorithms on an USIM.

EAP-SIM is specified in [RFC 4186](http://tools.ietf.org/html/rfc4186).

EAP-AKA is specified in [RFC 4187](http://tools.ietf.org/html/rfc4187).

Specifications about EAP can be found [here](http://tools.ietf.org/html/rfc3748) and [here](http://tools.ietf.org/html/rfc5247).

# Details #
An overview how a WLAN with EAP authentication may look like is shown below:

<img src='http://seek-for-android.googlecode.com/svn/wiki/img/EapSim_System.png' width='500' />

To run an EAP-SIM/AKA authentication you will need a client as wpa\_supplicant which has access to a (U)SIM.
The WLAN access point on the network has to support EAP (which is often transcribed as WPA(2)-RADIUS or WPA(2)-ENTERPRISE).
The WLAN AP has to have access to a RADIUS server who handles the authentication and session key generation.
The RADIUS server in a productive environment needs for EAP-SIM/AKA access to the home location register (HLR) of the MNO where the (U)SIMs are registered.
For testing a file with precreated values for authentication is sufficient.

The next picture shows simplified the data flow during the authentication process.

<img src='http://seek-for-android.googlecode.com/svn/wiki/img/EapSim_DataFlow.png' width='600' />

## Changes to Android ##
In order to run EAP-SIM/AKA on an Android smartphone it is necessary to adapt two parts of the sources:
  * the wpa\_supplicant and
  * the Settings.apk
To get access to the (U)SIM it is necessary to enable the PC/SC interface and integrate the SmartCard API with UICC support. <br />
The EAP-SIM/AKA patch was tested against Android 2.3.5\_r1, the [SmartCard API 2.2.2](http://seek-for-android.googlecode.com/files/smartcard-api-2_2_2.tgz)
and the [PCSC System Service 0.9.7](http://seek-for-android.googlecode.com/files/pcsc_system_service_v096.tgz).
### wpa\_supplicant ###
For the wpa\_supplicant the use of the PCSC interface has to be enabled and the functions which encapsulate
the PCSC interface have to be adapted so that they can handle the responses from the SmartCardService,
since the original sources of the wpa\_supplicant expected responses from the (U)SIM according to the T=0 protocol,
but the SmartCardService returns response data according to T=1.
### Settings.apk ###
To give the user the possibility to enable EAP-SIM or EAP-AKA on his Smartphone it is necessary to extend the WiFi Dialog
where it is possible to select the EAP methods.
The WiFi Dialog is located in the package Settings.apk.
Now the entries `SIM` and `AKA` are now available and selectable as EAP-Method.
When selecting `SIM` or `AKA` the other fields like `Phase 2 Authentication`, `CA-Cerificate` and so on can be left empty
or by their default values.
### Patch ###
Download the [EAP-SIM/AKA patch](http://seek-for-android.googlecode.com/files/eap-sim-aka_0.4.1.tar.gz) and extract the content.

Apply the patch with
```
patch -p1 -i eap-sim-aka_0.4.1.patch
```
in the root directory of your Android Sources. Build the system how it is shown in [BuildingTheSystem](http://code.google.com/p/seek-for-android/wiki/BuildingTheSystem).

# Additional Components #

## Compile freeradius with EAP-SIM/AKA support. ##

The EAP-AKA support for freeradius was introduced by a patch for Version 1.1.4. This patch is not available for Version 2 of freeradius server.
We have merged the old EAP-AKA patch and added some more features to the `rlm_sim_files` module so that it can provide the EAP-SIM and EAP-AKA module
with the correct data and keys.

The result is a patch was developed with version 2.1.9 of the freeradius server but it might also work for other 2.1.x versions.

**NOTE:** This patch will only work for EAP-AKA if on the USIM the sequence number check is disabled,
since the AUTN provided by `rlm_sim_files` is constant. Therefore check `EF_AuthAlgo` on your USIM.

On a ubuntu machine get freeradius sources via (first change to the directory where you want to store the sources)
```
sudo apt-get source freeradius
```
Get the build dependencies
```
sudo apt-get build-dep freeradius
```
A freeradius source directory will be created e.g. freeradius-2.1.9-dfsg.

Change the ownership to your development account and the goto the source directory.
```
sudo chown -R <user>:<group> ./freeradius-2.1.9-dfsg
cd freeradius-2.1.9-dfsg
```

Get the [freeradius patch archive](http://seek-for-android.googlecode.com/files/freeradius-2.1.9-dfsg_eap-sim-aka-0.1.tar.gz)
and extract the content.
Apply the patch within the freeradius source directory of freeradius.
You may want to try it first with option `--dry-run` to check if everything will do fine.
```
patch -p2 -i freeradius-2.1.9-dfsg_eap-sim-aka-0.1.patch
```

Build the deb packages:
```
debian/rules binary
```

Install the packages
```
sudo deb -i ../libfreeradius2_2.1.9+dfsg-sch02_i386.deb
sudo deb -i ../freeradius_2.1.9+dfsg-sch02_i386.deb
sudo deb -i ../freeradius-common_2.1.9+dfsg-sch02_all.deb
```

Since the `rlm_sim_files` module is not build by the debian packages routines. It has to be built and copied manually.
Change to and run make
```
cd ./src/modules/rlm_sim_files
make
```
Copy the libraries and create a symbolic link
```
sudo cp ./.libs/rlm_sim_files-2.1.9.so /usr/lib/freeradius
sudo ln -s /usr/lib/freeradius/rlm_sim_files-2.1.9.so /usr/lib/freeradius/rlm_sim_files.so
```

(Re-)Start the freeradius server with:
```
sudo /etc/init.d/freeradius restart
```

## Set up of the freeradius server ##

All configuration files for freeradius are located in `/etc/freeradius` and below.

Add to or change  `clients.conf` that it contains
```
client 192.168.0.0/16 {
    secret    = eap-sim 
    shortname  = eap-sim
}
```

The secret and shortname can be chosen freely, but you have to setup on your WLAN AP the same secret. e.g. on a LinkSys WRT54GL

<img src='http://seek-for-android.googlecode.com/svn/wiki/img/EapSim_LinksysWpaEapSetup.png' width='400' />

In the config directory `/etc/freeradius/modules` create a file `sim_files` with following content:
```
sim_files {
    simtriplets = "/etc/freeradius/simtriplets.dat"
}
```
In the config file `eap.conf` add at the end but before the closing }
```
aka {
    }
sim {
    }
```
In the config file `default` add
```
    sim_files
```
directly before
```
eap {
    ok = return
    }
```

After changes on the configuration files a restart of the server is necessary.

### simtriplets.dat ###
Create the file `/etc/freeradius/simtriplets.dat` with the content from your (U)SIMs you want to use.

Sample of a `simtriplets.dat` file
```
#   IMSI             RAND                             SRES     Kc
SIM,1262074920549791,64BC736EF7684de1921F9C9C0E0679E2,0B7e4e4b,D2119f41D8840400
SIM,1262074920549791,97D0C531F2A84000ACB5E4F966157908,181c8ac1,E2f6976a226bc800
SIM,1262074920549791,1E4FD2861D0848a499C91162234B255C,211056b1,8Bbdd2385B3a0400
#
SIM,0262074920549791,64BC736EF7684de1921F9C9C0E0679E2,0B7e4e4b,D2119f41D8840400
SIM,0262074920549791,97D0C531F2A84000ACB5E4F966157908,181c8ac1,E2f6976a226bc800
SIM,0262074920549791,1E4FD2861D0848a499C91162234B255C,211056b1,8Bbdd2385B3a0400

#   IMSI             RAND                             RES              AUTN                             IK                               CK
AKA,0262073961704408,9FDDC72092C6AD036B6E464789315B78,F553BBC042452202,478412477BFF61DFD5BE5A85664C0820,359CF653FDC8BD365AD32A264811B7EE,CA31C86C64F1C274565CFA7966E2CE0D
```

The first triplet block ist for EAP-SIM on a SIM card, the second block is for EAP-SIM on a USIM card
and the last line contains the values for EAP-AKA of a USIM card.

It is mandatory to have at least 3 different records for one IMSI when it is used for EAP-SIM.

It is not possible to use a single USIM for EAP-AKA and EAP-SIM at the same time.
Only one of the methods may be active, the other method can be disabled by adding a # as prefix.


### agsm ###
The EAP-SIM values can be creates using the tool asgm which kann be found here http://agsm.sourceforge.net/download.html

<img src='http://seek-for-android.googlecode.com/svn/wiki/img/EapSim_AgsmMiscGsmAuth.png' width='300' />