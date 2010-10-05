/*****************************************************************
/
/ File   :   pcsp_bluetoothhandler.c
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   September 30, 2010
/ Purpose:   Provides a transparent reader bridge to a bluetooth
/            device.
/
******************************************************************/

#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include "../devices.h"
#include "../btpcsc_config.h"
#include "../pcsc_bluetooth.h"
#include "pcsc_bluetoothhandler.h"

virtual_reader *get_virtual_reader(DWORD Channel) {
    parse_config("/etc/btpcsc.conf");

    virtual_reader *reader;
    for (reader = first_reader; reader; reader = reader->next) {
        if (reader->id == (int) Channel)
            return reader;
    }

    free_readers();

    return NULL;
}

// Reestablishes the given connection if it is closed
int ensure_connection_is_active(int Lun, int Channel) {

    bt_pcsc_connection *connection = get_connection(Lun);

    // Do we have any connection data?
    if (connection == NULL) {
        if (Channel >= -1) {
            // There is no connection data, but the channel is still open.
            // (Or has just been opened by CreateChannel)

            // Try to find the reader matching the given channel in the config file
            virtual_reader *reader = get_virtual_reader(Channel);
            if (!reader) {
                printf("No BT_PCSC reader with ID %d.\n", Channel);
                return BT_PCSC_ERROR_NO_SUCH_READER;
            }

            connection = add_connection(Lun, Channel, reader->address);   

        } else {
            // The channel was already closed. pcscd should know this.
            return BT_PCSC_ERROR_CONNECTION_CHANNEL_CLOSED;
        }
    }

    if (!connection)
        return BT_PCSC_ERROR_DISCONNECTED;

    Channel = connection->channel;

    // Is the bluetooth connection still active?
    if (connection->socket == 0) {
        // No, try to connect.
        int result = bt_connect(connection);

        // Try to set the correct slot
        virtual_reader *reader = get_virtual_reader(Channel);
        if (!reader) {
            printf("No BT_PCSC reader with ID %d.\n", Channel);
            return BT_PCSC_ERROR_NO_SUCH_READER;
        }
        if (result >= 0 && strlen(reader->slot) > 0) {
            // Try to set the specified slot
            int result = bt_set_slot(connection, reader->slot);
            if (result == BT_PCSC_ERROR_INVALID_SLOT)
                printf("Specified slot %s of reader %s (ID %d) is not available, using \
                    default slot instead.\n", reader->slot, reader->name, Channel);
            else
                return result;
        }
    } else {
        // Yes, nothing to do.
        return BT_PCSC_SUCCESS;
    }
}

RESPONSECODE IFDHCreateChannel ( DWORD Lun, DWORD Channel ) {

  /* Lun - Logical Unit Number, use this for multiple card slots 
     or multiple readers. 0xXXXXYYYY -  XXXX multiple readers,
     YYYY multiple slots. The resource manager will set these 
     automatically.  By default the resource manager loads a new
     instance of the driver so if your reader does not have more than
     one smartcard slot then ignore the Lun in all the functions.
     Future versions of PC/SC might support loading multiple readers
     through one instance of the driver in which XXXX would be important
     to implement if you want this.
  */
  
  /* Channel - Channel ID.  This is denoted by the following:
     0x000001 - /dev/pcsc/1
     0x000002 - /dev/pcsc/2
     0x000003 - /dev/pcsc/3
     
     USB readers may choose to ignore this parameter and query 
     the bus for the particular reader.
  */

  /* This function is required to open a communications channel to the 
     port listed by Channel.  For example, the first serial reader on COM1 would
     link to /dev/pcsc/1 which would be a sym link to /dev/ttyS0 on some machines
     This is used to help with intermachine independance.
     
     Once the channel is opened the reader must be in a state in which it is possible
     to query IFDHICCPresence() for card status.
 
     returns:

     IFD_SUCCESS
     IFD_COMMUNICATION_ERROR
  */

    // We'll try to establish a connection, but return success even if there is no
    // bt_pcsc server available. Otherwise, pcscd would refuse to use the device.

    ensure_connection_is_active(Lun, Channel);

    return IFD_SUCCESS;

}

RESPONSECODE IFDHCloseChannel ( DWORD Lun ) {

  /* This function should close the reader communication channel
     for the particular reader.  Prior to closing the communication channel
     the reader should make sure the card is powered down and the terminal
     is also powered down.

     returns:

     IFD_SUCCESS
     IFD_COMMUNICATION_ERROR     
  */
  
    bt_pcsc_connection *connection = get_connection(Lun);

    if (connection) {
        bt_disconnect(connection);
        remove_connection(connection->lun);
    }

    return IFD_SUCCESS;

}

