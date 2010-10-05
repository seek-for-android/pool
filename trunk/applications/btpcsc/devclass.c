/*****************************************************************
/
/ File   :   bt_pcsc_config.h
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   October 5, 2010
/ Purpose:   Provides functions to convert device class information
/            into human-readable text.
/
******************************************************************/

#include "devclass.h"

#include <string.h>
#include <stdio.h>

#define ncod_classes 9
const int cod_classes[ncod_classes] = {COD_ServiceAny, COD_LimitedDiscoverableMode, COD_Networking,
    COD_Rendering, COD_Capturing, COD_ObjectTransfer, COD_Audio, COD_Telephony, COD_Information};
const char *cod_classes_default = "None";
const char *cod_classes_strings[ncod_classes] = {"Any Service", "Limited Discoverable Mode", "Networking", "Rendering", "Capturing", "Object Transfer", "Audio", "Telephony", "Information"};

#define ncod_majors 6
int cod_majors[ncod_majors] = {COD_Major_Computer, COD_Major_Phone, COD_Major_Lan_Access_Point, COD_Major_Audio, COD_Major_Peripheral, COD_Major_Unclassified};
const char *cod_majors_default = "Miscellaneous";
const char *cod_majors_strings[ncod_majors] = {"Computer", "Phone", "LAN Access Point", "Audio", "Peripheral", "Unclassified"};

#define ncod_minors_comp 5
int cod_minors_comp[ncod_minors_comp] = {COD_Minor_Comp_Laptop, COD_Minor_Comp_Desktop,     COD_Minor_Comp_Server, COD_Minor_Comp_Handheld, COD_Minor_Comp_Palm};
const char *cod_minors_comp_default = "Unclassified";
const char *cod_minors_comp_strings[ncod_minors_comp] = {"Laptop", "Desktop", "Server", "Handheld",
    "Palm"};

#define ncod_minors_phone 4
int cod_minors_phone[ncod_minors_phone] = {COD_Minor_Phone_Smart, COD_Minor_Phone_Cellular,
    COD_Minor_Phone_Cordless, COD_Minor_Phone_Modem};
const char *cod_minors_phone_default = "Unclassified";
const char *cod_minors_phone_strings[ncod_minors_phone] = {"Smart", "Cellular", "Cordless", "Modem"};

#define ncod_minors_lan 7
int cod_minors_lan[ncod_minors_lan] = {COD_Minor_Lan_NoService, COD_Minor_Lan_99, COD_Minor_Lan_83,
    COD_Minor_Lan_67, COD_Minor_Lan_50, COD_Minor_Lan_33, COD_Minor_Lan_17};
const char *cod_minors_lan_default = "LAN 0";
const char *cod_minors_lan_strings[ncod_minors_lan] = {"No Service", "LAN 99", "LAN 83", "LAN 67",
    "LAN 50", "LAN 33", "LAN 17"};

#define ncod_minors_audio 1
int cod_minors_audio[ncod_minors_audio] = {COD_Minor_Audio_Headset};
const char *cod_minors_audio_default = "Unclassified";
const char *cod_minors_audio_strings[ncod_minors_audio] = {"Headset"};

void dev_service_class2str(char *result, int result_size, int dev_class) {

    int i, n;
    for (i = 0, n = 0; i < ncod_classes; i++) {
        if ((dev_class & cod_classes[i]) == cod_classes[i]) {
            dev_class &= ~cod_classes[i];
            if (n > 0)
		strncat(result, ", ", result_size);
            strncat(result, cod_classes_strings[i], result_size);
	    n++;
        }
    }

    if (n == 0)
        strncat(result, cod_classes_default, result_size);

}

int dev_major2str(char *result, int result_size, int dev_class) {
    
    int i, n, major = -1, majorcode = dev_class & 0x1F00;
    for (i = 0, n = 0; i < ncod_majors; i++) {
        if (majorcode == cod_majors[i]) {
            major = i;
            strncat(result, cod_majors_strings[i], result_size);
            n++;
	    break;
        }
    }

    if (n == 0) {
        strncpy(result, cod_majors_default, result_size);
        return ncod_majors;
    }

    return major;

}

void dev_minor2strwithmajor(char *result, int result_size, int dev_class, int major) {
    
    if ((dev_class & COD_Minor_Any) == COD_Minor_Any) {
        strncpy(result, "Any", result_size);
        return;
    }

    int ncod_minors;
    const int *cod_minors;
    const char *cod_minors_default;
    const char **cod_minors_strings;

    switch (major) {
    case 0:
        ncod_minors = ncod_minors_comp;
        cod_minors = cod_minors_comp;
        cod_minors_default = cod_minors_comp_default;
        cod_minors_strings = cod_minors_comp_strings;
        break;
    case 1:
        ncod_minors = ncod_minors_phone;
        cod_minors = cod_minors_phone;
        cod_minors_default = cod_minors_phone_default;
        cod_minors_strings = cod_minors_phone_strings;
        break;
    case 2:
        ncod_minors = ncod_minors_lan;
        cod_minors = cod_minors_lan;
        cod_minors_default = cod_minors_lan_default;
        cod_minors_strings = cod_minors_lan_strings;
        break;
    case 3:
        ncod_minors = ncod_minors_audio;
        cod_minors = cod_minors_audio;
        cod_minors_default = cod_minors_audio_default;
        cod_minors_strings = cod_minors_audio_strings;
        break;
    default:
        strncpy(result, "Invalid", result_size);
        return;
    }

    int i, n;
    for (i = 0, n = 0; i < ncod_minors; i++) {
        if ((dev_class & cod_minors[i]) == cod_minors[i]) {
            dev_class &= ~cod_minors[i];
            if (n > 0)
		strncat(result, ", ", result_size);
            strncat(result, cod_minors_strings[i], result_size);
	    n++;
        }
    }

    if (n == 0)
        strncat(result, cod_minors_default, result_size);

}

void dev_minor2str(char *result, int result_size, int dev_class) {
    if ((dev_class & COD_Minor_Any) == COD_Minor_Any) {
        strncpy(result, "Any", result_size);
        return;
    }

    int i, n, major = -1, majorcode = dev_class & 0x1F00;
    for (i = 0, n = 0; i < ncod_majors; i++) {
        if (majorcode == cod_majors[i]) {
            major = i;
	    n++;
            break;
        }
    }

    if (n == 0) {
        strncpy(result, "Miscellaneous", result_size);
	major = ncod_majors;
    }

    dev_minor2strwithmajor(result, result_size, dev_class, major);
}

void devclass2str(char *result, int result_size, int devclass) {
    char service_class[255] = {0}, major[255] = {0}, minor[255] = {0};

    dev_service_class2str(service_class, 255, devclass);
    int majorindex = dev_major2str(major, 255, devclass);
    dev_minor2strwithmajor(minor, 255, devclass, majorindex);

    snprintf(result, result_size, "Service class: %s\nMajor: %s\nMinor: %s\n", 
        service_class, major, minor);
        
}
