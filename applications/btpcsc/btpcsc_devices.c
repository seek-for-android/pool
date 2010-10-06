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
/ File   :   btpcsc_devices.c
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   October 4, 2010
/ Purpose:   Provides functions to scan for bluetooth devices and
/            retrieve information about them.
/
******************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdint.h>
#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include "btpcsc_bluetooth.h"
#include "btpcsc_devclass.h"

#include "btpcsc_devices.h"

void device2str(char *result, int resultsize, bt_device *device) {
    char device_class[1024] = {0};
    devclass2str(device_class, 1024, device->class);
    snprintf(result, resultsize, "Address: %s\nDevice name: %s\nDevice class: %.8x\n%s\n",
        device->addr, device->name, device->class, device_class);
}

char *get_device_name(char *addr, int maxlength, char *name) {

    int adapter = hci_get_route(NULL);
    if (adapter < 0) {
        strcpy(name, "[unknown]");
        return name;
    }

    int sock = hci_open_dev(adapter);
    if (sock < 0) {
        strcpy(name, "[unknown]");
        return name;
    }

    bdaddr_t bdaddr;
    str2ba(addr, &bdaddr);
    int result = hci_read_remote_name(sock, &bdaddr, maxlength, name, 0);
    if (result < 0) strcpy(name, "[unknown]");

    return name;
}

int scan_for_bt_devices(bt_device *devices, int adapter, int sock, int maxdevices, 
	float durationsecs, char flush) {

    inquiry_info *ii = NULL;

    if (adapter == 0) {
        adapter = hci_get_route(NULL);
        if (adapter < 0) return DEVICE_ERROR_ADAPTER;
    }

    if (sock == 0) {
        sock = hci_open_dev(adapter);
        if (sock < 0) return DEVICE_ERROR_SOCKET;
    }

    int flags = (flush) ? IREQ_CACHE_FLUSH : 0;
    ii = (inquiry_info*) malloc(maxdevices * sizeof(inquiry_info));
    
    int ndevices = hci_inquiry(adapter, (int) (durationsecs / 1.28), maxdevices, NULL, &ii, flags);
    if(ndevices < 0) {
        return DEVICE_ERROR_INQUIRY;
    }

    bt_device *device = devices;

    int i;
    for (i = 0; i < ndevices; i++, device++) {
        memset(device->addr, 0, DEVICE_ADDR_LENGTH);
        memset(device->name, 0, DEVICE_NAME_LENGTH);        

        ba2str(&(ii+i)->bdaddr, device->addr);
        int result = hci_read_remote_name(sock, &(ii+i)->bdaddr, DEVICE_NAME_LENGTH, device->name, 0);
        if (result < 0) strcpy(device->name, "[unknown]");
	uint8_t *dev_class_array = (ii+i)->dev_class;
        device->class = ((int) dev_class_array[2] << 16) | ((int) dev_class_array[1] << 8) |
            dev_class_array[0];
    }

    free(ii);
    return ndevices;
}

int scan_for_bt_devices_with_service(bt_device *devices, int adapter, int socket, int maxdevices, 
	float durationsecs, char flush, uint8_t *uuid_str) {

    bt_device tmpdevices[maxdevices];
    int ntmpdevices = scan_for_bt_devices(tmpdevices, adapter, socket, maxdevices,
        durationsecs, flush);

    if (ntmpdevices < 0)
      return ntmpdevices;

    bt_device *tmpdevice = tmpdevices, *device = devices;
    int i, ndevices = 0;
    for (i = 0; i < ntmpdevices; i++, tmpdevice++) {
       int channel = find_channel_by_uuid(tmpdevice->addr, uuid_str);
       if (channel >= 0) {
          memcpy(device, tmpdevice, sizeof(bt_device));
          device++;
          ndevices++;
       }
    }

    return ndevices;

}

int select_dest(char *addr, int socket, int adapter) {
    bt_device devices[255];
    int ndevices = scan_for_bt_devices(devices, adapter, socket, 255, 10.24, 1);

    if (ndevices == 0) {
        printf("Scan returned no devices in range.\n");
        return -1;
    } else if (ndevices == 1) {
        printf("Scan returned 1 device in range.\n\n");
    } else {
      printf("Scan returned %d devices in range.\n\n", ndevices);
    }

    char devstr[512] = {0};
    bt_device *device = devices;
    int i;
    for (i = 0; i < ndevices; i++, device++) {
        device2str(devstr, sizeof(devstr), device);
        printf("Device %d\n%s\n", i, devstr);
    }

    int dest_id = -1;
    do {
	printf("Please enter the number of the device to which you want to connect. (0-%d)\n", 
            ndevices - 1);
        int tmp = scanf("%d", &dest_id);
        if (tmp != 1 && dest_id < 0 && dest_id >= ndevices) {
            dest_id = -1;
            printf("Invalid input. ");
        }
    } while (dest_id < 0);

    memcpy(addr, devices[dest_id].addr, sizeof(devices[dest_id].addr));

    return dest_id;

}