RESPONSECODE IFDHGetCapabilities ( DWORD Lun, DWORD Tag, 
				   PDWORD Length, PUCHAR Value ) {

  /* This function should get the slot/card capabilities for a particular
     slot/card specified by Lun.  Again, if you have only 1 card slot and don't mind
     loading a new driver for each reader then ignore Lun.

     Tag - the tag for the information requested
         example: TAG_IFD_ATR - return the Atr and it's size (required).
         these tags are defined in ifdhandler.h

     Length - the length of the returned data
     Value  - the value of the data

     returns:
     
     IFD_SUCCESS
     IFD_ERROR_TAG
  */

    RESPONSECODE ret = IFD_ERROR_TAG;

	switch(Tag) {

        // Return 1 slot for now.
	case TAG_IFD_SLOTS_NUMBER:
            *Length = 1;
            *Value = 1;
            ret = IFD_SUCCESS;
            break;

        // Just return a generic ATR. The phone does not support ATR reading yet.
        case TAG_IFD_ATR:
            *Length = 2;
            Value[0] = 0x3B;
            Value[1] = 0x00;
            ret = IFD_SUCCESS;
            break;

	}

    // TODO: Possibly implement more stuff here

    return ret;

}

RESPONSECODE IFDHSetCapabilities ( DWORD Lun, DWORD Tag, 
			       DWORD Length, PUCHAR Value ) {

  /* This function should set the slot/card capabilities for a particular
     slot/card specified by Lun.  Again, if you have only 1 card slot and don't mind
     loading a new driver for each reader then ignore Lun.

     Tag - the tag for the information needing set

     Length - the length of the returned data
     Value  - the value of the data

     returns:
     
     IFD_SUCCESS
     IFD_ERROR_TAG
     IFD_ERROR_SET_FAILURE
     IFD_ERROR_VALUE_READ_ONLY
  */

    // TODO: Actually implement this, if needed.
    return IFD_SUCCESS;
  
}

RESPONSECODE IFDHSetProtocolParameters ( DWORD Lun, DWORD Protocol, 
				   UCHAR Flags, UCHAR PTS1,
				   UCHAR PTS2, UCHAR PTS3) {

  /* This function should set the PTS of a particular card/slot using
     the three PTS parameters sent

     Protocol  - 0 .... 14  T=0 .... T=14
     Flags     - Logical OR of possible values:
     IFD_NEGOTIATE_PTS1 IFD_NEGOTIATE_PTS2 IFD_NEGOTIATE_PTS3
     to determine which PTS values to negotiate.
     PTS1,PTS2,PTS3 - PTS Values.

     returns:

     IFD_SUCCESS
     IFD_ERROR_PTS_FAILURE
     IFD_COMMUNICATION_ERROR
     IFD_PROTOCOL_NOT_SUPPORTED
  */

    // We don't really need any of this - we don't care about protocols.
    return IFD_SUCCESS;

}


RESPONSECODE IFDHPowerICC ( DWORD Lun, DWORD Action, 
			    PUCHAR Atr, PDWORD AtrLength ) {

  /* This function controls the power and reset signals of the smartcard reader
     at the particular reader/slot specified by Lun.

     Action - Action to be taken on the card.

     IFD_POWER_UP - Power and reset the card if not done so 
     (store the ATR and return it and it's length).
 
     IFD_POWER_DOWN - Power down the card if not done already 
     (Atr/AtrLength should
     be zero'd)
 
    IFD_RESET - Perform a quick reset on the card.  If the card is not powered
     power up the card.  (Store and return the Atr/Length)

     Atr - Answer to Reset of the card.  The driver is responsible for caching
     this value in case IFDHGetCapabilities is called requesting the ATR and it's
     length.  This should not exceed MAX_ATR_SIZE.

     AtrLength - Length of the Atr.  This should not exceed MAX_ATR_SIZE.

     Notes:

     Memory cards without an ATR should return IFD_SUCCESS on reset
     but the Atr should be zero'd and the length should be zero

     Reset errors should return zero for the AtrLength and return 
     IFD_ERROR_POWER_ACTION.

     returns:

     IFD_SUCCESS
     IFD_ERROR_POWER_ACTION
     IFD_COMMUNICATION_ERROR
     IFD_NOT_SUPPORTED
  */

    int result;

    bt_pcsc_connection *connection;

    switch (Action) {

    // Cannot reset on the smartphone, just do the same thing as in power up.
    case IFD_RESET:
    case IFD_POWER_UP:
        *AtrLength = 2;
        memcpy(Atr, "\x3B\x00", 2);
        break;

    // Just disconnect, if it hasn't happened already.
    case IFD_POWER_DOWN:
        *AtrLength = 0;
        Atr = NULL;
        result = 0;
        break;

   }

   return IFD_SUCCESS;

}

