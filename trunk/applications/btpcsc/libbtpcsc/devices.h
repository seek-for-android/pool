#ifndef DEVICES_H
#define DEVICES_H

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
