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
/ File   :   btpcsp_bluetooth.c
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   September 30, 2010
/ Purpose:   Provides functions to establish and control a
/            Bluetooth connection for the PCSC Bluetooth bridge
/
******************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>
#include <string.h>
#include <stdint.h>
#include <pthread.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>
#include "btpcsc_devices.h"

#include "btpcsc_bluetooth.h"


bt_pcsc_connection *first_connection;
pthread_mutex_t mutex_connections_modification = PTHREAD_MUTEX_INITIALIZER;

// Creates a new connection, adds it to the list and returns a pointer
// to it.
bt_pcsc_connection *add_connection(int lun, int channel, char *remote_addr) {

    pthread_mutex_lock(&mutex_connections_modification);

    // Initialise connection
    bt_pcsc_connection *connection = malloc(sizeof(bt_pcsc_connection));
    connection->lun = lun;
    connection->channel = channel;
    connection->socket = 0;
    connection->remote_addr[0] = 0;
    strncat(connection->remote_addr, remote_addr, 17);

    // Append new connection to linked list or create list if no
    // connections exist.
    if (first_connection) {
        bt_pcsc_connection *last_connection = first_connection;
        while (last_connection->next)
            last_connection = last_connection->next;
        last_connection->next = connection;
    } else {
        first_connection = connection;
    }

    pthread_mutex_unlock(&mutex_connections_modification);
    return connection;
}



// Removes a connection, but does NOT close it.
void remove_connection(int lun) {
    bt_pcsc_connection *connection = NULL, *last_connection = NULL;
    
    pthread_mutex_lock(&mutex_connections_modification);

    // Find connection with the right lun
    connection = first_connection;
    while (connection && connection->lun != lun) {
        last_connection = connection;
        connection = connection->next;
    }

    // Does a connection with this lun even exist? If not, nothing to do
    if (connection) {
        // Is there a connection before the connection we want to delete?
        if (last_connection) {
            // Yes. Set its next pointer to the connection after the
            // one we're about to delete.
            last_connection->next = connection->next;
        } else {
            // No. The connection after the one we're deleting is the
            // new first connection.
            first_connection = connection->next;
        }

        free(connection);
    }

    pthread_mutex_unlock(&mutex_connections_modification);

}



// Finds and returns the connection with the specified lun or NULL if there
// is no such connection.
bt_pcsc_connection *get_connection(int lun) {
    bt_pcsc_connection *connection = NULL;
    pthread_mutex_lock(&mutex_connections_modification);
    
    // Find connection with the right lun
    connection = first_connection;
    while (connection && connection->lun != lun)
        connection = connection->next;

    // Return the pointer to the right connection (NULL if none exists)
    pthread_mutex_unlock(&mutex_connections_modification);
    return connection;
}



int find_channel_by_uuid(char *addr, void *uuid_int) {
    uuid_t svc_uuid;
    int err;
    bdaddr_t target;
    sdp_list_t *response_list = NULL, *search_list, *attrid_list;
    sdp_session_t *session = 0;

    str2ba(addr, &target);

    session = sdp_connect(BDADDR_ANY, &target, SDP_RETRY_IF_BUSY);

    if (session == 0) {
        return BT_PCSC_ERROR_CONNECTION_SDP;
    }

    // Create the UUID
    sdp_uuid128_create(&svc_uuid, uuid_int);

    search_list = sdp_list_append(NULL, &svc_uuid);

    // Set attribute range to all
    uint32_t range = 0x0000ffff;
    attrid_list = sdp_list_append(NULL, &range);

    // Search for services with correct ID
    err = sdp_service_search_attr_req( session, search_list, \
            SDP_ATTR_REQ_RANGE, attrid_list, &response_list);
    sdp_list_t *r = response_list;

    int channel = -1;

    // Traverse service records
    while (r) {
        sdp_record_t *rec = (sdp_record_t*) r->data;
        sdp_list_t *proto_list;
        
        // Retrieve protocol sequences
        if( sdp_get_access_protos(rec, &proto_list) == 0 ) {
            sdp_list_t *p = proto_list;

            // Traverse protocol sequence
            while (p) {
                sdp_list_t *pds = (sdp_list_t*) p->data;
    
                // Traverse protocol list
                while (pds) {
    
                    // Check attributes
                    sdp_data_t *d = (sdp_data_t*) pds->data;
                    int proto = 0;
                    while (d) {

                        switch(d->dtd) { 
                            case SDP_UUID16:
                            case SDP_UUID32:
                            case SDP_UUID128:
                                proto = sdp_uuid_to_proto(&d->val.uuid);
                                break;
                            case SDP_UINT8:
                                if( proto == RFCOMM_UUID ) {
                                    // We will just assume there's only one service with the UUID
                                    // running, so we return the first channel we find.
                                    channel = d->val.int8;
                                }
                                break;
                        }

                        d = d->next;
                    }

                    pds = pds->next;
                }

                sdp_list_free((sdp_list_t*)p->data, 0);
                p = p->next;
            }
        
            sdp_list_free(proto_list, 0);

        }

        sdp_record_free(rec);
        r = r->next;
    }

    sdp_close(session);

    return channel;
}