RESPONSECODE IFDHTransmitToICC ( DWORD Lun, SCARD_IO_HEADER SendPci, 
				 PUCHAR TxBuffer, DWORD TxLength, 
				 PUCHAR RxBuffer, PDWORD RxLength, 
				 PSCARD_IO_HEADER RecvPci ) {
  
  /* This function performs an APDU exchange with the card/slot specified by
     Lun.  The driver is responsible for performing any protocol specific exchanges
     such as T=0/1 ... differences.  Calling this function will abstract all protocol
     differences.

     SendPci
     Protocol - 0, 1, .... 14
     Length   - Not used.

     TxBuffer - Transmit APDU example (0x00 0xA4 0x00 0x00 0x02 0x3F 0x00)
     TxLength - Length of this buffer.
     RxBuffer - Receive APDU example (0x61 0x14)
     RxLength - Length of the received APDU.  This function will be passed
     the size of the buffer of RxBuffer and this function is responsible for
     setting this to the length of the received APDU.  This should be ZERO
     on all errors.  The resource manager will take responsibility of zeroing
     out any temporary APDU buffers for security reasons.
  
     RecvPci
     Protocol - 0, 1, .... 14
     Length   - Not used.

     Notes:
     The driver is responsible for knowing what type of card it has.  If the current
     slot/card contains a memory card then this command should ignore the Protocol
     and use the MCT style commands for support for these style cards and transmit 
     them appropriately.  If your reader does not support memory cards or you don't
     want to then ignore this.

     RxLength should be set to zero on error.

     returns:
     
     IFD_SUCCESS
     IFD_COMMUNICATION_ERROR
     IFD_RESPONSE_TIMEOUT
     IFD_ICC_NOT_PRESENT
     IFD_PROTOCOL_NOT_SUPPORTED
  */

    int status;

    *RxLength = 0;

    // Make sure we have a connection
    status = ensure_connection_is_active(Lun, -1);
    if (status < 0) return IFD_ICC_NOT_PRESENT;

    // This shouldn't fail, we have just made sure there is one.
    bt_pcsc_connection *connection = get_connection(Lun);
    if (!connection) return IFD_ICC_NOT_PRESENT;

    // Try to send the APDU to the server
    status = bt_send_apdu(connection, TxLength, TxBuffer);
    if (status < 0) return IFD_ICC_NOT_PRESENT;

    // Try to receive the reply from the server.
    uint16_t _RxLength;
    status = bt_recv_apdu(connection, &_RxLength, RxBuffer);
    *RxLength = _RxLength;
    if (status < 0) return IFD_ICC_NOT_PRESENT;

    return IFD_SUCCESS;
  
}

RESPONSECODE IFDHControl ( DWORD Lun, PUCHAR TxBuffer, 
			 DWORD TxLength, PUCHAR RxBuffer, 
			 PDWORD RxLength ) {

  /* This function performs a data exchange with the reader (not the card)
     specified by Lun.  Here XXXX will only be used.
     It is responsible for abstracting functionality such as PIN pads,
     biometrics, LCD panels, etc.  You should follow the MCT, CTBCS 
     specifications for a list of accepted commands to implement.

     TxBuffer - Transmit data
     TxLength - Length of this buffer.
     RxBuffer - Receive data
     RxLength - Length of the received data.  This function will be passed
     the length of the buffer RxBuffer and it must set this to the length
     of the received data.

     Notes:
     RxLength should be zero on error.
  */

    // TODO: Figure out what this is supposed to do in the first place.

    return IFD_SUCCESS;

}

RESPONSECODE IFDHICCPresence( DWORD Lun ) {

  /* This function returns the status of the card inserted in the 
     reader/slot specified by Lun.  It will return either:

     returns:
     IFD_ICC_PRESENT
     IFD_ICC_NOT_PRESENT
     IFD_COMMUNICATION_ERROR
  */

    int status;

    status = ensure_connection_is_active(Lun, -1);
    if (status < 0) return IFD_ICC_NOT_PRESENT;
    bt_pcsc_connection *connection = get_connection(Lun);
    int result = bt_is_card_present(connection);
//        if (result < 0)
//            printf("\33[31m[bt_pcsc] Error %d\33[0m\n", result);

    return (result > 0) ? IFD_ICC_PRESENT : IFD_ICC_NOT_PRESENT;

}
