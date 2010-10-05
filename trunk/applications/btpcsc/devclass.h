/*****************************************************************
/
/ File   :   bt_pcsc_config.h
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   October 5, 2010
/ Purpose:   Provides functions to convert device class information
/            into human-readable text.
/
******************************************************************/

#ifndef DEVCLASS_H
#define DEVCLASS_H

#define COD_LimitedDiscoverableMode	0x00002000
#define COD_Networking			0x00020000
#define COD_Rendering			0x00040000
#define COD_Capturing			0x00080000
#define COD_ObjectTransfer		0x00100000
#define COD_Audio			0x00200000
#define COD_Telephony			0x00400000
#define COD_Information			0x00800000
#define COD_ServiceAny			0x00FFE000
#define COD_Invalid			0x00000000

#define COD_Major_Computer		0x00000100
#define COD_Major_Phone			0x00000200
#define COD_Major_Lan_Access_Point	0x00000300
#define COD_Major_Audio			0x00000400
#define COD_Major_Peripheral		0x00000500
#define COD_Major_Unclassified		0x00001F00
#define COD_Major_Misc			0x00000000

#define COD_Minor_Comp_Palm		0x00000014
#define COD_Minor_Comp_Laptop		0x0000000C
#define COD_Minor_Comp_Desktop		0x00000004
#define COD_Minor_Comp_Server		0x00000008
#define COD_Minor_Comp_Handheld		0x00000010
#define COD_Minor_Comp_Unclassified	0x00000000

#define COD_Minor_Phone_Smart		0x0000000C
#define COD_Minor_Phone_Cellular	0x00000004
#define COD_Minor_Phone_Cordless	0x00000008
#define COD_Minor_Phone_Modem		0x00000010
#define COD_Minor_Phone_Unclassified	0x00000000

#define COD_Minor_Lan_17		0x00000020
#define COD_Minor_Lan_33		0x00000040
#define COD_Minor_Lan_50		0x00000060
#define COD_Minor_Lan_67		0x00000080
#define COD_Minor_Lan_83		0x000000A0
#define COD_Minor_Lan_99		0x000000C0
#define COD_Minor_Lan_NoService		0x000000E0
#define COD_Minor_Lan_0			0x00000000

#define COD_Minor_Audio_Headset		0x00000004
#define COD_Minor_Audio_Unclassified	0x00000000

#define COD_Minor_Any			0x000000FC


void dev_service_class2str(char *result, int result_size, int dev_class);
int dev_major2str(char *result, int result_size, int dev_class);
void dev_minor2str(char *result, int result_size, int dev_class);
void devclass2str(char *result, int result_size, int devclass);  

#endif