void initmutex(pthread_mutex_t *mutex) {
    pthread_mutex_t tmpmutex = PTHREAD_MUTEX_INITIALIZER;
    memcpy(mutex, &tmpmutex, sizeof(pthread_mutex_t));
}

// Establishes a bluetooth connection to the service with the right UUID
// on the device with the specified address.
int bt_connect(bt_pcsc_connection *connection) {

    initmutex(&connection->mutex);
    pthread_mutex_lock(&connection->mutex);

    uint8_t uuid[] = BT_PCSC_UUID;
    connection->socket = 0;
    int channel = find_channel_by_uuid(connection->remote_addr, uuid);

    if (channel < 0) {
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_CONNECTION_SERVER_NOT_ACTIVE;
    }

    int adapter = hci_get_route(NULL);
    int sock = hci_open_dev(adapter);

    struct sockaddr_rc addr = {0};
    int status;

    // Allocate a socket
    int s = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);

    // Set connection parameters
    addr.rc_family = AF_BLUETOOTH;
    addr.rc_channel = (uint8_t) channel;
    str2ba(connection->remote_addr, &addr.rc_bdaddr);
    // Try to establish a connection
    status = connect(s, (struct sockaddr *) &addr, sizeof(addr));
    if (status < 0) {
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_CONNECTION_CONNECT;
    }

    // Check if the server transmits the ack code. If it does not,
    // it's not a Bluetooth PCSC server.
    uint8_t buffer[4], ack_code[] = BT_PCSC_ACK_CONNECTION;
    status = read(s, buffer, 4);

    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    if (memcmp(buffer, ack_code, 4) != 0) {
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_INVALID_ACK;
    }

    // Everything set up and running, set the socket and return success.
    connection->socket = s;
    pthread_mutex_unlock(&connection->mutex);    
    return BT_PCSC_SUCCESS;
}



int handle_command(bt_pcsc_connection *connection, uint8_t cmd) {

    if (connection->socket == 0)
        return BT_PCSC_ERROR_DISCONNECTED;

    switch (cmd) {
    case BT_PCSC_CMD_DISCONNECT:
        connection->socket = 0;
        break;
    case BT_PCSC_CMD_NOT_SUPPORTED:
        return BT_PCSC_ERROR_NOT_SUPPORTED;
    default:
        return BT_PCSC_ERROR_UNKNOWN_CMD;
    }

    return BT_PCSC_SUCCESS;
}

int bt_read_cmd(bt_pcsc_connection *connection, uint8_t requested_command) {
    return bt_read_cmds(connection, 1, &requested_command);
}

// Reads and handles commands from the connection until the desired command
// is found.
int bt_read_cmds(bt_pcsc_connection *connection, int n_requested_commands, uint8_t *requested_commands) {

    if (connection->socket == 0)
        return BT_PCSC_ERROR_DISCONNECTED;

    uint8_t cmd;
    do {

        int status = read(connection->socket, &cmd, 1);
        if (status < 0) {
            connection->socket = 0;
            return BT_PCSC_ERROR_DISCONNECTED;
        }

        int i;
        for (i = 0; i < n_requested_commands; i++)
          if (cmd == requested_commands[i])
              return cmd;

        int result = handle_command(connection, cmd);
        if (result < 0)
            return result;

    } while (connection->socket != 0);

    return BT_PCSC_ERROR_DISCONNECTED;
}



// Transmits an APDU over the specified connection
int bt_send_apdu(bt_pcsc_connection *connection, uint16_t apdu_length, void *apdu) {

    if (connection->socket == 0)
        return BT_PCSC_ERROR_DISCONNECTED;

    pthread_mutex_lock(&connection->mutex);

    uint8_t buffer[apdu_length + 3];
    buffer[0] = BT_PCSC_CMD_SEND_APDU;
    buffer[1] = (apdu_length >> 8) & 0xFF;
    buffer[2] = apdu_length & 0xFF;
    memcpy(buffer + 3, apdu, apdu_length);
    
    int status = write(connection->socket, buffer, apdu_length + 3);

    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    pthread_mutex_unlock(&connection->mutex);
    return BT_PCSC_SUCCESS;

}



