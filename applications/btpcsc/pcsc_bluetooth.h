/*****************************************************************
/
/ File   :   pcsp_bluetooth.h
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   September 30, 2010
/ Purpose:   Provides functions to establish and control a
/            Bluetooth connection for the PCSC Bluetooth bridge
/ License:   See file LICENSE
/
******************************************************************/

#ifndef _bluetooth_h_
#define _bluetooth_h_

#ifdef __cplusplus
extern "C" {
#endif 

#define BT_PCSC_SUCCESS 0
#define BT_PCSC_ERROR_NOT_SUPPORTED -4200
#define BT_PCSC_ERROR_UNKNOWN_CMD -4201
#define BT_PCSC_ERROR_NO_SUCH_READER -4202
#define BT_PCSC_ERROR_CONNECTION -4203
#define BT_PCSC_ERROR_CONNECTION_CONNECT -4204
#define BT_PCSC_ERROR_CONNECTION_SDP -4205
#define BT_PCSC_ERROR_CONNECTION_SERVER_NOT_ACTIVE -4206
#define BT_PCSC_ERROR_CONNECTION_CHANNEL_CLOSED -4207
#define BT_PCSC_ERROR_INVALID_ACK -4208;
#define BT_PCSC_ERROR_DISCONNECTED -4209
#define BT_PCSC_ERROR_RECV -4217
#define BT_PCSC_ERROR_INVALID_SLOT -4218

#define BT_PCSC_UUID {0x42, 0x21, 0x9a, 0xbb, 0x16, 0x15, 0x44, 0x86, 0xbd, 0x50, 0x49, 0x6b, 0xd5, 0x04, 0x96, 0xd8}
#define BT_PCSC_ACK_CONNECTION {0x00, 0x00, 0x30, 0xF8}

#define BT_PCSC_CMD_ACK 1
#define BT_PCSC_CMD_DISCONNECT 2
#define BT_PCSC_CMD_SEND_APDU 16
#define BT_PCSC_CMD_RECV_APDU 17
#define BT_PCSC_CMD_GET_PRESENT 24
#define BT_PCSC_CMD_GET_PRESENT_RESULT 25
#define BT_PCSC_CMD_GET_SLOTS 32
#define BT_PCSC_CMD_GET_SLOTS_RESULT 33
#define BT_PCSC_CMD_SET_SLOT 34
#define BT_PCSC_CMD_NOT_SUPPORTED 254
#define BT_PCSC_CMD_ERROR 255

#include <stdint.h>

typedef struct bt_pcsc_connection {
    int lun, channel;
    char remote_addr[18];
    int socket;
    struct bt_pcsc_connection *next;
} bt_pcsc_connection;



// Creates a new connection, adds it to the list and returns a pointer to it.
bt_pcsc_connection *add_connection(int lun, int channel, char *remote_addr);

// Removes a connection, but does NOT close it.
void remove_connection(int lun);

// Finds and returns the connection with the specified lun or NULL if there
// is no such connection.
bt_pcsc_connection *get_connection(int lun);

// Establishes a bluetooth connection to the service with the right UUID
// on the device with the specified address.
int bt_connect(bt_pcsc_connection *connection);

// Transmits an APDU over the specified connection
int bt_recv_apdu(bt_pcsc_connection *connection, uint16_t *apdu_length, void *apdu);

// Receives an APDU over the specified connection
int bt_recv_apdu(bt_pcsc_connection *connection, uint16_t *apdu_length, void *apdu);

// Asks the server how many readers it has and what their names are
int bt_get_slots(bt_pcsc_connection *connection, char *slots[], int maxslots);

// Sets the card reader to be used by this connection
int bt_set_slot(bt_pcsc_connection *connection, char *slot);

// Closes the connection gracefully
int bt_disconnect(bt_pcsc_connection *connection);


#ifdef __cplusplus
}
#endif

#endif
