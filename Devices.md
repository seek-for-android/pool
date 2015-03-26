Android is running on different devices from various manufacturers. With the permanent increasing amount it is impossible to keep track of all activities. This overview should list the devices and configuration that was tested successfully with SmartCard API support.<br />
**System manufacturers & SEEK-followers**: please provide feedback to keep this list up to date!<br />

### Development devices ###
The following devices were used for SmartCard API development and internal testing according to [Building the System](BuildingTheSystem.md)

| **Device** | | **ASSD support** | **UICC support** | **eSE support** | | **Notes** |
|:-----------|:|:-----------------|:-----------------|:----------------|:|:----------|
| ADP1 (dream) |  | 1.1.0 | - | n/a |  | not supported anymore, last release SCAPI 1.1 |
| ADP2 (sapphire) |  | 1.2.0 | - | n/a |  | not supported anymore, last release SCAPI 1.2 |
| Nexus One (passion) |  | 2.3.0 | - | n/a |  | not supported anymore, last release SCAPI 2.3.0 |
| Nexus S (crespo) |  | n/a | 2.4.0 _(1)_ | 2.4.0 _(2)_ |  | not supported anymore, last release  SCAPI 2.4.0 |
| Galaxy Nexus (maguro) |  | n/a | - | 3.0.0 _(2)_ |  | reference for eSE development |
| Xoom (wingray) |  | 2.4.0 | - | n/a |  | not supported anymore, last release  SCAPI 2.4.0 |

_note.1_ required baseband firmware & RIL files are not available in public!<br />
_note.2_ eSE secure messaging keys are not available in public!<br />

The Android emulator is used as reference development environment!
<br />

### Commercial devices ###
The following devices summarizes all commercially available devices that have SmartCard API integrated

| **Device** | | **OS build** | **BB build** | **SCAPI** | | **ASSD** | **UICC** | **eSE** | | **Notes** |
|:-----------|:|:-------------|:-------------|:----------|:|:---------|:---------|:--------|:|:----------|
| Samsung GT-I9100P |  | <font size='1'>GINGERBREAD.XXLA3 <table><thead><th> <font size='1'>I9100PXXKI3 </th><th> 2.2.2 </th><th>  </th><th> - </th><th> + </th><th> - </th><th>  </th><th> <a href='DeviceDetails.md'>details</a> </th></thead><tbody>
<tr><td> Samsung GT-I9300 </td><td>  </td><td> <font size='1'>IMM76D.I9300XXALEF </td><td><font size='1'>I9300XXLEF </td><td> 2.3.2 </td><td>  </td><td> - </td><td> + </td><td> - </td><td>  </td><td> <a href='DeviceDetails.md'>details</a> </td></tr>
<tr><td> Sony Xperia S (LT26i) </td><td>  </td><td> <font size='1'>6.0.A.3.75 </td><td> <font size='1'>M8660-AAABQOLYM-314005T </td><td> 2.3.4 </td><td>  </td><td> - </td><td> + </td><td> - </td><td>  </td><td> <a href='DeviceDetails.md'>details</a> </td></tr>
<tr><td> Sony Xperia P (LT22i) </td><td>  </td><td>  </td><td>  </td><td> 2.3.4 </td><td>  </td><td> - </td><td> + </td><td> - </td><td>  </td><td> reported by Sony </td></tr>
<tr><td> Sony Xperia U (ST25i) </td><td>  </td><td>  </td><td>  </td><td> 2.3.4 </td><td>  </td><td> - </td><td> + </td><td> - </td><td>  </td><td> reported by Sony </td></tr>
<tr><td> Sony Xperia Sola (MT27i) </td><td>  </td><td>  </td><td>  </td><td> 2.3.4 </td><td>  </td><td> - </td><td> + </td><td> - </td><td>  </td><td> reported by Sony </td></tr>
<tr><td> Sony Xperia Go (ST27i) </td><td>  </td><td>  </td><td>  </td><td> 2.3.4 </td><td>  </td><td> - </td><td> + </td><td> - </td><td>  </td><td> reported by Sony </td></tr></tbody></table>

<i>note</i> SCAPI-2.3.4 is based on 2.3.2 with internal adaptations from Sony<br>
<br>
<br>
<br><br>
<h3>MSC SmartcardService</h3>
All devices with SD slot and transparent access to the SD flash memory are supported by the MSC SmartCardService or the MscPluginTerminal.<br />
The following devices were reported not to work due to the missing direct access to the flash memory<br>
<br>
<table><thead><th> <b>Device</b> </th><th> </th><th> <b>Model</b> </th><th> <b>OS</b> </th><th> <b>OS build</b> </th><th> </th><th> <b>Notes</b> </th></thead><tbody>
<tr><td> Motorola Pro+ </td><td>  </td><td> MB632 </td><td> 2.3.5 </td><td> 5.51Q-110_ELW-TA-21 </td><td>  </td><td> reported by G&D SFS </td></tr>
<tr><td> Motorola Droid </td><td>  </td><td>  </td><td>  </td><td>  </td><td>  </td><td> reported by G&D SFS</td></tr>
<tr><td> Motorola Atrix </td><td>  </td><td>  </td><td>  </td><td>  </td><td>  </td><td> reported by G&D SFS </td></tr></tbody></table>

<i>note</i>: On devices where the SmartCard API is already installed, the MSC SmartcardService cannot be installed again. In such case, the MscPluginTerminal has to be used instead.