// Receives an APDU over the specified connection
int bt_recv_apdu(bt_pcsc_connection *connection, uint16_t *apdu_length, void *apdu) {

    if (connection->socket == 0)
        return BT_PCSC_ERROR_DISCONNECTED;

    pthread_mutex_lock(&connection->mutex);

    int status;
    uint8_t _length[2];
    uint16_t length;
    *apdu_length = 0;

    status = bt_read_cmd(connection, BT_PCSC_CMD_RECV_APDU);
    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    status = read(connection->socket, _length, 2);
    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    length = (_length[0] << 8) | _length[1];

    if (length > 0) {
        status = read(connection->socket, apdu, length);
        if (status < 0) {
            connection->socket = 0;
            pthread_mutex_unlock(&connection->mutex);
            return BT_PCSC_ERROR_DISCONNECTED;
        }
    }

    *apdu_length = length;
    pthread_mutex_unlock(&connection->mutex);
    return BT_PCSC_SUCCESS;
}


int bt_is_card_present(bt_pcsc_connection *connection) {

    if (connection->socket == 0)
        return BT_PCSC_ERROR_DISCONNECTED;

    pthread_mutex_lock(&connection->mutex);

    int status;
    uint8_t cmd = BT_PCSC_CMD_GET_PRESENT;
    status = write(connection->socket, &cmd, 1);
    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    status = bt_read_cmd(connection, BT_PCSC_CMD_GET_PRESENT_RESULT);
    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    uint8_t present_result;
    status = read(connection->socket, &present_result, 1);
    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    pthread_mutex_unlock(&connection->mutex);
    return (present_result != 0) ? 1 : 0;
}


// Asks the server how many readers it has and what their names are
int bt_get_slots(bt_pcsc_connection *connection, char *slots[], int maxslots) {
    uint8_t buffer[2] = {BT_PCSC_CMD_GET_SLOTS, 255};
    
    pthread_mutex_lock(&connection->mutex);

    int status;
    status = write(connection->socket, buffer, 2);
    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    status = bt_read_cmd(connection, BT_PCSC_CMD_GET_SLOTS_RESULT);
    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    uint8_t nslots = 0;
    status = read(connection->socket, &nslots, 1);
    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    int i;
    for (i = 0; i < maxslots; i++) slots[i] = NULL;

    for (i = 0; i < nslots && i < maxslots; i++) {
        uint8_t length = 0;
        status = read(connection->socket, &length, 1);
        if (status < 0) {
            connection->socket = 0;
            pthread_mutex_unlock(&connection->mutex);
            return BT_PCSC_ERROR_DISCONNECTED;
        }

        slots[i] = malloc(length + 1);
        status = read(connection->socket, slots[i], length);
        slots[i][length] = 0;
        if (status < 0) {
            connection->socket = 0;
            pthread_mutex_unlock(&connection->mutex);
            return BT_PCSC_ERROR_DISCONNECTED;
        }
    }

    pthread_mutex_unlock(&connection->mutex);
    return nslots;

}

// Sets the card reader to be used by this connection
int bt_set_slot(bt_pcsc_connection *connection, char *slot) {
    int _length = strlen(slot);
    uint8_t length = (_length > 255) ? 255 : _length;
    uint8_t buffer[length + 2];
    buffer[0] = BT_PCSC_CMD_SET_SLOT;
    buffer[1] = length;
    memcpy(buffer + 2, slot, length);

    pthread_mutex_lock(&connection->mutex);

    int status;
    status = write(connection->socket, buffer, length + 2);
    if (status < 0) {
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }

    uint8_t cmds[2] = {BT_PCSC_CMD_ACK, BT_PCSC_CMD_ERROR};
    status = bt_read_cmds(connection, 2, cmds);
    switch (status) {
    case BT_PCSC_CMD_ACK:
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_SUCCESS;
    case BT_PCSC_CMD_ERROR:
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_INVALID_SLOT;
    default:
        connection->socket = 0;
        pthread_mutex_unlock(&connection->mutex);
        return BT_PCSC_ERROR_DISCONNECTED;
    }
}


// Closes the connection gracefully
int bt_disconnect(bt_pcsc_connection *connection) {
    if (connection->socket == 0)
        return BT_PCSC_ERROR_DISCONNECTED;

    pthread_mutex_lock(&connection->mutex);

    uint8_t cmd = BT_PCSC_CMD_DISCONNECT;
    write(connection->socket, &cmd, sizeof(cmd));
    close(connection->socket);
    connection->socket = 0;

    pthread_mutex_unlock(&connection->mutex);

    return BT_PCSC_SUCCESS;
}

