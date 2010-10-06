/*
 * Copyright 2010 Manuel Eberl <manueleberl@gmx.de> for Giesecke & Devrient
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

/*****************************************************************
/
/ File   :   btpcsc_devices.h
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   October 4, 2010
/ Purpose:   Provides functions to scan for bluetooth devices and
/            retrieve information about them.
/
******************************************************************/


#ifndef _BTPCSC_DEVICES_H_
#define _BTPCSC_DEVICES_H_

#define DEVICE_ERROR_ADAPTER -1;
#define DEVICE_ERROR_SOCKET -2;
#define DEVICE_ERROR_INQUIRY -3;

#define DEVICE_ADDR_LENGTH 19
#define DEVICE_NAME_LENGTH 248

#include <stdint.h>

typedef struct {
    char addr[DEVICE_ADDR_LENGTH];
    char name[DEVICE_NAME_LENGTH];
    int class;
} bt_device;

void device2str(char *result, int resultsize, bt_device *device);
char *get_device_name(char *addr, int maxlength, char *name);
int scan_for_bt_devices(bt_device *devices, int adapter, int socket, int maxdevices, 
	float durationsecs, char flush);
int scan_for_bt_devices_with_service(bt_device *devices, int adapter, int socket, int maxdevices, 
	float durationsecs, char flush, uint8_t *uuid_str);

#endif
